package com.example.notesapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var fabAdd: ExtendedFloatingActionButton
    private lateinit var emptyView: LinearLayout
    private lateinit var adapter: NotesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedNoteId: String? = null
    private var originalNotes: List<Note> = emptyList()
    private var currentSortOrder = SortOrder.NEWEST
    private var currentQuery: String? = null

    enum class SortOrder {
        NEWEST, OLDEST, TITLE_ASC, TITLE_DESC
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if user is logged in
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
        setupFirestoreListener()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        fabAdd = findViewById(R.id.fabAdd)
        emptyView = findViewById(R.id.emptyView)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Notes App"
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter { note ->
            selectedNoteId = note.id
            adapter.setSelectedNote(note.id)
            showNoteDialog(note)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            loadNotes()
        }
    }

    private fun setupFab() {
        fabAdd.setOnClickListener {
            showNoteDialog()
        }
    }

    private fun setupFirestoreListener() {
        val userId = auth.currentUser?.uid ?: return
        swipeRefresh.isRefreshing = true

        try {
            db.collection("notes")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, e ->
                    swipeRefresh.isRefreshing = false
                    if (e != null) {
                        Toast.makeText(this, "Error loading notes: ${e.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    try {
                        val notes = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Note::class.java)?.copy(id = doc.id)
                        } ?: emptyList()

                        originalNotes = notes
                        applySorting()
                        updateEmptyView()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error processing notes: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            swipeRefresh.isRefreshing = false
            Toast.makeText(this, "Error setting up listener: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadNotes() {
        val userId = auth.currentUser?.uid ?: return
        swipeRefresh.isRefreshing = true

        try {
            db.collection("notes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    try {
                        originalNotes = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Note::class.java)?.copy(id = doc.id)
                        }
                        applySorting()
                        updateEmptyView()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error processing notes: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        swipeRefresh.isRefreshing = false
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading notes: ${e.message}", Toast.LENGTH_SHORT).show()
                    swipeRefresh.isRefreshing = false
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading notes: ${e.message}", Toast.LENGTH_SHORT).show()
            swipeRefresh.isRefreshing = false
        }
    }

    private fun applySorting() {
        try {
            val sortedNotes = when (currentSortOrder) {
                SortOrder.NEWEST -> originalNotes.sortedByDescending { it.timestamp }
                SortOrder.OLDEST -> originalNotes.sortedBy { it.timestamp }
                SortOrder.TITLE_ASC -> originalNotes.sortedBy { it.title.lowercase() }
                SortOrder.TITLE_DESC -> originalNotes.sortedByDescending { it.title.lowercase() }
            }
            
            if (currentQuery.isNullOrBlank()) {
                adapter.submitList(sortedNotes)
            } else {
                filterNotes(currentQuery)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error sorting notes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmptyView() {
        try {
            if (adapter.itemCount == 0) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error updating view: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNoteDialog(note: Note? = null) {
        try {
            val dialog = NoteDialog(this)
            if (note != null) {
                dialog.setNote(note)
            }
            dialog.setOnSaveListener { title, content ->
                saveNote(title, content, note?.id)
            }
            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error showing dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNote(title: String, content: String, noteId: String? = null) {
        val userId = auth.currentUser?.uid ?: return
        val timestamp = System.currentTimeMillis()
        val note = mapOf(
            "title" to title,
            "content" to content,
            "userId" to userId,
            "timestamp" to timestamp
        )

        try {
            if (noteId != null) {
                // Update existing note
                db.collection("notes").document(noteId)
                    .update(note)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
                        selectedNoteId = null
                        adapter.setSelectedNote(null)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error updating note: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Create new note
                db.collection("notes")
                    .add(note)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error adding note: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving note: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteNote(noteId: String) {
        try {
            db.collection("notes").document(noteId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                    selectedNoteId = null
                    adapter.setSelectedNote(null)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error deleting note: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error deleting note: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        
        // Setup search
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText
                filterNotes(newText)
                return true
            }
        })
        
        return true
    }

    private fun filterNotes(query: String?) {
        try {
            if (query.isNullOrBlank()) {
                applySorting()
            } else {
                val filteredNotes = originalNotes.filter { note ->
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
                }
                adapter.submitList(filteredNotes)
            }
            updateEmptyView()
        } catch (e: Exception) {
            Toast.makeText(this, "Error filtering notes: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_add -> {
                showNoteDialog()
                true
            }
            R.id.action_edit -> {
                if (selectedNoteId == null) {
                    Toast.makeText(this, "Please select a note first", Toast.LENGTH_SHORT).show()
                    return true
                }
                try {
                    selectedNoteId?.let { id ->
                        db.collection("notes").document(id).get()
                            .addOnSuccessListener { doc ->
                                doc.toObject(Note::class.java)?.let { note ->
                                    showNoteDialog(note.copy(id = doc.id))
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error loading note: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error editing note: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_delete -> {
                if (selectedNoteId == null) {
                    Toast.makeText(this, "Please select a note first", Toast.LENGTH_SHORT).show()
                    return true
                }
                try {
                    selectedNoteId?.let { id ->
                        AlertDialog.Builder(this)
                            .setTitle("Delete Note")
                            .setMessage("Are you sure you want to delete this note?")
                            .setPositiveButton("Delete") { _, _ ->
                                deleteNote(id)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error deleting note: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            R.id.action_logout -> {
                AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout") { _, _ ->
                        try {
                            auth.signOut()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error logging out: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSortDialog() {
        val options = arrayOf("Newest First", "Oldest First", "Title (A-Z)", "Title (Z-A)")
        AlertDialog.Builder(this)
            .setTitle("Sort Notes")
            .setItems(options) { _, which ->
                currentSortOrder = when (which) {
                    0 -> SortOrder.NEWEST
                    1 -> SortOrder.OLDEST
                    2 -> SortOrder.TITLE_ASC
                    3 -> SortOrder.TITLE_DESC
                    else -> SortOrder.NEWEST
                }
                applySorting()
            }
            .show()
    }
}
