package com.aimusicgenerator

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aimusicgenerator.adapter.GenreAdapter
import com.aimusicgenerator.adapter.InstrumentAdapter
import com.aimusicgenerator.databinding.ActivityGeneratorBinding
import com.aimusicgenerator.model.Genre
import com.aimusicgenerator.model.Instrument
import com.aimusicgenerator.model.MusicGenerationRequest
import com.aimusicgenerator.service.MusicGenerationService
import com.aimusicgenerator.viewmodel.GeneratorViewModel
import java.io.File

class GeneratorActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityGeneratorBinding
    private lateinit var viewModel: GeneratorViewModel
    private lateinit var genreAdapter: GenreAdapter
    private lateinit var instrumentAdapter: InstrumentAdapter
    
    private var musicGenerationService: MusicGenerationService? = null
    private var isServiceBound = false
    private var mediaPlayer: MediaPlayer? = null
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicGenerationService.LocalBinder
            musicGenerationService = binder.getService()
            isServiceBound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            musicGenerationService = null
            isServiceBound = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[GeneratorViewModel::class.java]
        
        setupUI()
        setupRecyclerViews()
        observeViewModel()
        bindMusicGenerationService()
    }
    
    private fun setupUI() {
        binding.apply {
            // Tempo slider
            seekBarTempo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val tempo = 60 + progress * 2 // Range: 60-200 BPM
                    tvTempoValue.text = "$tempo BPM"
                    viewModel.setTempo(tempo)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            
            // Duration slider
            seekBarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val duration = 15 + progress * 3 // Range: 15-180 seconds
                    tvDurationValue.text = "${duration}s"
                    viewModel.setDuration(duration)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            
            // Generate button
            btnGenerate.setOnClickListener {
                generateMusic()
            }
            
            // Play/Pause button
            btnPlayPause.setOnClickListener {
                togglePlayback()
            }
            
            // Save button
            btnSave.setOnClickListener {
                saveGeneratedMusic()
            }
            
            // Back button
            btnBack.setOnClickListener {
                finish()
            }
        }
    }
    
    private fun setupRecyclerViews() {
        // Genre selection
        genreAdapter = GenreAdapter { genre ->
            viewModel.selectGenre(genre)
        }
        binding.recyclerViewGenres.apply {
            layoutManager = LinearLayoutManager(this@GeneratorActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = genreAdapter
        }
        
        // Instrument selection
        instrumentAdapter = InstrumentAdapter { instrument ->
            viewModel.toggleInstrument(instrument)
        }
        binding.recyclerViewInstruments.apply {
            layoutManager = LinearLayoutManager(this@GeneratorActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = instrumentAdapter
        }
        
        // Load data
        viewModel.loadGenres()
        viewModel.loadInstruments()
    }
    
    private fun observeViewModel() {
        viewModel.genres.observe(this) { genres ->
            genreAdapter.submitList(genres)
        }
        
        viewModel.instruments.observe(this) { instruments ->
            instrumentAdapter.submitList(instruments)
        }
        
        viewModel.isGenerating.observe(this) { isGenerating ->
            binding.apply {
                progressBarGeneration.visibility = if (isGenerating) View.VISIBLE else View.GONE
                btnGenerate.isEnabled = !isGenerating
                btnGenerate.text = if (isGenerating) "Generating..." else "Generate Music"
            }
        }
        
        viewModel.generatedMusicPath.observe(this) { path ->
            if (path.isNotEmpty()) {
                binding.apply {
                    layoutPlayback.visibility = View.VISIBLE
                    btnPlayPause.isEnabled = true
                    btnSave.isEnabled = true
                }
                setupMediaPlayer(path)
            }
        }
        
        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun generateMusic() {
        val request = viewModel.createGenerationRequest()
        if (isServiceBound) {
            musicGenerationService?.generateMusic(request) { result ->
                runOnUiThread {
                    if (result.isSuccess) {
                        viewModel.setGeneratedMusicPath(result.getOrNull() ?: "")
                    } else {
                        viewModel.setError(result.exceptionOrNull()?.message ?: "Generation failed")
                    }
                }
            }
        }
    }
    
    private fun setupMediaPlayer(filePath: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepareAsync()
                setOnPreparedListener {
                    binding.seekBarPlayback.max = duration
                    binding.tvTotalDuration.text = formatTime(duration)
                }
                setOnCompletionListener {
                    binding.btnPlayPause.text = "Play"
                    binding.seekBarPlayback.progress = 0
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading audio: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun togglePlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                binding.btnPlayPause.text = "Play"
            } else {
                player.start()
                binding.btnPlayPause.text = "Pause"
                updatePlaybackProgress()
            }
        }
    }
    
    private fun updatePlaybackProgress() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                binding.seekBarPlayback.progress = player.currentPosition
                binding.tvCurrentTime.text = formatTime(player.currentPosition)
                binding.seekBarPlayback.postDelayed({ updatePlaybackProgress() }, 100)
            }
        }
    }
    
    private fun saveGeneratedMusic() {
        val currentPath = viewModel.generatedMusicPath.value
        if (!currentPath.isNullOrEmpty()) {
            viewModel.saveToLibrary(currentPath)
            Toast.makeText(this, "Music saved to library!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
    
    private fun bindMusicGenerationService() {
        val intent = Intent(this, MusicGenerationService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        if (isServiceBound) {
            unbindService(serviceConnection)
        }
    }
}

