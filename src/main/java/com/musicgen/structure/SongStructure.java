package com.musicgen.structure;

import com.musicgen.theory.RhythmPattern;

import java.util.List;
import java.util.Map;

/**
 * Represents the complete structure of a generated song
 * Contains all musical elements: harmony, melody, rhythm, arrangement
 */
public class SongStructure {
    
    // Basic song properties
    private String title;
    private String genre;
    private String mood;
    private String lyrics;
    private int durationMinutes;
    
    // Musical parameters
    private String key;
    private String mode;
    private int bpm;
    private int[] timeSignature;
    
    // Song structure
    private List<Section> sections;
    
    // Musical content
    private Map<String, List<String>> chordProgressions; // Section -> Chords
    private Map<String, List<Integer>> melodies; // Section -> MIDI notes
    private Map<String, RhythmPattern.Pattern> rhythmPatterns; // Section -> Rhythm
    
    // Arrangement
    private List<String> instruments;
    private Map<String, List<String>> trackAssignments; // Section -> Active instruments
    
    // Audio data
    private byte[] audioData;
    private long durationMs;
    
    /**
     * Represents a section of the song (verse, chorus, etc.)
     */
    public static class Section {
        private String name;
        private int bars;
        private float intensity; // 0.0 to 1.0
        private int startBar;
        private int endBar;
        
        public Section(String name, int bars, float intensity) {
            this.name = name;
            this.bars = bars;
            this.intensity = intensity;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getBars() { return bars; }
        public void setBars(int bars) { this.bars = bars; }
        
        public float getIntensity() { return intensity; }
        public void setIntensity(float intensity) { this.intensity = intensity; }
        
        public int getStartBar() { return startBar; }
        public void setStartBar(int startBar) { this.startBar = startBar; }
        
        public int getEndBar() { return endBar; }
        public void setEndBar(int endBar) { this.endBar = endBar; }
        
        @Override
        public String toString() {
            return String.format("%s (%d bars, %.1f intensity)", name, bars, intensity);
        }
    }
    
    // Constructors
    public SongStructure() {
        this.timeSignature = new int[]{4, 4}; // Default 4/4 time
    }
    
    public SongStructure(String title, String genre, String mood) {
        this();
        this.title = title;
        this.genre = genre;
        this.mood = mood;
    }
    
    // Basic properties getters/setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    
    public String getLyrics() { return lyrics; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }
    
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    
    // Musical parameters getters/setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    
    public int getBpm() { return bpm; }
    public void setBpm(int bpm) { this.bpm = bpm; }
    
    public int[] getTimeSignature() { return timeSignature; }
    public void setTimeSignature(int[] timeSignature) { this.timeSignature = timeSignature; }
    
    // Structure getters/setters
    public List<Section> getSections() { return sections; }
    public void setSections(List<Section> sections) { 
        this.sections = sections;
        updateSectionPositions();
    }
    
    // Musical content getters/setters
    public Map<String, List<String>> getChordProgressions() { return chordProgressions; }
    public void setChordProgressions(Map<String, List<String>> chordProgressions) { 
        this.chordProgressions = chordProgressions; 
    }
    
    public Map<String, List<Integer>> getMelodies() { return melodies; }
    public void setMelodies(Map<String, List<Integer>> melodies) { this.melodies = melodies; }
    
    public Map<String, RhythmPattern.Pattern> getRhythmPatterns() { return rhythmPatterns; }
    public void setRhythmPatterns(Map<String, RhythmPattern.Pattern> rhythmPatterns) { 
        this.rhythmPatterns = rhythmPatterns; 
    }
    
    // Arrangement getters/setters
    public List<String> getInstruments() { return instruments; }
    public void setInstruments(List<String> instruments) { this.instruments = instruments; }
    
    public Map<String, List<String>> getTrackAssignments() { return trackAssignments; }
    public void setTrackAssignments(Map<String, List<String>> trackAssignments) { 
        this.trackAssignments = trackAssignments; 
    }
    
    // Audio data getters/setters
    public byte[] getAudioData() { return audioData; }
    public void setAudioData(byte[] audioData) { this.audioData = audioData; }
    
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    
    // Utility methods
    
    /**
     * Update section start/end positions
     */
    private void updateSectionPositions() {
        if (sections == null) return;
        
        int currentBar = 0;
        for (Section section : sections) {
            section.setStartBar(currentBar);
            section.setEndBar(currentBar + section.getBars() - 1);
            currentBar += section.getBars();
        }
    }
    
    /**
     * Get total number of bars
     */
    public int getTotalBars() {
        if (sections == null) return 0;
        return sections.stream().mapToInt(Section::getBars).sum();
    }
    
    /**
     * Get section at specific bar
     */
    public Section getSectionAtBar(int bar) {
        if (sections == null) return null;
        
        for (Section section : sections) {
            if (bar >= section.getStartBar() && bar <= section.getEndBar()) {
                return section;
            }
        }
        return null;
    }
    
    /**
     * Get chords for specific section
     */
    public List<String> getChordsForSection(String sectionName) {
        return chordProgressions != null ? chordProgressions.get(sectionName) : null;
    }
    
    /**
     * Get melody for specific section
     */
    public List<Integer> getMelodyForSection(String sectionName) {
        return melodies != null ? melodies.get(sectionName) : null;
    }
    
    /**
     * Get rhythm pattern for specific section
     */
    public RhythmPattern.Pattern getRhythmForSection(String sectionName) {
        return rhythmPatterns != null ? rhythmPatterns.get(sectionName) : null;
    }
    
    /**
     * Get active instruments for specific section
     */
    public List<String> getInstrumentsForSection(String sectionName) {
        return trackAssignments != null ? trackAssignments.get(sectionName) : null;
    }
    
    /**
     * Calculate song duration in milliseconds based on BPM and bars
     */
    public long calculateDurationMs() {
        if (bpm <= 0 || timeSignature == null) return 0;
        
        int totalBars = getTotalBars();
        double beatsPerBar = timeSignature[0];
        double totalBeats = totalBars * beatsPerBar;
        double beatsPerSecond = bpm / 60.0;
        double durationSeconds = totalBeats / beatsPerSecond;
        
        return (long) (durationSeconds * 1000);
    }
    
    /**
     * Get song info summary
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Title: %s\n", title != null ? title : "Untitled"));
        sb.append(String.format("Genre: %s, Mood: %s\n", genre, mood));
        sb.append(String.format("Key: %s %s, BPM: %d, Time: %d/%d\n", 
                                key, mode, bpm, timeSignature[0], timeSignature[1]));
        sb.append(String.format("Duration: %d minutes (%d bars)\n", durationMinutes, getTotalBars()));
        
        if (sections != null) {
            sb.append("Sections:\n");
            for (Section section : sections) {
                sb.append(String.format("  - %s\n", section.toString()));
            }
        }
        
        if (instruments != null) {
            sb.append(String.format("Instruments: %s\n", String.join(", ", instruments)));
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("SongStructure[%s - %s %s, %d bars, %d instruments]", 
                           title != null ? title : "Untitled", 
                           genre, mood, getTotalBars(), 
                           instruments != null ? instruments.size() : 0);
    }
}

