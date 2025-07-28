package com.musicgen.ui;

import com.musicgen.audio.AudioEngine;
import com.musicgen.structure.SongStructure;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Main UI controller for the AI Music Generator
 */
public class MainController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    // FXML Controls
    @FXML private ComboBox<String> genreComboBox;
    @FXML private ComboBox<String> moodComboBox;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private TextArea lyricsTextArea;
    @FXML private Canvas waveformCanvas;
    @FXML private Button generateButton;
    @FXML private Button playButton;
    @FXML private Button stopButton;
    @FXML private Button exportMp3Button;
    @FXML private Button exportWavButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label timeLabel;
    @FXML private TextArea songInfoTextArea;
    @FXML private VBox mixerPanel;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;
    
    // Dependencies
    private AudioEngine audioEngine;
    
    // State
    private SongStructure currentSong;
    private boolean isGenerating = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing Main Controller...");
        
        setupControls();
        setupWaveformCanvas();
        updateUIState();
        
        logger.info("Main Controller initialized");
    }
    
    /**
     * Setup UI controls with initial values
     */
    private void setupControls() {
        // Genre options
        genreComboBox.getItems().addAll(
            "Pop", "Rock", "Electronic", "Jazz", "Classical", 
            "Hip-Hop", "Country", "Folk", "Ambient", "Experimental"
        );
        genreComboBox.setValue("Pop");
        
        // Mood options
        moodComboBox.getItems().addAll(
            "Happy", "Sad", "Energetic", "Calm", "Mysterious", 
            "Romantic", "Dark", "Uplifting", "Melancholic", "Aggressive"
        );
        moodComboBox.setValue("Happy");
        
        // Duration spinner
        durationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));
        
        // Initial button states
        playButton.setDisable(true);
        stopButton.setDisable(true);
        exportMp3Button.setDisable(true);
        exportWavButton.setDisable(true);
    }
    
    /**
     * Setup waveform canvas
     */
    private void setupWaveformCanvas() {
        GraphicsContext gc = waveformCanvas.getGraphicsContext2D();
        gc.setFill(Color.web("#2c3e50"));
        gc.fillRect(0, 0, waveformCanvas.getWidth(), waveformCanvas.getHeight());
        
        // Draw placeholder text
        gc.setFill(Color.web("#bdc3c7"));
        gc.fillText("Waveform will appear here after generation", 20, waveformCanvas.getHeight() / 2);
    }
    
    /**
     * Set audio engine dependency
     */
    public void setAudioEngine(AudioEngine audioEngine) {
        this.audioEngine = audioEngine;
    }
    
    /**
     * Handle generate button click
     */
    @FXML
    private void onGenerateClicked() {
        if (isGenerating) {
            logger.warn("Generation already in progress");
            return;
        }
        
        String genre = genreComboBox.getValue();
        String mood = moodComboBox.getValue();
        String lyrics = lyricsTextArea.getText().trim();
        int duration = durationSpinner.getValue();
        
        logger.info("Starting song generation - Genre: {}, Mood: {}, Duration: {}min", 
                   genre, mood, duration);
        
        // Update UI for generation
        isGenerating = true;
        updateUIState();
        statusLabel.setText("Generating song...");
        progressIndicator.setVisible(true);
        
        // Generate song asynchronously
        Task<SongStructure> generateTask = new Task<SongStructure>() {
            @Override
            protected SongStructure call() throws Exception {
                updateMessage("Initializing composition engine...");
                Thread.sleep(500); // Simulate processing time
                
                updateMessage("Generating chord progressions...");
                Thread.sleep(1000);
                
                updateMessage("Creating melodies...");
                Thread.sleep(1000);
                
                updateMessage("Arranging instruments...");
                Thread.sleep(1000);
                
                updateMessage("Synthesizing audio...");
                Thread.sleep(2000);
                
                if (lyrics != null && !lyrics.isEmpty()) {
                    updateMessage("Generating vocals...");
                    Thread.sleep(1500);
                }
                
                updateMessage("Applying effects and mastering...");
                Thread.sleep(1000);
                
                // Generate the actual song
                return audioEngine.generateSong(genre, mood, lyrics, duration).get();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    currentSong = getValue();
                    onGenerationComplete();
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    logger.error("Song generation failed", getException());
                    onGenerationFailed(getException());
                });
            }
        };
        
        // Bind status updates
        statusLabel.textProperty().bind(generateTask.messageProperty());
        
        // Run task
        Thread generateThread = new Thread(generateTask);
        generateThread.setDaemon(true);
        generateThread.start();
    }
    
    /**
     * Handle successful generation completion
     */
    private void onGenerationComplete() {
        logger.info("Song generation completed successfully");
        
        isGenerating = false;
        updateUIState();
        statusLabel.textProperty().unbind();
        statusLabel.setText("Song generated successfully!");
        progressIndicator.setVisible(false);
        
        // Update song info
        if (currentSong != null) {
            songInfoTextArea.setText(currentSong.getSummary());
            drawWaveform();
            setupMixer();
        }
    }
    
    /**
     * Handle generation failure
     */
    private void onGenerationFailed(Throwable exception) {
        logger.error("Song generation failed", exception);
        
        isGenerating = false;
        updateUIState();
        statusLabel.textProperty().unbind();
        statusLabel.setText("Generation failed: " + exception.getMessage());
        progressIndicator.setVisible(false);
        
        // Show error dialog
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Generation Error");
        alert.setHeaderText("Failed to generate song");
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
    
    /**
     * Handle play button click
     */
    @FXML
    private void onPlayClicked() {
        if (audioEngine != null && currentSong != null) {
            logger.info("Starting playback");
            audioEngine.playSong();
            updatePlaybackState();
        }
    }
    
    /**
     * Handle stop button click
     */
    @FXML
    private void onStopClicked() {
        if (audioEngine != null) {
            logger.info("Stopping playback");
            audioEngine.stopPlayback();
            updatePlaybackState();
        }
    }
    
    /**
     * Handle MP3 export button click
     */
    @FXML
    private void onExportMp3Clicked() {
        exportSong("mp3");
    }
    
    /**
     * Handle WAV export button click
     */
    @FXML
    private void onExportWavClicked() {
        exportSong("wav");
    }
    
    /**
     * Export song to file
     */
    private void exportSong(String format) {
        if (currentSong == null) {
            logger.warn("No song to export");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Song");
        fileChooser.setInitialFileName("generated_song." + format);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                format.toUpperCase() + " Files", "*." + format
            )
        );
        
        File file = fileChooser.showSaveDialog(generateButton.getScene().getWindow());
        if (file != null) {
            logger.info("Exporting song to: {}", file.getAbsolutePath());
            
            statusLabel.setText("Exporting " + format.toUpperCase() + "...");
            progressIndicator.setVisible(true);
            
            CompletableFuture<String> exportFuture = audioEngine.exportSong(
                file.getAbsolutePath(), format
            );
            
            exportFuture.thenAccept(result -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Export completed: " + file.getName());
                    progressIndicator.setVisible(false);
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Export Complete");
                    alert.setHeaderText("Song exported successfully");
                    alert.setContentText("File saved to: " + file.getAbsolutePath());
                    alert.showAndWait();
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> {
                    logger.error("Export failed", throwable);
                    statusLabel.setText("Export failed");
                    progressIndicator.setVisible(false);
                    
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Export Error");
                    alert.setHeaderText("Failed to export song");
                    alert.setContentText(throwable.getMessage());
                    alert.showAndWait();
                });
                return null;
            });
        }
    }
    
    /**
     * Update UI state based on current conditions
     */
    private void updateUIState() {
        boolean hasAudioEngine = audioEngine != null;
        boolean hasSong = currentSong != null;
        boolean canGenerate = hasAudioEngine && !isGenerating;
        
        generateButton.setDisable(!canGenerate);
        playButton.setDisable(!hasSong || isGenerating);
        stopButton.setDisable(!hasSong || isGenerating);
        exportMp3Button.setDisable(!hasSong || isGenerating);
        exportWavButton.setDisable(!hasSong || isGenerating);
        
        genreComboBox.setDisable(isGenerating);
        moodComboBox.setDisable(isGenerating);
        durationSpinner.setDisable(isGenerating);
        lyricsTextArea.setDisable(isGenerating);
    }
    
    /**
     * Update playback state
     */
    private void updatePlaybackState() {
        boolean isPlaying = audioEngine != null && audioEngine.isPlaying();
        playButton.setDisable(isPlaying);
        stopButton.setDisable(!isPlaying);
        
        if (isPlaying) {
            // Start progress update timer
            // This would be implemented with a Timeline in a full implementation
        } else {
            progressBar.setProgress(0.0);
            timeLabel.setText("00:00 / 00:00");
        }
    }
    
    /**
     * Draw waveform visualization
     */
    private void drawWaveform() {
        if (currentSong == null) return;
        
        GraphicsContext gc = waveformCanvas.getGraphicsContext2D();
        double width = waveformCanvas.getWidth();
        double height = waveformCanvas.getHeight();
        
        // Clear canvas
        gc.setFill(Color.web("#2c3e50"));
        gc.fillRect(0, 0, width, height);
        
        // Draw waveform (simplified visualization)
        gc.setStroke(Color.web("#3498db"));
        gc.setLineWidth(1.0);
        
        double centerY = height / 2;
        double stepX = width / 1000.0; // 1000 sample points
        
        gc.beginPath();
        for (int i = 0; i < 1000; i++) {
            double x = i * stepX;
            // Generate pseudo-waveform based on song structure
            double amplitude = Math.sin(i * 0.1) * Math.cos(i * 0.05) * (height / 4);
            double y = centerY + amplitude;
            
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                gc.lineTo(x, y);
            }
        }
        gc.stroke();
        
        // Draw section markers
        if (currentSong.getSections() != null) {
            gc.setStroke(Color.web("#e74c3c"));
            gc.setLineWidth(2.0);
            
            int totalBars = currentSong.getTotalBars();
            for (SongStructure.Section section : currentSong.getSections()) {
                double sectionX = (double) section.getStartBar() / totalBars * width;
                gc.strokeLine(sectionX, 0, sectionX, height);
                
                // Section label
                gc.setFill(Color.web("#e74c3c"));
                gc.fillText(section.getName(), sectionX + 5, 15);
            }
        }
    }
    
    /**
     * Setup mixer panel with track controls
     */
    private void setupMixer() {
        mixerPanel.getChildren().clear();
        
        if (currentSong == null || currentSong.getInstruments() == null) {
            return;
        }
        
        for (String instrument : currentSong.getInstruments()) {
            VBox trackControl = createTrackControl(instrument);
            mixerPanel.getChildren().add(trackControl);
        }
    }
    
    /**
     * Create track control for instrument
     */
    private VBox createTrackControl(String instrument) {
        VBox trackBox = new VBox(5);
        trackBox.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 10; -fx-border-color: #bdc3c7;");
        
        Label nameLabel = new Label(instrument.toUpperCase());
        nameLabel.setStyle("-fx-font-weight: bold;");
        
        Slider volumeSlider = new Slider(0, 100, 80);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        
        CheckBox muteCheckBox = new CheckBox("Mute");
        CheckBox soloCheckBox = new CheckBox("Solo");
        
        trackBox.getChildren().addAll(nameLabel, volumeSlider, muteCheckBox, soloCheckBox);
        
        return trackBox;
    }
}

