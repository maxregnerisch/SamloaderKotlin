package com.aimusicgenerator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aimusicgenerator.R
import com.aimusicgenerator.databinding.ItemGenreBinding
import com.aimusicgenerator.model.Genre

class GenreAdapter(
    private val onGenreClick: (Genre) -> Unit
) : ListAdapter<Genre, GenreAdapter.GenreViewHolder>(GenreDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = ItemGenreBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GenreViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class GenreViewHolder(
        private val binding: ItemGenreBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(genre: Genre) {
            binding.apply {
                tvGenreName.text = genre.name
                tvGenreDescription.text = genre.description
                
                // Update selection state
                val backgroundColor = if (genre.isSelected) {
                    ContextCompat.getColor(root.context, R.color.genre_selected)
                } else {
                    ContextCompat.getColor(root.context, R.color.genre_unselected)
                }
                cardGenre.setCardBackgroundColor(backgroundColor)
                
                val textColor = if (genre.isSelected) {
                    ContextCompat.getColor(root.context, R.color.white)
                } else {
                    ContextCompat.getColor(root.context, R.color.black)
                }
                tvGenreName.setTextColor(textColor)
                tvGenreDescription.setTextColor(textColor)
                
                root.setOnClickListener {
                    onGenreClick(genre)
                }
            }
        }
    }
    
    private class GenreDiffCallback : DiffUtil.ItemCallback<Genre>() {
        override fun areItemsTheSame(oldItem: Genre, newItem: Genre): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Genre, newItem: Genre): Boolean {
            return oldItem == newItem
        }
    }
}

