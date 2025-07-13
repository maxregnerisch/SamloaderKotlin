package com.aimusicgenerator.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aimusicgenerator.R
import com.aimusicgenerator.databinding.ItemInstrumentBinding
import com.aimusicgenerator.model.Instrument

class InstrumentAdapter(
    private val onInstrumentClick: (Instrument) -> Unit
) : ListAdapter<Instrument, InstrumentAdapter.InstrumentViewHolder>(InstrumentDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstrumentViewHolder {
        val binding = ItemInstrumentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InstrumentViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: InstrumentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class InstrumentViewHolder(
        private val binding: ItemInstrumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(instrument: Instrument) {
            binding.apply {
                tvInstrumentName.text = instrument.name
                tvInstrumentCategory.text = instrument.category
                
                // Update selection state
                val backgroundColor = if (instrument.isSelected) {
                    ContextCompat.getColor(root.context, R.color.instrument_selected)
                } else {
                    ContextCompat.getColor(root.context, R.color.instrument_unselected)
                }
                cardInstrument.setCardBackgroundColor(backgroundColor)
                
                val textColor = if (instrument.isSelected) {
                    ContextCompat.getColor(root.context, R.color.white)
                } else {
                    ContextCompat.getColor(root.context, R.color.black)
                }
                tvInstrumentName.setTextColor(textColor)
                tvInstrumentCategory.setTextColor(textColor)
                
                // Show selection indicator
                ivSelectionIndicator.visibility = if (instrument.isSelected) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
                
                root.setOnClickListener {
                    onInstrumentClick(instrument)
                }
            }
        }
    }
    
    private class InstrumentDiffCallback : DiffUtil.ItemCallback<Instrument>() {
        override fun areItemsTheSame(oldItem: Instrument, newItem: Instrument): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Instrument, newItem: Instrument): Boolean {
            return oldItem == newItem
        }
    }
}

