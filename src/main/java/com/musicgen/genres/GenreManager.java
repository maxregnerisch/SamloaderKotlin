package com.musicgen.genres;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages genre-specific musical characteristics and instrument selections
 */
public class GenreManager {
    
    private static final Logger logger = LoggerFactory.getLogger(GenreManager.class);
    
    private Map<String, Set<String>> genreInstruments;
    
    public void initialize() {
        logger.info("Initializing Genre Manager...");
        
        genreInstruments = new HashMap<>();
        
        // Pop instruments
        genreInstruments.put("pop", Set.of("drums", "bass", "guitar", "piano", "synth", "vocals"));
        
        // Rock instruments
        genreInstruments.put("rock", Set.of("drums", "bass", "guitar", "vocals"));
        
        // Electronic instruments
        genreInstruments.put("electronic", Set.of("drums", "bass", "synth", "pad", "lead"));
        
        // Jazz instruments
        genreInstruments.put("jazz", Set.of("drums", "bass", "piano", "saxophone", "trumpet"));
        
        // Classical instruments
        genreInstruments.put("classical", Set.of("strings", "piano", "woodwinds", "brass"));
        
        logger.info("Genre Manager initialized with {} genres", genreInstruments.size());
    }
    
    /**
     * Get instruments for a genre
     */
    public Set<String> getInstrumentsForGenre(String genre) {
        return genreInstruments.getOrDefault(genre.toLowerCase(), 
                                           Set.of("drums", "bass", "guitar", "piano"));
    }
}

