package com.example.notesapp

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val userId: String = "",
    val timestamp: Long = 0
) 