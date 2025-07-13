package com.aimusicgenerator.ai

import com.aimusicgenerator.model.MusicGenerationRequest
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import kotlin.math.*
import kotlin.random.Random

class MusicAIEngine {
    
    private val sampleRate = 44100
    private val channels = 2
    private val bitsPerSample = 16
    
    suspend fun generateMusic(request: MusicGenerationRequest, outputPath: String): Boolean {
        return try {
            // Simulate AI processing time
            delay(2000)
            
            val durationInSamples = request.duration * sampleRate
            val audioData = generateAudioData(request, durationInSamples)
            
            writeWavFile(audioData, outputPath)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun generateAudioData(request: MusicGenerationRequest, samples: Int): ShortArray {
        val audioData = ShortArray(samples * channels)
        val tempo = request.tempo
        val genre = request.selectedGenre?.name ?: "Electronic"
        
        // Generate music based on genre and parameters
        when (genre.lowercase()) {
            "electronic" -> generateElectronicMusic(audioData, tempo, samples)
            "classical" -> generateClassicalMusic(audioData, tempo, samples)
            "jazz" -> generateJazzMusic(audioData, tempo, samples)
            "rock" -> generateRockMusic(audioData, tempo, samples)
            "ambient" -> generateAmbientMusic(audioData, tempo, samples)
            else -> generateDefaultMusic(audioData, tempo, samples)
        }
        
        return audioData
    }
    
    private fun generateElectronicMusic(audioData: ShortArray, tempo: Int, samples: Int) {
        val beatsPerSecond = tempo / 60.0
        val samplesPerBeat = (sampleRate / beatsPerSecond).toInt()
        
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val beatPosition = (i % samplesPerBeat).toDouble() / samplesPerBeat
            
            // Bass line (sine wave)
            val bassFreq = if (beatPosition < 0.5) 80.0 else 60.0
            val bass = sin(2 * PI * bassFreq * time) * 0.3
            
            // Lead synth (sawtooth wave)
            val leadFreq = 440.0 * (1 + sin(time * 0.5) * 0.2)
            val lead = generateSawtooth(leadFreq, time) * 0.2
            
            // Kick drum simulation
            val kickEnvelope = if (beatPosition < 0.1) exp(-beatPosition * 50) else 0.0
            val kick = sin(2 * PI * 60 * time) * kickEnvelope * 0.4
            
            val sample = (bass + lead + kick) * Short.MAX_VALUE * 0.7
            val clampedSample = sample.coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble()).toInt().toShort()
            
            audioData[i * channels] = clampedSample // Left channel
            audioData[i * channels + 1] = clampedSample // Right channel
        }
    }
    
    private fun generateClassicalMusic(audioData: ShortArray, tempo: Int, samples: Int) {
        val notes = arrayOf(261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88) // C major scale
        val noteDuration = sampleRate / 2 // Half second per note
        
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val noteIndex = (i / noteDuration) % notes.size
            val noteTime = (i % noteDuration).toDouble() / sampleRate
            
            // Piano-like envelope
            val envelope = exp(-noteTime * 2)
            
            // Harmonic series for richer sound
            val fundamental = sin(2 * PI * notes[noteIndex] * time) * envelope
            val harmonic2 = sin(2 * PI * notes[noteIndex] * 2 * time) * envelope * 0.3
            val harmonic3 = sin(2 * PI * notes[noteIndex] * 3 * time) * envelope * 0.1
            
            val sample = (fundamental + harmonic2 + harmonic3) * Short.MAX_VALUE * 0.5
            val clampedSample = sample.coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble()).toInt().toShort()
            
            audioData[i * channels] = clampedSample
            audioData[i * channels + 1] = clampedSample
        }
    }
    
    private fun generateJazzMusic(audioData: ShortArray, tempo: Int, samples: Int) {
        val jazzChord = arrayOf(261.63, 329.63, 392.00, 466.16) // C7 chord
        val swingRatio = 0.67 // Swing feel
        
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val beatTime = (time * tempo / 60.0) % 1.0
            
            // Swing rhythm adjustment
            val swingTime = if (beatTime < 0.5) beatTime * swingRatio else 0.5 * swingRatio + (beatTime - 0.5) * (2 - swingRatio)
            
            // Walking bass line
            val bassFreq = 80.0 + sin(time * 0.3) * 20.0
            val bass = sin(2 * PI * bassFreq * time) * 0.3
            
            // Jazz chord
            var chord = 0.0
            for (freq in jazzChord) {
                chord += sin(2 * PI * freq * time) * 0.1
            }
            
            // Brush drums simulation (noise)
            val brushes = (Random.nextDouble() - 0.5) * 0.1 * sin(swingTime * PI)
            
            val sample = (bass + chord + brushes) * Short.MAX_VALUE * 0.6
            val clampedSample = sample.coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble()).toInt().toShort()
            
            audioData[i * channels] = clampedSample
            audioData[i * channels + 1] = clampedSample
        }
    }
    
    private fun generateRockMusic(audioData: ShortArray, tempo: Int, samples: Int) {
        val powerChord = arrayOf(82.41, 164.81) // E power chord
        val beatsPerSecond = tempo / 60.0
        val samplesPerBeat = (sampleRate / beatsPerSecond).toInt()
        
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val beatPosition = (i % samplesPerBeat).toDouble() / samplesPerBeat
            
            // Distorted guitar (square wave with harmonics)
            var guitar = 0.0
            for (freq in powerChord) {
                guitar += generateSquareWave(freq, time) * 0.2
            }
            
            // Add distortion
            guitar = tanh(guitar * 3) * 0.3
            
            // Drums
            val kickEnvelope = if (beatPosition < 0.05) exp(-beatPosition * 100) else 0.0
            val kick = sin(2 * PI * 60 * time) * kickEnvelope * 0.5
            
            val snareEnvelope = if (beatPosition > 0.45 && beatPosition < 0.55) exp(-(beatPosition - 0.5) * 100) else 0.0
            val snare = (Random.nextDouble() - 0.5) * snareEnvelope * 0.3
            
            val sample = (guitar + kick + snare) * Short.MAX_VALUE * 0.7
            val clampedSample = sample.coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble()).toInt().toShort()
            
            audioData[i * channels] = clampedSample
            audioData[i * channels + 1] = clampedSample
        }
    }
    
    private fun generateAmbientMusic(audioData: ShortArray, tempo: Int, samples: Int) {
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            
            // Slow evolving pad sounds
            val pad1 = sin(2 * PI * 220 * time + sin(time * 0.1) * 2) * 0.2
            val pad2 = sin(2 * PI * 330 * time + sin(time * 0.07) * 1.5) * 0.15
            val pad3 = sin(2 * PI * 440 * time + sin(time * 0.13) * 1) * 0.1
            
            // Reverb simulation (simple delay)
            val delay = if (i > sampleRate / 4) audioData[(i - sampleRate / 4) * channels].toDouble() / Short.MAX_VALUE * 0.3 else 0.0
            
            val sample = (pad1 + pad2 + pad3 + delay) * Short.MAX_VALUE * 0.6
            val clampedSample = sample.coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble()).toInt().toShort()
            
            audioData[i * channels] = clampedSample
            audioData[i * channels + 1] = clampedSample
        }
    }
    
    private fun generateDefaultMusic(audioData: ShortArray, tempo: Int, samples: Int) {
        generateElectronicMusic(audioData, tempo, samples)
    }
    
    private fun generateSawtooth(frequency: Double, time: Double): Double {
        val period = 1.0 / frequency
        val phase = (time % period) / period
        return 2 * phase - 1
    }
    
    private fun generateSquareWave(frequency: Double, time: Double): Double {
        return if (sin(2 * PI * frequency * time) >= 0) 1.0 else -1.0
    }
    
    private fun writeWavFile(audioData: ShortArray, outputPath: String) {
        val file = File(outputPath)
        val fos = FileOutputStream(file)
        
        // WAV header
        val dataSize = audioData.size * 2 // 2 bytes per sample
        val fileSize = 36 + dataSize
        
        // RIFF header
        fos.write("RIFF".toByteArray())
        fos.write(intToByteArray(fileSize))
        fos.write("WAVE".toByteArray())
        
        // Format chunk
        fos.write("fmt ".toByteArray())
        fos.write(intToByteArray(16)) // Chunk size
        fos.write(shortToByteArray(1)) // Audio format (PCM)
        fos.write(shortToByteArray(channels.toShort()))
        fos.write(intToByteArray(sampleRate))
        fos.write(intToByteArray(sampleRate * channels * bitsPerSample / 8)) // Byte rate
        fos.write(shortToByteArray((channels * bitsPerSample / 8).toShort())) // Block align
        fos.write(shortToByteArray(bitsPerSample.toShort()))
        
        // Data chunk
        fos.write("data".toByteArray())
        fos.write(intToByteArray(dataSize))
        
        // Audio data
        for (sample in audioData) {
            fos.write(shortToByteArray(sample))
        }
        
        fos.close()
    }
    
    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }
    
    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }
}

