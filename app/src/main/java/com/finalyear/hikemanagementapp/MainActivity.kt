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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: HikeDatabase
    private lateinit var hikeAdapter: HikeAdapter

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

    private fun loadHikes() {
        lifecycleScope.launch {
            database.hikeDao().getAllHikes().collectLatest { hikes ->
                hikeAdapter.submitList(hikes)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

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

    override fun onResume() {
        super.onResume()
        loadHikes()
    }
}
