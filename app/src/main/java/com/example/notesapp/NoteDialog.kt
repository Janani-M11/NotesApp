package com.example.notesapp

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import com.example.notesapp.databinding.DialogNoteBinding
import com.google.android.material.textfield.TextInputLayout

class NoteDialog(context: Context) : Dialog(context) {
    private lateinit var binding: DialogNoteBinding
    private var onSaveListener: ((String, String) -> Unit)? = null
    private var currentNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        binding.apply {
            // Set default values
            tilTitle.editText?.setText(currentNote?.title ?: "")
            tilContent.editText?.setText(currentNote?.content ?: "")
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnSave.setOnClickListener {
                val title = tilTitle.editText?.text?.toString()?.trim() ?: ""
                val content = tilContent.editText?.text?.toString()?.trim() ?: ""
                
                if (title.isNotEmpty() || content.isNotEmpty()) {
                    onSaveListener?.invoke(title, content)
                    dismiss()
                }
            }

            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    fun setNote(note: Note) {
        currentNote = note
        if (isShowing) {
            setupViews()
        }
    }

    fun setOnSaveListener(listener: (String, String) -> Unit) {
        onSaveListener = listener
    }
} 