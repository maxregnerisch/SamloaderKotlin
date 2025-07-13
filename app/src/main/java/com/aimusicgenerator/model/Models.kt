package com.aimusicgenerator.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

data class Genre(
    val id: String,
    val name: String,
    val description: String,
    val color: String = "#6200EE",
    val isSelected: Boolean = false
)

data class Instrument(
    val id: String,
    val name: String,
    val category: String,
    val icon: String = "🎵",
    val isSelected: Boolean = false
)

data class MusicGenerationRequest(
    val selectedGenre: Genre?,
    val selectedInstruments: List<Instrument>,
    val tempo: Int,
    val duration: Int,
    val mood: String = "neutral",
    val complexity: String = "medium",
    val audioFormat: AudioFormat = AudioFormat.WAV_32BIT,
    val sampleRate: Int = 96000
)

@Entity(tableName = "generated_music")
data class GeneratedMusic(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val filePath: String,
    val duration: Int,
    val genre: String,
    val tempo: Int,
    val instruments: String, // JSON string of instrument names
    val createdAt: Long = System.currentTimeMillis(),
    val isPlaying: Boolean = false,
    val audioFormat: String = "WAV_32BIT", // WAV_16BIT, WAV_32BIT, MP3_320, MP3_512
    val sampleRate: Int = 96000,
    val bitDepth: Int = 32,
    val isRemix: Boolean = false,
    val originalFilePath: String? = null,
    val fileSize: Long = 0
)

data class MusicStyle(
    val name: String,
    val baseFrequencies: List<Double>,
    val rhythmPattern: List<Double>,
    val harmonicComplexity: Double
)

data class RemixRequest(
    val inputFilePath: String,
    val remixStyle: String, // "deep_house", "trap", "dubstep", "ambient", "orchestral", "custom"
    val bassBoost: Double = 1.0,
    val trebleBoost: Double = 1.0,
    val tempo: Int? = null, // Optional tempo change
    val pitch: Double = 1.0, // Pitch shift multiplier
    val reverb: Double = 0.0, // Reverb amount (0.0 to 1.0)
    val delay: Double = 0.0, // Delay amount (0.0 to 1.0)
    val distortion: Double = 0.0, // Distortion amount (0.0 to 1.0)
    val outputFormat: AudioFormat = AudioFormat.MP3_320
)

enum class AudioFormat(val displayName: String, val extension: String, val quality: String) {
    WAV_16BIT("WAV 16-bit", "wav", "CD Quality"),
    WAV_32BIT("WAV 32-bit Float", "wav", "Studio Quality"),
    MP3_128("MP3 128 kbps", "mp3", "Standard"),
    MP3_192("MP3 192 kbps", "mp3", "Good"),
    MP3_320("MP3 320 kbps", "mp3", "High Quality"),
    MP3_512("MP3 512 kbps", "mp3", "Ultra Quality")
}

data class AudioFileInfo(
    val filePath: String,
    val fileName: String,
    val format: String,
    val duration: Long, // in milliseconds
    val sampleRate: Int,
    val channels: Int,
    val bitRate: Int,
    val fileSize: Long
)

data class RemixStyle(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val presetSettings: RemixPreset
)

data class RemixPreset(
    val bassBoost: Double,
    val trebleBoost: Double,
    val reverb: Double,
    val delay: Double,
    val distortion: Double,
    val tempoChange: Double = 1.0
)

data class ExportOptions(
    val format: AudioFormat,
    val quality: String,
    val fileName: String,
    val includeMetadata: Boolean = true
)

