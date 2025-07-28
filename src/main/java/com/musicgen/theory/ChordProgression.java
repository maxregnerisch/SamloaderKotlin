package com.musicgen.theory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates chord progressions based on music theory and genre conventions
 */
public class ChordProgression {
    
    private static final Logger logger = LoggerFactory.getLogger(ChordProgression.class);
    
    private Random random;
    private Map<String, String[]> scaleChords;
    private Map<String, List<int[]>> commonProgressions;
    
    // Chord intervals for different modes
    private static final Map<String, int[]> MODE_INTERVALS = Map.of(
        "Major", new int[]{0, 2, 4, 5, 7, 9, 11},
        "Minor", new int[]{0, 2, 3, 5, 7, 8, 10},
        "Dorian", new int[]{0, 2, 3, 5, 7, 9, 10},
        "Mixolydian", new int[]{0, 2, 4, 5, 7, 9, 10},
        "Pentatonic", new int[]{0, 2, 4, 7, 9}
    );
    
    // Note names
    private static final String[] NOTES = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    
    public ChordProgression() {
        this.random = ThreadLocalRandom.current();
        this.scaleChords = new HashMap<>();
        this.commonProgressions = new HashMap<>();
    }
    
    /**
     * Initialize chord progression generator
     */
    public void initialize() {
        logger.info("Initializing Chord Progression generator...");
        
        initializeCommonProgressions();
        logger.info("Chord Progression generator initialized");
    }
    
    /**
     * Initialize common chord progressions for different genres
     */
    private void initializeCommonProgressions() {
        // Pop progressions (using scale degrees)
        commonProgressions.put("pop", Arrays.asList(
            new int[]{1, 5, 6, 4}, // I-V-vi-IV (very common)
            new int[]{6, 4, 1, 5}, // vi-IV-I-V
            new int[]{1, 6, 4, 5}, // I-vi-IV-V
            new int[]{4, 1, 5, 6}  // IV-I-V-vi
        ));
        
        // Rock progressions
        commonProgressions.put("rock", Arrays.asList(
            new int[]{1, 7, 4, 1}, // I-bVII-IV-I
            new int[]{1, 4, 5, 1}, // I-IV-V-I (classic)
            new int[]{6, 4, 1, 5}, // vi-IV-I-V
            new int[]{1, 3, 4, 1}  // I-iii-IV-I
        ));
        
        // Jazz progressions
        commonProgressions.put("jazz", Arrays.asList(
            new int[]{2, 5, 1, 6}, // ii-V-I-vi
            new int[]{1, 6, 2, 5}, // I-vi-ii-V
            new int[]{3, 6, 2, 5}, // iii-vi-ii-V
            new int[]{1, 4, 7, 3}  // I-IV-bVII-iii
        ));
        
        // Electronic progressions
        commonProgressions.put("electronic", Arrays.asList(
            new int[]{6, 1, 4, 5}, // vi-I-IV-V
            new int[]{1, 5, 6, 4}, // I-V-vi-IV
            new int[]{4, 5, 6, 1}, // IV-V-vi-I
            new int[]{6, 7, 1, 2}  // vi-bVII-I-ii
        ));
        
        // Folk/Country progressions
        commonProgressions.put("folk", Arrays.asList(
            new int[]{1, 4, 5, 1}, // I-IV-V-I
            new int[]{1, 5, 6, 4}, // I-V-vi-IV
            new int[]{6, 4, 1, 5}, // vi-IV-I-V
            new int[]{1, 6, 4, 5}  // I-vi-IV-V
        ));
        
        // Classical progressions
        commonProgressions.put("classical", Arrays.asList(
            new int[]{1, 4, 5, 1}, // I-IV-V-I
            new int[]{1, 6, 4, 5}, // I-vi-IV-V
            new int[]{2, 5, 1, 1}, // ii-V-I-I
            new int[]{1, 2, 5, 1}  // I-ii-V-I
        ));
    }
    
    /**
     * Generate chord progression for a section
     */
    public List<String> generateProgression(String key, String mode, String sectionName, int bars) {
        logger.debug("Generating progression - Key: {} {}, Section: {}, Bars: {}", 
                    key, mode, sectionName, bars);
        
        // Get scale chords for the key and mode
        String[] chords = getScaleChords(key, mode);
        
        // Determine progression length (typically 4 or 8 chords)
        int progressionLength = bars <= 8 ? 4 : 8;
        
        // Select base progression pattern
        List<Integer> pattern = selectProgressionPattern(sectionName, progressionLength);
        
        // Convert to actual chords
        List<String> progression = new ArrayList<>();
        for (int degree : pattern) {
            if (degree > 0 && degree <= chords.length) {
                progression.add(chords[degree - 1]);
            }
        }
        
        // Extend progression to fill bars if needed
        while (progression.size() < bars) {
            progression.addAll(progression.subList(0, Math.min(progressionLength, bars - progression.size())));
        }
        
        // Trim to exact bar count
        if (progression.size() > bars) {
            progression = progression.subList(0, bars);
        }
        
        logger.debug("Generated progression: {}", progression);
        return progression;
    }
    
    /**
     * Get chords for a scale
     */
    private String[] getScaleChords(String key, String mode) {
        String cacheKey = key + "_" + mode;
        
        if (scaleChords.containsKey(cacheKey)) {
            return scaleChords.get(cacheKey);
        }
        
        int[] intervals = MODE_INTERVALS.get(mode);
        if (intervals == null) {
            intervals = MODE_INTERVALS.get("Major"); // Default to major
        }
        
        int rootIndex = Arrays.asList(NOTES).indexOf(key);
        if (rootIndex == -1) {
            rootIndex = 0; // Default to C
        }
        
        String[] chords = new String[7];
        
        for (int i = 0; i < 7; i++) {
            // Build triad for each scale degree
            int root = (rootIndex + intervals[i % intervals.length]) % 12;
            int third = (rootIndex + intervals[(i + 2) % intervals.length]) % 12;
            int fifth = (rootIndex + intervals[(i + 4) % intervals.length]) % 12;
            
            // Determine chord quality
            String chordName = NOTES[root];
            int thirdInterval = (third - root + 12) % 12;
            int fifthInterval = (fifth - root + 12) % 12;
            
            if (thirdInterval == 3) { // Minor third
                chordName += "m";
            } else if (thirdInterval == 4) { // Major third
                // Major chord (no suffix)
            }
            
            if (fifthInterval == 6) { // Diminished fifth
                chordName += "dim";
            } else if (fifthInterval == 8) { // Augmented fifth
                chordName += "aug";
            }
            
            chords[i] = chordName;
        }
        
        scaleChords.put(cacheKey, chords);
        return chords;
    }
    
    /**
     * Select progression pattern based on section
     */
    private List<Integer> selectProgressionPattern(String sectionName, int length) {
        List<Integer> pattern;
        
        switch (sectionName.toLowerCase()) {
            case "intro":
                // Simple, stable progressions
                pattern = Arrays.asList(1, 4, 1, 5);
                break;
                
            case "verse":
                // Storytelling progressions
                pattern = Arrays.asList(6, 4, 1, 5);
                break;
                
            case "chorus":
                // Strong, memorable progressions
                pattern = Arrays.asList(1, 5, 6, 4);
                break;
                
            case "bridge":
                // Contrasting progressions
                pattern = Arrays.asList(4, 1, 2, 5);
                break;
                
            case "outro":
                // Resolving progressions
                pattern = Arrays.asList(4, 5, 1, 1);
                break;
                
            default:
                // Default progression
                pattern = Arrays.asList(1, 4, 5, 1);
                break;
        }
        
        // Extend or modify pattern for different lengths
        if (length == 8) {
            List<Integer> extended = new ArrayList<>(pattern);
            extended.addAll(pattern);
            return extended;
        } else if (length != 4) {
            // Adjust pattern length
            List<Integer> adjusted = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                adjusted.add(pattern.get(i % pattern.size()));
            }
            return adjusted;
        }
        
        return pattern;
    }
    
    /**
     * Generate chord progression with variations
     */
    public List<String> generateProgressionWithVariations(String key, String mode, 
                                                         String genre, int bars) {
        List<String> baseProgression = generateProgression(key, mode, "verse", bars);
        
        // Apply genre-specific variations
        return applyGenreVariations(baseProgression, genre);
    }
    
    /**
     * Apply genre-specific chord variations
     */
    private List<String> applyGenreVariations(List<String> progression, String genre) {
        List<String> varied = new ArrayList<>(progression);
        
        switch (genre.toLowerCase()) {
            case "jazz":
                // Add extensions and alterations
                for (int i = 0; i < varied.size(); i++) {
                    if (random.nextFloat() < 0.3f) {
                        varied.set(i, addJazzExtension(varied.get(i)));
                    }
                }
                break;
                
            case "rock":
                // Add power chords and suspensions
                for (int i = 0; i < varied.size(); i++) {
                    if (random.nextFloat() < 0.2f) {
                        varied.set(i, addRockVariation(varied.get(i)));
                    }
                }
                break;
                
            case "electronic":
                // Add suspended and add chords
                for (int i = 0; i < varied.size(); i++) {
                    if (random.nextFloat() < 0.25f) {
                        varied.set(i, addElectronicVariation(varied.get(i)));
                    }
                }
                break;
        }
        
        return varied;
    }
    
    /**
     * Add jazz extensions to chord
     */
    private String addJazzExtension(String chord) {
        String[] extensions = {"7", "maj7", "9", "11", "13"};
        return chord + extensions[random.nextInt(extensions.length)];
    }
    
    /**
     * Add rock variations to chord
     */
    private String addRockVariation(String chord) {
        String[] variations = {"5", "sus2", "sus4", "add9"};
        return chord + variations[random.nextInt(variations.length)];
    }
    
    /**
     * Add electronic variations to chord
     */
    private String addElectronicVariation(String chord) {
        String[] variations = {"sus2", "sus4", "add9", "6"};
        return chord + variations[random.nextInt(variations.length)];
    }
    
    /**
     * Get chord notes for MIDI generation
     */
    public int[] getChordNotes(String chordName, int octave) {
        // Parse chord name and return MIDI note numbers
        String root = chordName.replaceAll("[^A-G#b]", "");
        int rootNote = noteNameToMidi(root, octave);
        
        List<Integer> notes = new ArrayList<>();
        notes.add(rootNote);
        
        // Add third
        if (chordName.contains("m") && !chordName.contains("maj")) {
            notes.add(rootNote + 3); // Minor third
        } else {
            notes.add(rootNote + 4); // Major third
        }
        
        // Add fifth
        if (chordName.contains("dim")) {
            notes.add(rootNote + 6); // Diminished fifth
        } else if (chordName.contains("aug")) {
            notes.add(rootNote + 8); // Augmented fifth
        } else {
            notes.add(rootNote + 7); // Perfect fifth
        }
        
        // Add extensions
        if (chordName.contains("7")) {
            if (chordName.contains("maj7")) {
                notes.add(rootNote + 11); // Major seventh
            } else {
                notes.add(rootNote + 10); // Minor seventh
            }
        }
        
        if (chordName.contains("9")) {
            notes.add(rootNote + 14); // Ninth
        }
        
        return notes.stream().mapToInt(Integer::intValue).toArray();
    }
    
    /**
     * Convert note name to MIDI number
     */
    private int noteNameToMidi(String noteName, int octave) {
        int noteValue = Arrays.asList(NOTES).indexOf(noteName.toUpperCase());
        if (noteValue == -1) noteValue = 0; // Default to C
        
        return (octave + 1) * 12 + noteValue;
    }
}
