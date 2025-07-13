package com.aimusicgenerator

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aimusicgenerator.adapter.MusicLibraryAdapter
import com.aimusicgenerator.databinding.ActivityLibraryBinding
import com.aimusicgenerator.model.GeneratedMusic
import com.aimusicgenerator.viewmodel.LibraryViewModel

class LibraryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLibraryBinding
    private lateinit var viewModel: LibraryViewModel
    private lateinit var libraryAdapter: MusicLibraryAdapter
    
    private var mediaPlayer: MediaPlayer? = null
    private var currentlyPlayingItem: GeneratedMusic? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[LibraryViewModel::class.java]
        
        setupUI()
        setupRecyclerView()
        observeViewModel()
        
        viewModel.loadLibrary()
    }
    
    private fun setupUI() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }
            
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.loadLibrary()
            }
        }
    }
    
    private fun setupRecyclerView() {
        libraryAdapter = MusicLibraryAdapter(
            onPlayClick = { music ->
                playMusic(music)
            },
            onDeleteClick = { music ->
                viewModel.deleteMusic(music)
            },
            onShareClick = { music ->
                shareMusic(music)
            }
        )
        
        binding.recyclerViewLibrary.apply {
            layoutManager = LinearLayoutManager(this@LibraryActivity)
            adapter = libraryAdapter
        }
    }
    
    private fun observeViewModel() {
        viewModel.musicLibrary.observe(this) { musicList ->
            libraryAdapter.submitList(musicList)
            binding.apply {
                if (musicList.isEmpty()) {
                    recyclerViewLibrary.visibility = View.GONE
                    tvEmptyLibrary.visibility = View.VISIBLE
                } else {
                    recyclerViewLibrary.visibility = View.VISIBLE
                    tvEmptyLibrary.visibility = View.GONE
                }
            }
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
        
        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun playMusic(music: GeneratedMusic) {
        try {
            // Stop current playback
            mediaPlayer?.release()
            
            // Update UI for previously playing item
            currentlyPlayingItem?.let { previousItem ->
                libraryAdapter.updatePlayingState(previousItem.id, false)
            }
            
            // Start new playback
            mediaPlayer = MediaPlayer().apply {
                setDataSource(music.filePath)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    currentlyPlayingItem = music
                    libraryAdapter.updatePlayingState(music.id, true)
                }
                setOnCompletionListener {
                    currentlyPlayingItem = null
                    libraryAdapter.updatePlayingState(music.id, false)
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@LibraryActivity, "Error playing music", Toast.LENGTH_SHORT).show()
                    currentlyPlayingItem = null
                    libraryAdapter.updatePlayingState(music.id, false)
                    true
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error playing music: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun shareMusic(music: GeneratedMusic) {
        // TODO: Implement music sharing functionality
        Toast.makeText(this, "Share functionality coming soon!", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}

