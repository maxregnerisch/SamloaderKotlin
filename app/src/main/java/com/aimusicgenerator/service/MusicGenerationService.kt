package com.aimusicgenerator.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.aimusicgenerator.ai.MusicAIEngine
import com.aimusicgenerator.model.MusicGenerationRequest
import kotlinx.coroutines.*
import java.io.File

class MusicGenerationService : Service() {
    
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val aiEngine = MusicAIEngine()
    
    inner class LocalBinder : Binder() {
        fun getService(): MusicGenerationService = this@MusicGenerationService
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    fun generateMusic(
        request: MusicGenerationRequest,
        callback: (Result<String>) -> Unit
    ) {
        serviceScope.launch {
            try {
                val outputPath = createOutputPath()
                val success = aiEngine.generateMusic(request, outputPath)
                
                if (success) {
                    callback(Result.success(outputPath))
                } else {
                    callback(Result.failure(Exception("Music generation failed")))
                }
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }
    
    private fun createOutputPath(): String {
        val outputDir = File(applicationContext.getExternalFilesDir(null), "generated_music")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        
        val timestamp = System.currentTimeMillis()
        return File(outputDir, "generated_music_$timestamp.wav").absolutePath
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

