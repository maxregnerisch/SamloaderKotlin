package com.musicgen.composition;

import com.musicgen.genres.GenreManager;
import com.musicgen.structure.SongStructure;
import com.musicgen.theory.ChordProgression;
import com.musicgen.theory.MelodyGenerator;
import com.musicgen.theory.RhythmPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * AI-powered composition engine that generates complete songs with realistic musical structure
 * Uses music theory algorithms and genre-specific patterns
 */
public class CompositionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(CompositionEngine.class);
    
    private GenreManager genreManager;
    private ChordProgression chordProgression;
    private MelodyGenerator melodyGenerator;
    private RhythmPattern rhythmPattern;
    private Random random;
    
    // Musical constants
    private static final String[] KEYS = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    
    private static final String[] MODES = {
        "Major", "Minor", "Dorian", "Mixolydian", "Pentatonic"
    };
    
    public CompositionEngine() {
        this.random = ThreadLocalRandom.current();
    }
    
    /**
     * Initialize the composition engine
     */
    public void initialize() throws Exception {
        logger.info("Initializing Composition Engine...");
        
        try {
            genreManager = new GenreManager();
            chordProgression = new ChordProgression();
            melodyGenerator = new MelodyGenerator();
            rhythmPattern = new RhythmPattern();
            
            genreManager.initialize();
            chordProgression.initialize();
            melodyGenerator.initialize();
            rhythmPattern.initialize();
            
            logger.info("Composition Engine initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize Composition Engine", e);
            throw e;
        }
    }
    
    /**
     * Generate a complete song structure
     */
    public SongStructure generateSong(String genre, String mood, String lyrics, int durationMinutes) {
        logger.info("Generating song - Genre: {}, Mood: {}, Duration: {}min", genre, mood, durationMinutes);
        
        try {
            // Create song structure
            SongStructure song = new SongStructure();
            
            // Set basic properties
            song.setGenre(genre);
            song.setMood(mood);
            song.setLyrics(lyrics);
            song.setDurationMinutes(durationMinutes);
            
            // Generate musical parameters
            generateMusicalParameters(song, genre, mood);
            
            // Generate song sections
            generateSongSections(song, durationMinutes);
            
            // Generate harmonic structure
            generateHarmony(song);
            
            // Generate melodic content
            generateMelody(song);
            
            // Generate rhythmic patterns
            generateRhythm(song);
            
            // Generate arrangement
            generateArrangement(song);
            
            logger.info("Song generation completed - Key: {}, BPM: {}, Sections: {}", 
                       song.getKey(), song.getBpm(), song.getSections().size());
            
            return song;
            
        } catch (Exception e) {
            logger.error("Failed to generate song", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Generate basic musical parameters
     */
    private void generateMusicalParameters(SongStructure song, String genre, String mood) {
        // Select key based on mood
        String key = selectKeyForMood(mood);
        String mode = selectModeForGenre(genre, mood);
        
        song.setKey(key);
        song.setMode(mode);
        
        // Generate tempo based on genre and mood
        int bpm = generateBPM(genre, mood);
        song.setBpm(bpm);
        
        // Set time signature
        int[] timeSignature = selectTimeSignature(genre);
        song.setTimeSignature(timeSignature);
        
        logger.debug("Generated parameters - Key: {} {}, BPM: {}, Time: {}/{}", 
                    key, mode, bpm, timeSignature[0], timeSignature[1]);
    }
    
    /**
     * Select key based on mood
     */
    private String selectKeyForMood(String mood) {
        Map<String, String[]> moodKeys = Map.of(
            "happy", new String[]{"C", "G", "D", "A", "E"},
            "sad", new String[]{"A", "E", "B", "F#", "C#"},
            "energetic", new String[]{"E", "B", "F#", "C#", "G#"},
            "calm", new String[]{"F", "Bb", "Eb", "Ab", "Db"},
            "mysterious", new String[]{"F#", "C#", "G#", "D#", "A#"},
            "romantic", new String[]{"F", "C", "G", "D", "A"}
        );
        
        String[] keys = moodKeys.getOrDefault(mood.toLowerCase(), KEYS);
        return keys[random.nextInt(keys.length)];
    }
    
    /**
     * Select mode based on genre and mood
     */
    private String selectModeForGenre(String genre, String mood) {
        if (mood.toLowerCase().contains("sad") || mood.toLowerCase().contains("dark")) {
            return "Minor";
        } else if (genre.toLowerCase().contains("jazz")) {
            return random.nextBoolean() ? "Dorian" : "Mixolydian";
        } else if (genre.toLowerCase().contains("folk") || genre.toLowerCase().contains("country")) {
            return "Pentatonic";
        } else {
            return "Major";
        }
    }
    
    /**
     * Generate BPM based on genre and mood
     */
    private int generateBPM(String genre, String mood) {
        Map<String, int[]> genreBPM = Map.of(
            "pop", new int[]{120, 140},
            "rock", new int[]{110, 160},
            "electronic", new int[]{128, 140},
            "jazz", new int[]{90, 120},
            "classical", new int[]{60, 120},
            "hip-hop", new int[]{70, 100},
            "country", new int[]{100, 130},
            "folk", new int[]{80, 110}
        );
        
        int[] range = genreBPM.getOrDefault(genre.toLowerCase(), new int[]{100, 130});
        int baseBPM = range[0] + random.nextInt(range[1] - range[0]);
        
        // Adjust for mood
        if (mood.toLowerCase().contains("energetic") || mood.toLowerCase().contains("fast")) {
            baseBPM += 10 + random.nextInt(20);
        } else if (mood.toLowerCase().contains("slow") || mood.toLowerCase().contains("calm")) {
            baseBPM -= 10 + random.nextInt(20);
        }
        
        return Math.max(60, Math.min(200, baseBPM));
    }
    
    /**
     * Select time signature based on genre
     */
    private int[] selectTimeSignature(String genre) {
        switch (genre.toLowerCase()) {
            case "waltz":
                return new int[]{3, 4};
            case "jazz":
                return random.nextBoolean() ? new int[]{4, 4} : new int[]{3, 4};
            case "progressive":
                int[][] complex = {new int[]{7, 8}, new int[]{5, 4}, new int[]{6, 8}};
                return complex[random.nextInt(complex.length)];
            default:
                return new int[]{4, 4};
        }
    }
    
    /**
     * Generate song sections (intro, verse, chorus, etc.)
     */
    private void generateSongSections(SongStructure song, int durationMinutes) {
        List<SongStructure.Section> sections = new ArrayList<>();
        
        int totalBars = calculateTotalBars(song.getBpm(), durationMinutes, song.getTimeSignature());
        int remainingBars = totalBars;
        
        // Intro (4-8 bars)
        int introBars = 4 + random.nextInt(5);
        sections.add(new SongStructure.Section("Intro", introBars, 0.6f)); // Quieter
        remainingBars -= introBars;
        
        // Main song structure
        while (remainingBars > 16) {
            // Verse (16-24 bars)
            int verseBars = Math.min(16 + random.nextInt(9), remainingBars / 3);
            sections.add(new SongStructure.Section("Verse", verseBars, 0.8f));
            remainingBars -= verseBars;
            
            if (remainingBars <= 16) break;
            
            // Chorus (12-16 bars)
            int chorusBars = Math.min(12 + random.nextInt(5), remainingBars / 2);
            sections.add(new SongStructure.Section("Chorus", chorusBars, 1.0f)); // Full volume
            remainingBars -= chorusBars;
            
            // Bridge occasionally
            if (sections.size() >= 4 && random.nextFloat() < 0.3f && remainingBars > 20) {
                int bridgeBars = Math.min(8 + random.nextInt(9), remainingBars / 3);
                sections.add(new SongStructure.Section("Bridge", bridgeBars, 0.9f));
                remainingBars -= bridgeBars;
            }
        }
        
        // Outro
        if (remainingBars > 0) {
            sections.add(new SongStructure.Section("Outro", remainingBars, 0.7f));
        }
        
        song.setSections(sections);
        logger.debug("Generated {} sections totaling {} bars", sections.size(), totalBars);
    }
    
    /**
     * Calculate total bars for given duration
     */
    private int calculateTotalBars(int bpm, int durationMinutes, int[] timeSignature) {
        double beatsPerMinute = bpm;
        double totalBeats = beatsPerMinute * durationMinutes;
        double beatsPerBar = timeSignature[0];
        return (int) Math.ceil(totalBeats / beatsPerBar);
    }
    
    /**
     * Generate harmonic structure (chord progressions)
     */
    private void generateHarmony(SongStructure song) {
        logger.debug("Generating harmony for {} sections", song.getSections().size());
        
        Map<String, List<String>> sectionChords = new HashMap<>();
        
        for (SongStructure.Section section : song.getSections()) {
            List<String> chords = chordProgression.generateProgression(
                song.getKey(), song.getMode(), section.getName(), section.getBars()
            );
            sectionChords.put(section.getName(), chords);
        }
        
        song.setChordProgressions(sectionChords);
    }
    
    /**
     * Generate melodic content
     */
    private void generateMelody(SongStructure song) {
        logger.debug("Generating melody");
        
        Map<String, List<Integer>> melodies = new HashMap<>();
        
        for (SongStructure.Section section : song.getSections()) {
            List<String> chords = song.getChordProgressions().get(section.getName());
            List<Integer> melody = melodyGenerator.generateMelody(
                song.getKey(), song.getMode(), chords, section.getBars()
            );
            melodies.put(section.getName(), melody);
        }
        
        song.setMelodies(melodies);
    }
    
    /**
     * Generate rhythmic patterns
     */
    private void generateRhythm(SongStructure song) {
        logger.debug("Generating rhythm patterns");
        
        Map<String, RhythmPattern.Pattern> rhythms = new HashMap<>();
        
        for (SongStructure.Section section : song.getSections()) {
            RhythmPattern.Pattern pattern = rhythmPattern.generatePattern(
                song.getGenre(), section.getName(), song.getTimeSignature()
            );
            rhythms.put(section.getName(), pattern);
        }
        
        song.setRhythmPatterns(rhythms);
    }
    
    /**
     * Generate arrangement (instrumentation)
     */
    private void generateArrangement(SongStructure song) {
        logger.debug("Generating arrangement");
        
        Set<String> instruments = genreManager.getInstrumentsForGenre(song.getGenre());
        song.setInstruments(new ArrayList<>(instruments));
        
        // Generate track assignments
        Map<String, List<String>> trackAssignments = new HashMap<>();
        
        for (SongStructure.Section section : song.getSections()) {
            List<String> activeInstruments = selectInstrumentsForSection(
                instruments, section.getName(), section.getIntensity()
            );
            trackAssignments.put(section.getName(), activeInstruments);
        }
        
        song.setTrackAssignments(trackAssignments);
    }
    
    /**
     * Select instruments for a specific section
     */
    private List<String> selectInstrumentsForSection(Set<String> availableInstruments, 
                                                    String sectionName, float intensity) {
        List<String> selected = new ArrayList<>();
        
        // Always include rhythm section
        if (availableInstruments.contains("drums")) selected.add("drums");
        if (availableInstruments.contains("bass")) selected.add("bass");
        
        // Add instruments based on section and intensity
        switch (sectionName.toLowerCase()) {
            case "intro":
                // Minimal instrumentation
                if (availableInstruments.contains("piano")) selected.add("piano");
                break;
                
            case "verse":
                // Medium instrumentation
                if (availableInstruments.contains("guitar")) selected.add("guitar");
                if (availableInstruments.contains("piano")) selected.add("piano");
                if (intensity > 0.7f && availableInstruments.contains("strings")) {
                    selected.add("strings");
                }
                break;
                
            case "chorus":
                // Full instrumentation
                selected.addAll(availableInstruments);
                break;
                
            case "bridge":
                // Varied instrumentation
                List<String> available = new ArrayList<>(availableInstruments);
                Collections.shuffle(available, random);
                selected.addAll(available.subList(0, Math.min(4, available.size())));
                break;
                
            case "outro":
                // Gradual reduction
                if (availableInstruments.contains("piano")) selected.add("piano");
                if (availableInstruments.contains("strings")) selected.add("strings");
                break;
        }
        
        return selected;
    }
}
