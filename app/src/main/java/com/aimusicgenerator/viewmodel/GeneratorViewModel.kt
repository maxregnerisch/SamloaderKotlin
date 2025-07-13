package com.aimusicgenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aimusicgenerator.database.MusicDatabase
import com.aimusicgenerator.model.GeneratedMusic
import com.aimusicgenerator.model.Genre
import com.aimusicgenerator.model.Instrument
import com.aimusicgenerator.model.MusicGenerationRequest
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GeneratorViewModel : ViewModel() {
    
    private val _genres = MutableLiveData<List<Genre>>()
    val genres: LiveData<List<Genre>> = _genres
    
    private val _instruments = MutableLiveData<List<Instrument>>()
    val instruments: LiveData<List<Instrument>> = _instruments
    
    private val _selectedGenre = MutableLiveData<Genre?>()
    val selectedGenre: LiveData<Genre?> = _selectedGenre
    
    private val _selectedInstruments = MutableLiveData<List<Instrument>>()
    val selectedInstruments: LiveData<List<Instrument>> = _selectedInstruments
    
    private val _tempo = MutableLiveData<Int>()
    val tempo: LiveData<Int> = _tempo
    
    private val _duration = MutableLiveData<Int>()
    val duration: LiveData<Int> = _duration
    
    private val _isGenerating = MutableLiveData<Boolean>()
    val isGenerating: LiveData<Boolean> = _isGenerating
    
    private val _generatedMusicPath = MutableLiveData<String>()
    val generatedMusicPath: LiveData<String> = _generatedMusicPath
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    init {
        _tempo.value = 120
        _duration.value = 30
        _selectedInstruments.value = emptyList()
    }
    
    fun loadGenres() {
        val genreList = listOf(
            Genre("1", "Electronic", "Modern electronic music with synthesizers"),
            Genre("2", "Classical", "Traditional orchestral music"),
            Genre("3", "Jazz", "Smooth jazz with improvisation"),
            Genre("4", "Rock", "Energetic rock music"),
            Genre("5", "Ambient", "Atmospheric and relaxing sounds"),
            Genre("6", "Pop", "Catchy popular music"),
            Genre("7", "Hip Hop", "Rhythmic rap and beats"),
            Genre("8", "Folk", "Traditional acoustic music")
        )
        _genres.value = genreList
    }
    
    fun loadInstruments() {
        val instrumentList = listOf(
            Instrument("1", "Piano", "Keyboard", false),
            Instrument("2", "Guitar", "String", false),
            Instrument("3", "Violin", "String", false),
            Instrument("4", "Drums", "Percussion", false),
            Instrument("5", "Bass", "String", false),
            Instrument("6", "Flute", "Wind", false),
            Instrument("7", "Saxophone", "Wind", false),
            Instrument("8", "Synthesizer", "Electronic", false),
            Instrument("9", "Trumpet", "Brass", false),
            Instrument("10", "Cello", "String", false)
        )
        _instruments.value = instrumentList
    }
    
    fun selectGenre(genre: Genre) {
        val updatedGenres = _genres.value?.map { 
            it.copy(isSelected = it.id == genre.id) 
        }
        _genres.value = updatedGenres
        _selectedGenre.value = genre
    }
    
    fun toggleInstrument(instrument: Instrument) {
        val currentInstruments = _instruments.value?.toMutableList() ?: mutableListOf()
        val index = currentInstruments.indexOfFirst { it.id == instrument.id }
        
        if (index != -1) {
            currentInstruments[index] = currentInstruments[index].copy(
                isSelected = !currentInstruments[index].isSelected
            )
            _instruments.value = currentInstruments
            
            _selectedInstruments.value = currentInstruments.filter { it.isSelected }
        }
    }
    
    fun setTempo(tempo: Int) {
        _tempo.value = tempo
    }
    
    fun setDuration(duration: Int) {
        _duration.value = duration
    }
    
    fun createGenerationRequest(): MusicGenerationRequest {
        return MusicGenerationRequest(
            selectedGenre = _selectedGenre.value,
            selectedInstruments = _selectedInstruments.value ?: emptyList(),
            tempo = _tempo.value ?: 120,
            duration = _duration.value ?: 30
        )
    }
    
    fun setGeneratedMusicPath(path: String) {
        _generatedMusicPath.value = path
        _isGenerating.value = false
    }
    
    fun setError(message: String) {
        _errorMessage.value = message
        _isGenerating.value = false
    }
    
    fun startGeneration() {
        _isGenerating.value = true
        _errorMessage.value = ""
    }
    
    fun saveToLibrary(filePath: String) {
        viewModelScope.launch {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    _errorMessage.value = "Generated file not found"
                    return@launch
                }
                
                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                val title = "Generated Music ${dateFormat.format(Date())}"
                
                val instrumentNames = _selectedInstruments.value?.map { it.name } ?: emptyList()
                val instrumentsJson = Gson().toJson(instrumentNames)
                
                val generatedMusic = GeneratedMusic(
                    title = title,
                    filePath = filePath,
                    duration = _duration.value ?: 30,
                    genre = _selectedGenre.value?.name ?: "Unknown",
                    tempo = _tempo.value ?: 120,
                    instruments = instrumentsJson
                )
                
                // Save to database would go here
                // database.musicDao().insert(generatedMusic)
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save music: ${e.message}"
            }
        }
    }
}

