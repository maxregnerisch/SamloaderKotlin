package com.musicgen.audio;

import com.musicgen.composition.CompositionEngine;
import com.musicgen.effects.EffectsProcessor;
import com.musicgen.export.AudioExporter;
import com.musicgen.structure.SongStructure;
import com.musicgen.tracks.TrackManager;
import com.musicgen.vocals.VocalSynthesizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Core audio engine that orchestrates all music generation components
 * Handles MIDI synthesis, audio mixing, real-time playback, and export
 */
public class AudioEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(AudioEngine.class);
    
    // Audio configuration
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 4096;
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, 16, 2, 4, SAMPLE_RATE, false
    );
    
    // Core components
    private MidiHandler midiHandler;
    private SynthesizerManager synthesizerManager;
    private TrackManager trackManager;
    private CompositionEngine compositionEngine;
    private VocalSynthesizer vocalSynthesizer;
    private EffectsProcessor effectsProcessor;
    private AudioExporter audioExporter;
    
    // Audio system
    private Mixer mixer;
    private SourceDataLine audioLine;
    private boolean isInitialized = false;
    private boolean isPlaying = false;
    
    // Threading
    private ExecutorService executorService;
    
    // Current song data
    private SongStructure currentSong;
    private byte[] currentAudioData;
    
    public AudioEngine() {
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "AudioEngine-Worker");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * Initialize the audio engine and all components
     */
    public void initialize() throws Exception {
        logger.info("Initializing Audio Engine...");
        
        try {
            // Initialize audio system
            initializeAudioSystem();
            
            // Initialize components
            midiHandler = new MidiHandler();
            synthesizerManager = new SynthesizerManager();
            trackManager = new TrackManager(this);
            compositionEngine = new CompositionEngine();
            vocalSynthesizer = new VocalSynthesizer();
            effectsProcessor = new EffectsProcessor();
            audioExporter = new AudioExporter();
            
            // Initialize all components
            midiHandler.initialize();
            synthesizerManager.initialize();
            trackManager.initialize();
            compositionEngine.initialize();
            vocalSynthesizer.initialize();
            effectsProcessor.initialize();
            audioExporter.initialize();
            
            isInitialized = true;
            logger.info("Audio Engine initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize Audio Engine", e);
            throw e;
        }
    }
    
    /**
     * Initialize the audio system for playback
     */
    private void initializeAudioSystem() throws Exception {
        // Get default mixer
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        if (mixerInfos.length == 0) {
            throw new Exception("No audio mixers available");
        }
        
        mixer = AudioSystem.getMixer(mixerInfos[0]);
        
        // Setup audio line for playback
        DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
        if (!AudioSystem.isLineSupported(lineInfo)) {
            throw new Exception("Audio format not supported");
        }
        
        audioLine = (SourceDataLine) AudioSystem.getLine(lineInfo);
        audioLine.open(AUDIO_FORMAT, BUFFER_SIZE);
        
        logger.info("Audio system initialized - Sample Rate: {}Hz, Buffer: {} bytes", 
                   SAMPLE_RATE, BUFFER_SIZE);
    }
    
    /**
     * Generate a complete song with AI composition
     */
    public CompletableFuture<SongStructure> generateSong(String genre, String mood, 
                                                         String lyrics, int durationMinutes) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Generating song - Genre: {}, Mood: {}, Duration: {}min", 
                           genre, mood, durationMinutes);
                
                if (!isInitialized) {
                    throw new IllegalStateException("Audio engine not initialized");
                }
                
                // Generate composition
                currentSong = compositionEngine.generateSong(genre, mood, lyrics, durationMinutes);
                
                // Generate tracks
                trackManager.generateTracks(currentSong);
                
                // Generate vocals if lyrics provided
                if (lyrics != null && !lyrics.trim().isEmpty()) {
                    vocalSynthesizer.generateVocals(currentSong, lyrics);
                }
                
                // Mix all tracks
                currentAudioData = mixTracks();
                
                // Apply effects
                currentAudioData = effectsProcessor.processAudio(currentAudioData, currentSong);
                
                logger.info("Song generation completed successfully");
                return currentSong;
                
            } catch (Exception e) {
                logger.error("Failed to generate song", e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Mix all tracks into final audio
     */
    private byte[] mixTracks() {
        logger.info("Mixing tracks...");
        return trackManager.mixAllTracks();
    }
    
    /**
     * Play the currently generated song
     */
    public void playSong() {
        if (currentAudioData == null) {
            logger.warn("No song data to play");
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                isPlaying = true;
                audioLine.start();
                
                int bytesPerFrame = AUDIO_FORMAT.getFrameSize();
                int numBytesToWrite = BUFFER_SIZE - (BUFFER_SIZE % bytesPerFrame);
                
                int offset = 0;
                while (offset < currentAudioData.length && isPlaying) {
                    int bytesToWrite = Math.min(numBytesToWrite, currentAudioData.length - offset);
                    audioLine.write(currentAudioData, offset, bytesToWrite);
                    offset += bytesToWrite;
                }
                
                audioLine.drain();
                audioLine.stop();
                isPlaying = false;
                
            } catch (Exception e) {
                logger.error("Error playing song", e);
                isPlaying = false;
            }
        }, executorService);
    }
    
    /**
     * Stop playback
     */
    public void stopPlayback() {
        isPlaying = false;
        if (audioLine != null && audioLine.isRunning()) {
            audioLine.stop();
            audioLine.flush();
        }
    }
    
    /**
     * Export current song to file
     */
    public CompletableFuture<String> exportSong(String filePath, String format) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (currentAudioData == null) {
                    throw new IllegalStateException("No song data to export");
                }
                
                return audioExporter.exportAudio(currentAudioData, filePath, format, AUDIO_FORMAT);
                
            } catch (Exception e) {
                logger.error("Failed to export song", e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Get current playback position
     */
    public double getPlaybackPosition() {
        if (audioLine == null || currentAudioData == null) {
            return 0.0;
        }
        
        long framePosition = audioLine.getLongFramePosition();
        long totalFrames = currentAudioData.length / AUDIO_FORMAT.getFrameSize();
        
        return totalFrames > 0 ? (double) framePosition / totalFrames : 0.0;
    }
    
    /**
     * Check if currently playing
     */
    public boolean isPlaying() {
        return isPlaying;
    }
    
    /**
     * Get current song structure
     */
    public SongStructure getCurrentSong() {
        return currentSong;
    }
    
    /**
     * Get audio format
     */
    public AudioFormat getAudioFormat() {
        return AUDIO_FORMAT;
    }
    
    /**
     * Get sample rate
     */
    public int getSampleRate() {
        return SAMPLE_RATE;
    }
    
    /**
     * Shutdown the audio engine
     */
    public void shutdown() {
        logger.info("Shutting down Audio Engine...");
        
        stopPlayback();
        
        if (audioLine != null) {
            audioLine.close();
        }
        
        if (midiHandler != null) {
            midiHandler.shutdown();
        }
        
        if (synthesizerManager != null) {
            synthesizerManager.shutdown();
        }
        
        if (executorService != null) {
            executorService.shutdown();
        }
        
        logger.info("Audio Engine shutdown complete");
    }
}

