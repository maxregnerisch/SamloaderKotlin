package com.musicgen.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Exports generated audio to various formats (MP3, WAV)
 */
public class AudioExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(AudioExporter.class);
    
    public void initialize() {
        logger.info("Audio Exporter initialized");
    }
    
    public String exportAudio(byte[] audioData, String filePath, String format, AudioFormat audioFormat) throws Exception {
        logger.info("Exporting {} bytes to {} format: {}", audioData.length, format, filePath);
        
        File outputFile = new File(filePath);
        
        if ("wav".equalsIgnoreCase(format)) {
            // Export as WAV
            AudioInputStream audioInputStream = new AudioInputStream(
                new ByteArrayInputStream(audioData), audioFormat, audioData.length / audioFormat.getFrameSize()
            );
            AudioSystem.write(audioInputStream, javax.sound.sampled.AudioFileFormat.Type.WAVE, outputFile);
        } else if ("mp3".equalsIgnoreCase(format)) {
            // For MP3, would need additional encoding library
            logger.warn("MP3 export not fully implemented - saving as WAV");
            String wavPath = filePath.replace(".mp3", ".wav");
            AudioInputStream audioInputStream = new AudioInputStream(
                new ByteArrayInputStream(audioData), audioFormat, audioData.length / audioFormat.getFrameSize()
            );
            AudioSystem.write(audioInputStream, javax.sound.sampled.AudioFileFormat.Type.WAVE, new File(wavPath));
            return wavPath;
        }
        
        logger.info("Export completed: {}", outputFile.getAbsolutePath());
        return outputFile.getAbsolutePath();
    }
}

