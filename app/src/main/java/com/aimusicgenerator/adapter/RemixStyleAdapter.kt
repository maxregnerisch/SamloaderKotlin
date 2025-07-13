package com.aimusicgenerator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aimusicgenerator.databinding.ItemRemixStyleBinding
import com.aimusicgenerator.model.RemixStyle

class RemixStyleAdapter(
    private val onStyleSelected: (RemixStyle) -> Unit
) : ListAdapter<RemixStyle, RemixStyleAdapter.RemixStyleViewHolder>(RemixStyleDiffCallback()) {
    
    private var selectedPosition = -1
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemixStyleViewHolder {
        val binding = ItemRemixStyleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RemixStyleViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: RemixStyleViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }
    
    inner class RemixStyleViewHolder(
        private val binding: ItemRemixStyleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(style: RemixStyle, isSelected: Boolean) {
            binding.apply {
                textStyleName.text = style.name
                textStyleDescription.text = style.description
                textStyleIcon.text = style.icon
                
                // Update selection state
                cardRemixStyle.isSelected = isSelected
                cardRemixStyle.alpha = if (isSelected) 1.0f else 0.7f
                
                root.setOnClickListener {
                    val previousPosition = selectedPosition
                    selectedPosition = adapterPosition
                    
                    // Notify changes for selection state
                    if (previousPosition != -1) {
                        notifyItemChanged(previousPosition)
                    }
                    notifyItemChanged(selectedPosition)
                    
                    onStyleSelected(style)
                }
            }
        }
    }
    
    private class RemixStyleDiffCallback : DiffUtil.ItemCallback<RemixStyle>() {
        override fun areItemsTheSame(oldItem: RemixStyle, newItem: RemixStyle): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: RemixStyle, newItem: RemixStyle): Boolean {
            return oldItem == newItem
        }
    }
}

