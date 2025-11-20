package com.finalyear.hikemanagementapp

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finalyear.hikemanagementapp.adapter.PhotoThumbnailAdapter
import com.finalyear.hikemanagementapp.data.HikeDatabase
import com.finalyear.hikemanagementapp.data.Observation
import com.finalyear.hikemanagementapp.data.PhotoStorage
import com.finalyear.hikemanagementapp.databinding.ActivityObservationFormBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ObservationFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObservationFormBinding
    private lateinit var database: HikeDatabase
    private lateinit var photoStorage: PhotoStorage
    private lateinit var photoAdapter: PhotoThumbnailAdapter
    
    private var hikeId: Long = -1
    private var observationId: Long? = null
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    private var selectedDateTime: Long = System.currentTimeMillis()
    
    private val photoUris = mutableListOf<String>()
    private var tempPhotoUri: Uri? = null
    
    // Camera permission launcher
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, R.string.camera_permission_rationale, Toast.LENGTH_SHORT).show()
        }
    }
    
    // Gallery permission launcher
    private val requestGalleryPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchGallery()
        } else {
            Toast.makeText(this, R.string.gallery_permission_rationale, Toast.LENGTH_SHORT).show()
        }
    }
    
    // Camera launcher
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            lifecycleScope.launch {
                photoStorage.savePhoto(tempPhotoUri!!)?.let { savedPath ->
                    photoUris.add(savedPath)
                    updatePhotoGrid()
                }
            }
        }
    }
    
    // Gallery launcher
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                photoStorage.savePhoto(it)?.let { savedPath ->
                    photoUris.add(savedPath)
                    updatePhotoGrid()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObservationFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Observation"

        database = HikeDatabase.getDatabase(this)
        photoStorage = PhotoStorage(this)
        
        hikeId = intent.getLongExtra("hike_id", -1)
        observationId = intent.getLongExtra("observation_id", -1).takeIf { it != -1L }

        if (hikeId == -1L) {
            Toast.makeText(this, "Invalid hike ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupPhotoAdapter()
        loadHikeName()
        setupDateTimePicker()
        setupPhotoButtons()

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
    
    private fun setupPhotoAdapter() {
        photoAdapter = PhotoThumbnailAdapter(
            onRemoveClick = { photoPath ->
                showRemovePhotoDialog(photoPath)
            }
        )
        
        binding.recyclerViewPhotos.apply {
            layoutManager = LinearLayoutManager(this@ObservationFormActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }
    }
    
    private fun setupPhotoButtons() {
        binding.buttonTakePhoto.setOnClickListener {
            checkCameraPermissionAndTakePhoto()
        }
        
        binding.buttonChoosePhoto.setOnClickListener {
            checkGalleryPermissionAndPick()
        }
    }
    
    private fun checkCameraPermissionAndTakePhoto() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AlertDialog.Builder(this)
                    .setTitle("Camera Permission")
                    .setMessage(R.string.camera_permission_rationale)
                    .setPositiveButton("OK") { _, _ ->
                        requestCameraPermission.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            else -> {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun checkGalleryPermissionAndPick() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED -> {
                launchGallery()
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && 
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                launchGallery()
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                launchGallery()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                AlertDialog.Builder(this)
                    .setTitle("Gallery Permission")
                    .setMessage(R.string.gallery_permission_rationale)
                    .setPositiveButton("OK") { _, _ ->
                        requestGalleryPermission.launch(permission)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            else -> {
                requestGalleryPermission.launch(permission)
            }
        }
    }
    
    private fun launchCamera() {
        val photoFile = photoStorage.createTempPhotoFile()
        tempPhotoUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )
        takePicture.launch(tempPhotoUri)
    }
    
    private fun launchGallery() {
        pickImage.launch("image/*")
    }
    
    private fun updatePhotoGrid() {
        photoAdapter.submitList(photoUris.toList())
        binding.recyclerViewPhotos.visibility = if (photoUris.isEmpty()) View.GONE else View.VISIBLE
    }
    
    private fun showRemovePhotoDialog(photoPath: String) {
        AlertDialog.Builder(this)
            .setTitle("Remove Photo")
            .setMessage("Remove this photo from observation?")
            .setPositiveButton("Remove") { _, _ ->
                photoUris.remove(photoPath)
                updatePhotoGrid()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
                comments = comments,
                photoUris = photoUris.takeIf { it.isNotEmpty() }
            )
        } else {
            Observation(
                hikeId = hikeId,
                observation = observationText,
                observedAt = selectedDateTime,
                comments = comments,
                photoUris = photoUris.takeIf { it.isNotEmpty() }
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
                    observation.photoUris?.let { 
                        photoUris.addAll(it)
                        updatePhotoGrid()
                    }
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

