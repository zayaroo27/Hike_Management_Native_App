package com.finalyear.hikemanagementapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finalyear.hikemanagementapp.adapter.HikeAdapter
import com.finalyear.hikemanagementapp.data.Hike
import com.finalyear.hikemanagementapp.data.HikeDatabase
import com.finalyear.hikemanagementapp.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Main activity of the Hike Management application.
 * Displays a list of all hikes and provides navigation to add new hikes,
 * search hikes, and reset the database.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: HikeDatabase
    private lateinit var hikeAdapter: HikeAdapter

    /**
     * Called when the activity is first created.
     * Initializes the UI components, sets up the RecyclerView with the hike adapter,
     * and configures click listeners for the floating action button.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *        being shut down, this contains the most recent data. Otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = HikeDatabase.getDatabase(this)
        
        hikeAdapter = HikeAdapter { hike ->
            val intent = Intent(this, HikeDetailActivity::class.java)
            intent.putExtra("hike_id", hike.id)
            startActivity(intent)
        }

        binding.recyclerViewHikes.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHikes.adapter = hikeAdapter

        binding.fabAddHike.setOnClickListener {
            val intent = Intent(this, HikeFormActivity::class.java)
            startActivity(intent)
        }

        loadHikes()
    }

    /**
     * Loads all hikes from the database and updates the RecyclerView adapter.
     * Uses coroutines to perform database operations asynchronously and
     * collects the Flow of hikes to update the UI when data changes.
     */
    private fun loadHikes() {
        lifecycleScope.launch {
            database.hikeDao().getAllHikes().collectLatest { hikes ->
                hikeAdapter.submitList(hikes)
            }
        }
    }

    /**
     * Inflates the options menu for the main activity.
     *
     * @param menu The options menu in which items are placed.
     * @return true to display the menu, false otherwise.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /**
     * Handles selection of menu items in the options menu.
     * Navigates to SearchActivity when search is selected,
     * or shows the reset database dialog when reset is selected.
     *
     * @param item The menu item that was selected.
     * @return true if the item was handled, false otherwise.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_reset -> {
                showResetDatabaseDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Shows a confirmation dialog for resetting the database.
     * When confirmed, deletes all hikes from the database.
     * This action cannot be undone.
     */
    private fun showResetDatabaseDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset Database")
            .setMessage("Are you sure you want to delete all hikes? This action cannot be undone.")
            .setPositiveButton("Delete All") { _, _ ->
                lifecycleScope.launch {
                    database.hikeDao().deleteAllHikes()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Called when the activity resumes from a paused state.
     * Reloads the hikes to ensure the list is up-to-date
     * after returning from other activities.
     */
    override fun onResume() {
        super.onResume()
        loadHikes()
    }
}
