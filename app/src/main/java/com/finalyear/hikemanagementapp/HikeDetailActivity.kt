package com.finalyear.hikemanagementapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finalyear.hikemanagementapp.adapter.ObservationAdapter
import com.finalyear.hikemanagementapp.data.Hike
import com.finalyear.hikemanagementapp.data.HikeDatabase
import com.finalyear.hikemanagementapp.data.Observation
import com.finalyear.hikemanagementapp.databinding.ActivityHikeDetailBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity for displaying detailed information about a hike.
 * Shows all hike attributes including photo and location, lists associated observations,
 * and provides options to edit, delete, add observations, or view the hike on a map.
 */
class HikeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHikeDetailBinding
    private lateinit var database: HikeDatabase
    private var hikeId: Long = -1
    private lateinit var observationAdapter: ObservationAdapter
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    /**
     * Called when the activity is first created.
     * Initializes the UI components, sets up the observation adapter,
     * configures button click listeners, and loads hike and observation data.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *        being shut down, this contains the most recent data. Otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHikeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Hike Details"

        database = HikeDatabase.getDatabase(this)
        hikeId = intent.getLongExtra("hike_id", -1)

        if (hikeId == -1L) {
            Toast.makeText(this, "Invalid hike ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        observationAdapter = ObservationAdapter(
            onEditClick = { observation ->
                val intent = Intent(this, ObservationFormActivity::class.java)
                intent.putExtra("observation_id", observation.id)
                intent.putExtra("hike_id", hikeId)
                startActivity(intent)
            },
            onDeleteClick = { observation ->
                showDeleteObservationDialog(observation)
            }
        )

        binding.recyclerViewObservations.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewObservations.adapter = observationAdapter

        binding.buttonAddObservation.setOnClickListener {
            val intent = Intent(this, ObservationFormActivity::class.java)
            intent.putExtra("hike_id", hikeId)
            startActivity(intent)
        }

        binding.buttonViewObservations.setOnClickListener {
            // Scroll to observations or expand them
            binding.recyclerViewObservations.smoothScrollToPosition(0)
        }

        binding.buttonEdit.setOnClickListener {
            val intent = Intent(this, HikeFormActivity::class.java)
            intent.putExtra("hike_id", hikeId)
            startActivity(intent)
        }

        binding.buttonDelete.setOnClickListener {
            showDeleteHikeDialog()
        }

        binding.buttonViewMap.setOnClickListener {
            lifecycleScope.launch {
                database.hikeDao().getHikeById(hikeId)?.let { hike ->
                    if (hike.latitude != null && hike.longitude != null) {
                        val intent = Intent(this@HikeDetailActivity, MapActivity::class.java)
                        intent.putExtra("hike_id", hikeId)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@HikeDetailActivity, "No location data available", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        loadHike()
        loadObservations()
    }

    /**
     * Loads the hike data from the database.
     * Retrieves the hike by ID and displays it using displayHike.
     */
    private fun loadHike() {
        lifecycleScope.launch {
            database.hikeDao().getHikeById(hikeId)?.let { hike ->
                displayHike(hike)
            }
        }
    }

    /**
     * Displays the hike information in the UI.
     * Populates all text views with hike data and shows optional fields
     * only if they have values. Displays the photo if available and
     * shows the map button if location coordinates exist.
     *
     * @param hike The Hike object containing the data to display.
     */
    private fun displayHike(hike: Hike) {
        binding.textViewDetailName.text = hike.name
        binding.textViewDetailLocation.text = "Location: ${hike.location}"
        binding.textViewDetailDate.text = "Date: ${dateFormat.format(Date(hike.date))}"
        binding.textViewDetailParking.text = "Parking: ${if (hike.parkingAvailable) "Yes" else "No"}"
        binding.textViewDetailLength.text = "Length: ${hike.length} km"
        binding.textViewDetailDifficulty.text = "Difficulty: ${hike.difficulty}"

        hike.description?.let {
            binding.textViewDetailDescription.text = "Description: $it"
            binding.textViewDetailDescription.visibility = View.VISIBLE
        }

        hike.weather?.let {
            binding.textViewDetailWeather.text = "Weather: $it"
            binding.textViewDetailWeather.visibility = View.VISIBLE
        }

        hike.groupSize?.let {
            binding.textViewDetailGroupSize.text = "Group Size: $it"
            binding.textViewDetailGroupSize.visibility = View.VISIBLE
        }

        hike.photoPath?.let {
            val file = File(it)
            if (file.exists()) {
                BitmapFactory.decodeFile(it)?.let { bitmap ->
                    binding.imageViewDetailPhoto.setImageBitmap(bitmap)
                    binding.imageViewDetailPhoto.visibility = View.VISIBLE
                }
            }
        }

        if (hike.latitude != null && hike.longitude != null) {
            binding.buttonViewMap.visibility = View.VISIBLE
        }
    }

    /**
     * Loads observations for the current hike from the database.
     * Uses a Flow to continuously update the adapter when observations change.
     */
    private fun loadObservations() {
        lifecycleScope.launch {
            database.observationDao().getObservationsForHike(hikeId).collectLatest { observations ->
                observationAdapter.submitList(observations)
            }
        }
    }

    /**
     * Shows a confirmation dialog for deleting the hike.
     * When confirmed, deletes the hike from the database (which also cascades
     * to delete all associated observations) and finishes the activity.
     */
    private fun showDeleteHikeDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Hike")
            .setMessage("Are you sure you want to delete this hike? This will also delete all observations.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    database.hikeDao().getHikeById(hikeId)?.let { hike ->
                        database.hikeDao().deleteHike(hike)
                        finish()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows a confirmation dialog for deleting an observation.
     * When confirmed, deletes the observation from the database.
     *
     * @param observation The observation to delete.
     */
    private fun showDeleteObservationDialog(observation: Observation) {
        AlertDialog.Builder(this)
            .setTitle("Delete Observation")
            .setMessage("Are you sure you want to delete this observation?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    database.observationDao().deleteObservation(observation)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Called when the activity resumes from a paused state.
     * Reloads the hike and observations to ensure data is up-to-date
     * after returning from edit activities.
     */
    override fun onResume() {
        super.onResume()
        loadHike()
        loadObservations()
    }

    /**
     * Handles the up navigation button press in the action bar.
     * Finishes the activity and returns to the previous screen.
     *
     * @return true to indicate the navigation was handled.
     */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

