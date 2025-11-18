package com.finalyear.hikemanagementapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.finalyear.hikemanagementapp.data.HikeDatabase
import com.finalyear.hikemanagementapp.data.Observation
import com.finalyear.hikemanagementapp.databinding.ActivityObservationFormBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ObservationFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObservationFormBinding
    private lateinit var database: HikeDatabase
    private var hikeId: Long = -1
    private var observationId: Long? = null
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    private var selectedDateTime: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObservationFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Observation"

        database = HikeDatabase.getDatabase(this)
        hikeId = intent.getLongExtra("hike_id", -1)
        observationId = intent.getLongExtra("observation_id", -1).takeIf { it != -1L }

        if (hikeId == -1L) {
            Toast.makeText(this, "Invalid hike ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadHikeName()
        setupDateTimePicker()

        if (observationId != null) {
            loadObservation()
        } else {
            binding.editTextObservedAt.setText(dateTimeFormat.format(Date(selectedDateTime)))
        }

        binding.buttonSaveObservation.setOnClickListener {
            if (validateInput()) {
                saveObservation()
            }
        }
    }

    private fun loadHikeName() {
        lifecycleScope.launch {
            database.hikeDao().getHikeById(hikeId)?.let { hike ->
                binding.textViewHikeName.text = hike.name
            }
        }
    }

    private fun setupDateTimePicker() {
        binding.editTextObservedAt.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDateTime

            // First show date picker
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    
                    // Then show time picker
                    TimePickerDialog(
                        this,
                        { _, hourOfDay, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)
                            selectedDateTime = calendar.timeInMillis
                            binding.editTextObservedAt.setText(dateTimeFormat.format(Date(selectedDateTime)))
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun validateInput(): Boolean {
        val observation = binding.editTextObservation.text.toString().trim()

        if (observation.isEmpty()) {
            binding.editTextObservation.error = "Observation is required"
            return false
        }

        return true
    }

    private fun saveObservation() {
        val observationText = binding.editTextObservation.text.toString().trim()
        val comments = binding.editTextComments.text.toString().trim().takeIf { it.isNotEmpty() }

        val observation = if (observationId != null) {
            Observation(
                id = observationId!!,
                hikeId = hikeId,
                observation = observationText,
                observedAt = selectedDateTime,
                comments = comments
            )
        } else {
            Observation(
                hikeId = hikeId,
                observation = observationText,
                observedAt = selectedDateTime,
                comments = comments
            )
        }

        lifecycleScope.launch {
            if (observationId != null) {
                database.observationDao().updateObservation(observation)
            } else {
                database.observationDao().insertObservation(observation)
            }
            finish()
        }
    }

    private fun loadObservation() {
        lifecycleScope.launch {
            observationId?.let { id ->
                database.observationDao().getObservationById(id)?.let { observation ->
                    binding.editTextObservation.setText(observation.observation)
                    binding.editTextObservedAt.setText(dateTimeFormat.format(Date(observation.observedAt)))
                    selectedDateTime = observation.observedAt
                    observation.comments?.let { binding.editTextComments.setText(it) }
                    supportActionBar?.title = "Edit Observation"
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

