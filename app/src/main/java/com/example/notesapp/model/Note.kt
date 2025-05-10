package com.example.notesapp.model

data class Note(
    val id: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
