package com.musicgen.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Handles MIDI operations including sequence creation, playback, and instrument management
 */
public class MidiHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MidiHandler.class);
    
    private Synthesizer synthesizer;
    private Sequencer sequencer;
    private Sequence currentSequence;
    private ConcurrentMap<Integer, MidiChannel> channels;
    private boolean isInitialized = false;
    
    // Standard MIDI instruments
    public static final int ACOUSTIC_GRAND_PIANO = 0;
    public static final int ELECTRIC_GUITAR_CLEAN = 27;
    public static final int ELECTRIC_BASS_FINGER = 33;
    public static final int DRUM_KIT = 128; // Channel 9 (percussion)
    public static final int SYNTH_LEAD = 80;
    public static final int SYNTH_PAD = 88;
    public static final int CHOIR_AAHS = 52;
    
    public MidiHandler() {
        this.channels = new ConcurrentHashMap<>();
    }
    
    /**
     * Initialize MIDI system
     */
    public void initialize() throws Exception {
        logger.info("Initializing MIDI Handler...");
        
        try {
            // Get default synthesizer
            synthesizer = MidiSystem.getSynthesizer();
            if (synthesizer == null) {
                throw new Exception("No MIDI synthesizer available");
            }
            
            synthesizer.open();
            
            // Get default sequencer
            sequencer = MidiSystem.getSequencer();
            if (sequencer == null) {
                throw new Exception("No MIDI sequencer available");
            }
            
            sequencer.open();
            
            // Connect sequencer to synthesizer
            Transmitter transmitter = sequencer.getTransmitter();
            Receiver receiver = synthesizer.getReceiver();
            transmitter.setReceiver(receiver);
            
            // Initialize channels
            MidiChannel[] midiChannels = synthesizer.getChannels();
            for (int i = 0; i < midiChannels.length && i < 16; i++) {
                if (midiChannels[i] != null) {
                    channels.put(i, midiChannels[i]);
                }
            }
            
            isInitialized = true;
            logger.info("MIDI Handler initialized - Channels: {}, Instruments: {}", 
                       channels.size(), synthesizer.getDefaultSoundbank().getInstruments().length);
            
        } catch (Exception e) {
            logger.error("Failed to initialize MIDI Handler", e);
            throw e;
        }
    }
    
    /**
     * Create a new MIDI sequence
     */
    public Sequence createSequence(float divisionType, int resolution) throws InvalidMidiDataException {
        currentSequence = new Sequence(divisionType, resolution);
        logger.debug("Created new MIDI sequence - Division: {}, Resolution: {}", divisionType, resolution);
        return currentSequence;
    }
    
    /**
     * Add a track to the current sequence
     */
    public Track addTrack() {
        if (currentSequence == null) {
            throw new IllegalStateException("No sequence created");
        }
        
        Track track = currentSequence.createTrack();
        logger.debug("Added new track - Total tracks: {}", currentSequence.getTracks().length);
        return track;
    }
    
    /**
     * Add a note to a track
     */
    public void addNote(Track track, int channel, int note, int velocity, 
                       long startTick, long duration) throws InvalidMidiDataException {
        
        // Note on event
        ShortMessage noteOn = new ShortMessage();
        noteOn.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
        MidiEvent noteOnEvent = new MidiEvent(noteOn, startTick);
        track.add(noteOnEvent);
        
        // Note off event
        ShortMessage noteOff = new ShortMessage();
        noteOff.setMessage(ShortMessage.NOTE_OFF, channel, note, 0);
        MidiEvent noteOffEvent = new MidiEvent(noteOff, startTick + duration);
        track.add(noteOffEvent);
        
        logger.trace("Added note - Channel: {}, Note: {}, Velocity: {}, Start: {}, Duration: {}", 
                    channel, note, velocity, startTick, duration);
    }
    
    /**
     * Add a chord to a track
     */
    public void addChord(Track track, int channel, int[] notes, int velocity, 
                        long startTick, long duration) throws InvalidMidiDataException {
        
        for (int note : notes) {
            addNote(track, channel, note, velocity, startTick, duration);
        }
        
        logger.debug("Added chord - Channel: {}, Notes: {}, Velocity: {}", 
                    channel, notes.length, velocity);
    }
    
    /**
     * Set instrument for a channel
     */
    public void setInstrument(int channel, int instrument) throws InvalidMidiDataException {
        MidiChannel midiChannel = channels.get(channel);
        if (midiChannel != null) {
            midiChannel.programChange(instrument);
            logger.debug("Set instrument {} on channel {}", instrument, channel);
        }
    }
    
    /**
     * Add program change event to track
     */
    public void addProgramChange(Track track, int channel, int instrument, long tick) 
            throws InvalidMidiDataException {
        
        ShortMessage programChange = new ShortMessage();
        programChange.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
        MidiEvent event = new MidiEvent(programChange, tick);
        track.add(event);
        
        logger.debug("Added program change - Channel: {}, Instrument: {}, Tick: {}", 
                    channel, instrument, tick);
    }
    
    /**
     * Add tempo change event
     */
    public void addTempoChange(Track track, int bpm, long tick) throws InvalidMidiDataException {
        // Calculate microseconds per quarter note
        int microsecondsPerQuarterNote = 60000000 / bpm;
        
        byte[] tempoData = new byte[3];
        tempoData[0] = (byte) ((microsecondsPerQuarterNote >> 16) & 0xFF);
        tempoData[1] = (byte) ((microsecondsPerQuarterNote >> 8) & 0xFF);
        tempoData[2] = (byte) (microsecondsPerQuarterNote & 0xFF);
        
        MetaMessage tempoMessage = new MetaMessage();
        tempoMessage.setMessage(0x51, tempoData, 3);
        MidiEvent tempoEvent = new MidiEvent(tempoMessage, tick);
        track.add(tempoEvent);
        
        logger.debug("Added tempo change - BPM: {}, Tick: {}", bpm, tick);
    }
    
    /**
     * Add time signature event
     */
    public void addTimeSignature(Track track, int numerator, int denominator, long tick) 
            throws InvalidMidiDataException {
        
        byte[] timeSignatureData = new byte[4];
        timeSignatureData[0] = (byte) numerator;
        timeSignatureData[1] = (byte) (Math.log(denominator) / Math.log(2)); // Power of 2
        timeSignatureData[2] = 24; // MIDI clocks per metronome click
        timeSignatureData[3] = 8;  // 32nd notes per quarter note
        
        MetaMessage timeSignatureMessage = new MetaMessage();
        timeSignatureMessage.setMessage(0x58, timeSignatureData, 4);
        MidiEvent timeSignatureEvent = new MidiEvent(timeSignatureMessage, tick);
        track.add(timeSignatureEvent);
        
        logger.debug("Added time signature - {}/{}, Tick: {}", numerator, denominator, tick);
    }
    
    /**
     * Play the current sequence
     */
    public void playSequence() throws Exception {
        if (currentSequence == null) {
            throw new IllegalStateException("No sequence to play");
        }
        
        sequencer.setSequence(currentSequence);
        sequencer.start();
        logger.info("Started MIDI playback");
    }
    
    /**
     * Stop playback
     */
    public void stopPlayback() {
        if (sequencer != null && sequencer.isRunning()) {
            sequencer.stop();
            logger.info("Stopped MIDI playback");
        }
    }
    
    /**
     * Get current playback position
     */
    public long getPlaybackPosition() {
        return sequencer != null ? sequencer.getTickPosition() : 0;
    }
    
    /**
     * Check if playing
     */
    public boolean isPlaying() {
        return sequencer != null && sequencer.isRunning();
    }
    
    /**
     * Get available instruments
     */
    public Instrument[] getAvailableInstruments() {
        if (synthesizer != null && synthesizer.getDefaultSoundbank() != null) {
            return synthesizer.getDefaultSoundbank().getInstruments();
        }
        return new Instrument[0];
    }
    
    /**
     * Get MIDI channel
     */
    public MidiChannel getChannel(int channel) {
        return channels.get(channel);
    }
    
    /**
     * Convert note name to MIDI number
     */
    public static int noteNameToMidi(String noteName) {
        // Parse note name like "C4", "F#3", "Bb5"
        String note = noteName.substring(0, noteName.length() - 1);
        int octave = Integer.parseInt(noteName.substring(noteName.length() - 1));
        
        int noteValue;
        switch (note.toUpperCase()) {
            case "C": noteValue = 0; break;
            case "C#": case "DB": noteValue = 1; break;
            case "D": noteValue = 2; break;
            case "D#": case "EB": noteValue = 3; break;
            case "E": noteValue = 4; break;
            case "F": noteValue = 5; break;
            case "F#": case "GB": noteValue = 6; break;
            case "G": noteValue = 7; break;
            case "G#": case "AB": noteValue = 8; break;
            case "A": noteValue = 9; break;
            case "A#": case "BB": noteValue = 10; break;
            case "B": noteValue = 11; break;
            default: throw new IllegalArgumentException("Invalid note name: " + note);
        }
        
        return (octave + 1) * 12 + noteValue;
    }
    
    /**
     * Convert MIDI number to note name
     */
    public static String midiToNoteName(int midiNumber) {
        String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        int octave = (midiNumber / 12) - 1;
        int note = midiNumber % 12;
        return noteNames[note] + octave;
    }
    
    /**
     * Shutdown MIDI handler
     */
    public void shutdown() {
        logger.info("Shutting down MIDI Handler...");
        
        stopPlayback();
        
        if (sequencer != null) {
            sequencer.close();
        }
        
        if (synthesizer != null) {
            synthesizer.close();
        }
        
        channels.clear();
        isInitialized = false;
        
        logger.info("MIDI Handler shutdown complete");
    }
}

