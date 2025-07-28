# AI Music Generator

A sophisticated Java-based AI music generator with vocal synthesis, multi-track arrangement, and professional audio export capabilities.

## Features

🎵 **AI-Powered Composition**
- Intelligent chord progression generation based on music theory
- Genre-specific melody creation (Pop, Rock, Electronic, Jazz, Classical, etc.)
- Mood-based musical parameter selection
- Complete song structure generation (intro, verse, chorus, bridge, outro)

🎤 **Vocal Synthesis**
- Text-to-speech with musical pitch modulation
- Lyrics synchronization with musical rhythm
- Realistic singing voice generation

🎛️ **Multi-Track Audio System**
- Professional instrument synthesis (drums, bass, guitar, piano, synth, etc.)
- Real-time audio mixing and effects processing
- Individual track volume and mute controls

🎚️ **Audio Effects & Processing**
- Reverb, delay, compression, and EQ
- Genre-specific instrument effects
- Professional mastering chain

💾 **Export Capabilities**
- High-quality MP3 and WAV export
- Professional audio encoding
- Customizable export settings

🖥️ **Modern JavaFX Interface**
- Intuitive music generation controls
- Real-time waveform visualization
- Professional mixer interface
- Progress tracking and status updates

## Technical Architecture

### Core Components

- **AudioEngine**: Central orchestrator for all audio operations
- **CompositionEngine**: AI-powered music composition using music theory algorithms
- **MidiHandler**: MIDI sequence creation and instrument management
- **SynthesizerManager**: Advanced audio synthesis and instrument effects
- **TrackManager**: Multi-track audio mixing and arrangement
- **VocalSynthesizer**: Text-to-speech with pitch modulation for singing
- **EffectsProcessor**: Professional audio effects and processing
- **AudioExporter**: High-quality audio file export

### Music Theory Implementation

- **ChordProgression**: Generates realistic chord progressions based on genre and mood
- **MelodyGenerator**: Creates melodies that follow musical scales and chord tones
- **RhythmPattern**: Genre-specific rhythm and drum pattern generation
- **GenreManager**: Manages instrument selections and characteristics per genre

## Requirements

- Java 17 or higher
- JavaFX 19+
- Maven 3.6+
- Audio system with MIDI support

## Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ai-music-generator
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

   Or build and run the JAR:
   ```bash
   mvn clean package
   java -jar target/ai-music-generator-1.0.0.jar
   ```

## Usage

### Basic Song Generation

1. **Select Genre**: Choose from Pop, Rock, Electronic, Jazz, Classical, etc.
2. **Choose Mood**: Select the emotional tone (Happy, Sad, Energetic, Calm, etc.)
3. **Set Duration**: Specify song length (1-10 minutes)
4. **Add Lyrics** (Optional): Enter lyrics for vocal synthesis
5. **Generate**: Click "Generate Song" to create your music

### Playback & Export

- **Play/Stop**: Control playback with the transport controls
- **Waveform**: View real-time waveform visualization with section markers
- **Mixer**: Adjust individual track volumes and mute/solo tracks
- **Export**: Save your generated music as MP3 or WAV files

### Advanced Features

- **Real-time Progress**: Monitor generation progress with detailed status updates
- **Song Structure**: View generated song sections and musical information
- **Professional Quality**: High-quality 44.1kHz audio with professional effects

## Architecture Details

### Audio Processing Pipeline

1. **Composition**: AI generates chord progressions, melodies, and song structure
2. **Arrangement**: Selects appropriate instruments based on genre
3. **Synthesis**: Converts musical data to audio using MIDI synthesis
4. **Vocals**: Processes lyrics into singing voice (if provided)
5. **Effects**: Applies reverb, compression, EQ, and mastering
6. **Export**: Encodes final audio to MP3/WAV format

### Music Theory Engine

The composition engine uses advanced music theory algorithms:

- **Scale-based harmony**: Generates chords that fit the selected key and mode
- **Voice leading**: Ensures smooth transitions between chords
- **Melodic contour**: Creates memorable melodies that follow musical principles
- **Rhythmic variation**: Generates appropriate rhythm patterns for each genre
- **Song form**: Structures complete songs with proper sections and transitions

## Development

### Project Structure

```
src/main/java/com/musicgen/
├── audio/              # Core audio engine and MIDI handling
├── composition/        # AI composition algorithms
├── theory/            # Music theory implementation
├── tracks/            # Multi-track audio management
├── vocals/            # Vocal synthesis system
├── genres/            # Genre-specific characteristics
├── effects/           # Audio effects processing
├── ui/                # JavaFX user interface
├── structure/         # Song structure representation
└── export/            # Audio export functionality
```

### Key Technologies

- **Java Sound API**: MIDI synthesis and audio playback
- **JavaFX**: Modern user interface
- **Maven**: Build and dependency management
- **SLF4J + Logback**: Comprehensive logging
- **Jackson**: JSON configuration handling

## Contributing

1. Fork the repository
2. Create a feature branch
3. Implement your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built with Java Sound API for professional audio processing
- Uses music theory principles for realistic composition
- Inspired by modern AI music generation techniques

---

**Note**: This is a sophisticated music generation system that demonstrates advanced Java audio programming, music theory implementation, and AI-driven composition techniques. While some features like advanced vocal synthesis may require additional development, the core architecture provides a solid foundation for professional music generation applications.

