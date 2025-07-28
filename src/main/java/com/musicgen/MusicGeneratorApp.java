package com.musicgen;

import com.musicgen.audio.AudioEngine;
import com.musicgen.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for the AI Music Generator
 * Features: AI composition, vocal synthesis, multi-track arrangement, MP3 export
 */
public class MusicGeneratorApp extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(MusicGeneratorApp.class);
    private AudioEngine audioEngine;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting AI Music Generator...");
            
            // Initialize audio engine
            audioEngine = new AudioEngine();
            audioEngine.initialize();
            
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            
            // Get controller and inject dependencies
            MainController controller = loader.getController();
            controller.setAudioEngine(audioEngine);
            
            // Setup stage
            primaryStage.setTitle("AI Music Generator - Professional Music Creation Suite");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            
            // Add application icon
            try {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
            } catch (Exception e) {
                logger.warn("Could not load application icon: {}", e.getMessage());
            }
            
            // Handle close event
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Shutting down AI Music Generator...");
                if (audioEngine != null) {
                    audioEngine.shutdown();
                }
            });
            
            primaryStage.show();
            logger.info("AI Music Generator started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        // Set system properties for better audio performance
        System.setProperty("javax.sound.sampled.Clip", "com.sun.media.sound.DirectAudioDeviceProvider");
        System.setProperty("javax.sound.sampled.Port", "com.sun.media.sound.PortMixerProvider");
        System.setProperty("javax.sound.sampled.SourceDataLine", "com.sun.media.sound.DirectAudioDeviceProvider");
        System.setProperty("javax.sound.sampled.TargetDataLine", "com.sun.media.sound.DirectAudioDeviceProvider");
        
        logger.info("Launching AI Music Generator with args: {}", String.join(" ", args));
        launch(args);
    }
}

