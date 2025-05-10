package com.example.notesapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.notesapp.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Load and display notes from Firestore here.
    }
}
