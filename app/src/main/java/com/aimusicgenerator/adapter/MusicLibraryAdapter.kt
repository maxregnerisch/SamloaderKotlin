package com.aimusicgenerator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aimusicgenerator.databinding.ItemMusicLibraryBinding
import com.aimusicgenerator.model.GeneratedMusic
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class MusicLibraryAdapter(
    private val onPlayClick: (GeneratedMusic) -> Unit,
    private val onDeleteClick: (GeneratedMusic) -> Unit,
    private val onShareClick: (GeneratedMusic) -> Unit
) : ListAdapter<GeneratedMusic, MusicLibraryAdapter.MusicViewHolder>(MusicDiffCallback()) {
    
    private val playingStates = mutableMapOf<String, Boolean>()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = ItemMusicLibraryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MusicViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    fun updatePlayingState(musicId: String, isPlaying: Boolean) {
        playingStates[musicId] = isPlaying
        notifyDataSetChanged()
    }
    
    inner class MusicViewHolder(
        private val binding: ItemMusicLibraryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(music: GeneratedMusic) {
            binding.apply {
                tvMusicTitle.text = music.title
                tvMusicGenre.text = music.genre
                tvMusicDuration.text = formatDuration(music.duration)
                tvMusicTempo.text = "${music.tempo} BPM"
                
                // Parse and display instruments
                val instruments = try {
                    val type = object : TypeToken<List<String>>() {}.type
                    Gson().fromJson<List<String>>(music.instruments, type)
                } catch (e: Exception) {
                    emptyList<String>()
                }
                tvMusicInstruments.text = instruments.joinToString(", ")
                
                // Format creation date
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvMusicDate.text = dateFormat.format(Date(music.createdAt))
                
                // Update play button state
                val isPlaying = playingStates[music.id] ?: false
                btnPlay.text = if (isPlaying) "⏸️" else "▶️"
                
                // Set click listeners
                btnPlay.setOnClickListener {
                    onPlayClick(music)
                }
                
                btnDelete.setOnClickListener {
                    onDeleteClick(music)
                }
                
                btnShare.setOnClickListener {
                    onShareClick(music)
                }
            }
        }
        
        private fun formatDuration(seconds: Int): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%d:%02d", minutes, remainingSeconds)
        }
    }
    
    private class MusicDiffCallback : DiffUtil.ItemCallback<GeneratedMusic>() {
        override fun areItemsTheSame(oldItem: GeneratedMusic, newItem: GeneratedMusic): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: GeneratedMusic, newItem: GeneratedMusic): Boolean {
            return oldItem == newItem
        }
    }
}

