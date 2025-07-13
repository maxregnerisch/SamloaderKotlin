package com.aimusicgenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aimusicgenerator.model.GeneratedMusic
import kotlinx.coroutines.launch
import java.io.File

class LibraryViewModel : ViewModel() {
    
    private val _musicLibrary = MutableLiveData<List<GeneratedMusic>>()
    val musicLibrary: LiveData<List<GeneratedMusic>> = _musicLibrary
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    fun loadLibrary() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // In a real app, this would load from database
                // For demo purposes, we'll create some sample data
                val sampleMusic = createSampleMusicLibrary()
                _musicLibrary.value = sampleMusic
                
                _errorMessage.value = ""
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load library: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteMusic(music: GeneratedMusic) {
        viewModelScope.launch {
            try {
                // Delete file
                val file = File(music.filePath)
                if (file.exists()) {
                    file.delete()
                }
                
                // Remove from database
                // database.musicDao().delete(music)
                
                // Update UI
                val currentList = _musicLibrary.value?.toMutableList() ?: mutableListOf()
                currentList.removeAll { it.id == music.id }
                _musicLibrary.value = currentList
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete music: ${e.message}"
            }
        }
    }
    
    private fun createSampleMusicLibrary(): List<GeneratedMusic> {
        // This would normally come from the database
        return listOf(
            GeneratedMusic(
                id = "1",
                title = "Electronic Beats #1",
                filePath = "/storage/emulated/0/Android/data/com.aimusicgenerator/files/generated_music/sample1.wav",
                duration = 45,
                genre = "Electronic",
                tempo = 128,
                instruments = "[\"Synthesizer\", \"Drums\"]",
                createdAt = System.currentTimeMillis() - 86400000 // 1 day ago
            ),
            GeneratedMusic(
                id = "2",
                title = "Classical Symphony",
                filePath = "/storage/emulated/0/Android/data/com.aimusicgenerator/files/generated_music/sample2.wav",
                duration = 60,
                genre = "Classical",
                tempo = 90,
                instruments = "[\"Piano\", \"Violin\", \"Cello\"]",
                createdAt = System.currentTimeMillis() - 172800000 // 2 days ago
            ),
            GeneratedMusic(
                id = "3",
                title = "Jazz Improvisation",
                filePath = "/storage/emulated/0/Android/data/com.aimusicgenerator/files/generated_music/sample3.wav",
                duration = 75,
                genre = "Jazz",
                tempo = 110,
                instruments = "[\"Piano\", \"Saxophone\", \"Bass\", \"Drums\"]",
                createdAt = System.currentTimeMillis() - 259200000 // 3 days ago
            )
        )
    }
}

