package com.example.notesapp.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.example.notesapp.model.Note

object FirebaseHelper {

    private val firestore = FirebaseFirestore.getInstance()

    fun addNote(note: Note) {
        firestore.collection("notes")
            .add(note)
            .addOnSuccessListener {
                // Handle successful note addition
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    fun deleteNote(noteId: String) {
        firestore.collection("notes").document(noteId).delete()
            .addOnSuccessListener {
                // Handle successful note deletion
            }
            .addOnFailureListener {
                // Handle error
            }
    }
}
