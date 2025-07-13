package com.aimusicgenerator.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

data class Genre(
    val id: String,
    val name: String,
    val description: String,
    val isSelected: Boolean = false
)

data class Instrument(
    val id: String,
    val name: String,
    val category: String,
    val isSelected: Boolean = false
)

data class MusicGenerationRequest(
    val selectedGenre: Genre?,
    val selectedInstruments: List<Instrument>,
    val tempo: Int,
    val duration: Int,
    val mood: String = "neutral",
    val complexity: String = "medium"
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
    val isPlaying: Boolean = false
)

data class MusicStyle(
    val name: String,
    val baseFrequencies: List<Double>,
    val rhythmPattern: List<Double>,
    val harmonicComplexity: Double
)

