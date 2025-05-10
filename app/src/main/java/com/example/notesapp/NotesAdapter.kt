package com.example.notesapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(private val onNoteClick: (Note) -> Unit) : 
    ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note, position == selectedPosition)
    }

    fun setSelectedNote(noteId: String?) {
        val previousSelected = selectedPosition
        selectedPosition = if (noteId == null) {
            RecyclerView.NO_POSITION
        } else {
            currentList.indexOfFirst { it.id == noteId }
        }
        
        if (previousSelected != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelected)
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition)
        }
    }

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = getItem(position)
                    selectedPosition = position
                    notifyDataSetChanged()
                    onNoteClick(note)
                }
            }
        }

        fun bind(note: Note, isSelected: Boolean) {
            binding.apply {
                tvTitle.text = note.title.ifEmpty { "Untitled Note" }
                tvContent.text = note.content.ifEmpty { "No content" }
                tvDate.text = formatDate(note.timestamp)
                
                // Update selection state
                root.isSelected = isSelected
                root.setBackgroundResource(
                    if (isSelected) R.drawable.note_item_selected_background
                    else R.drawable.note_item_background
                )
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    private class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
} 