package com.aimusicgenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aimusicgenerator.ai.AdvancedAudioEngine
import com.aimusicgenerator.model.RemixRequest
import com.aimusicgenerator.model.RemixStyle
import kotlinx.coroutines.launch
import java.io.File

class RemixViewModel : ViewModel() {
    
    private val _selectedRemixStyle = MutableLiveData<RemixStyle?>()
    val selectedRemixStyle: LiveData<RemixStyle?> = _selectedRemixStyle
    
    private val _bassBoost = MutableLiveData(1.0)
    val bassBoost: LiveData<Double> = _bassBoost
    
    private val _trebleBoost = MutableLiveData(1.0)
    val trebleBoost: LiveData<Double> = _trebleBoost
    
    private val _reverb = MutableLiveData(0.0)
    val reverb: LiveData<Double> = _reverb
    
    private val _delay = MutableLiveData(0.0)
    val delay: LiveData<Double> = _delay
    
    private val _distortion = MutableLiveData(0.0)
    val distortion: LiveData<Double> = _distortion
    
    private val _isRemixing = MutableLiveData(false)
    val isRemixing: LiveData<Boolean> = _isRemixing
    
    private val _remixProgress = MutableLiveData(0)
    val remixProgress: LiveData<Int> = _remixProgress
    
    private val _remixComplete = MutableLiveData<String?>()
    val remixComplete: LiveData<String?> = _remixComplete
    
    private val _remixOutputPath = MutableLiveData<String?>()
    val remixOutputPath: LiveData<String?> = _remixOutputPath
    
    fun selectRemixStyle(style: RemixStyle) {
        _selectedRemixStyle.value = style
        
        // Update parameters based on preset
        _bassBoost.value = style.presetSettings.bassBoost
        _trebleBoost.value = style.presetSettings.trebleBoost
        _reverb.value = style.presetSettings.reverb
        _delay.value = style.presetSettings.delay
        _distortion.value = style.presetSettings.distortion
    }
    
    fun updateBassBoost(boost: Double) {
        _bassBoost.value = boost
    }
    
    fun updateTrebleBoost(boost: Double) {
        _trebleBoost.value = boost
    }
    
    fun updateReverb(reverb: Double) {
        _reverb.value = reverb
    }
    
    fun updateDelay(delay: Double) {
        _delay.value = delay
    }
    
    fun updateDistortion(distortion: Double) {
        _distortion.value = distortion
    }
    
    fun startRemixing(request: RemixRequest, audioEngine: AdvancedAudioEngine) {
        viewModelScope.launch {
            try {
                _isRemixing.value = true
                _remixProgress.value = 0
                
                // Generate output path
                val outputDir = File("/storage/emulated/0/Music/AI_Music_Generator/Remixes")
                if (!outputDir.exists()) {
                    outputDir.mkdirs()
                }
                
                val timestamp = System.currentTimeMillis()
                val outputPath = "${outputDir.absolutePath}/remix_${timestamp}.${request.outputFormat.extension}"
                
                // Simulate progress updates
                updateProgress(10)
                
                // Perform the actual remixing
                val success = audioEngine.remixAudio(request, outputPath, request.outputFormat)
                
                updateProgress(100)
                
                if (success) {
                    _remixOutputPath.value = outputPath
                    _remixComplete.value = outputPath
                } else {
                    _remixComplete.value = null
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                _remixComplete.value = null
            } finally {
                _isRemixing.value = false
            }
        }
    }
    
    private suspend fun updateProgress(progress: Int) {
        _remixProgress.value = progress
        kotlinx.coroutines.delay(100) // Small delay for UI updates
    }
}

