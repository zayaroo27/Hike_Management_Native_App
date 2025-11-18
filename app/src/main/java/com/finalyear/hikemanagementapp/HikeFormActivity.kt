package com.finalyear.hikemanagementapp

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.finalyear.hikemanagementapp.data.Hike
import com.finalyear.hikemanagementapp.data.HikeDatabase
import com.finalyear.hikemanagementapp.databinding.ActivityHikeFormBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class HikeFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHikeFormBinding
    private lateinit var database: HikeDatabase
    private var hikeId: Long? = null
    private var photoPath: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private var selectedDate: Long = System.currentTimeMillis()

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.getParcelable("data", Bitmap::class.java)
            imageBitmap?.let {
                savePhoto(it)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.CAMERA, false) -> {
                takePhoto()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }
            else -> {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHikeFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Hike"

        database = HikeDatabase.getDatabase(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        hikeId = intent.getLongExtra("hike_id", -1).takeIf { it != -1L }

        setupSpinners()
        setupDatePicker()

        if (hikeId != null) {
            loadHike()
        } else {
            binding.editTextDate.setText(dateFormat.format(Date(selectedDate)))
        }

        binding.buttonGetLocation.setOnClickListener {
            requestLocationPermission()
        }

        binding.buttonTakePhoto.setOnClickListener {
            requestCameraPermission()
        }

        binding.buttonSave.setOnClickListener {
            if (validateInput()) {
                saveHike()
            }
        }

        binding.buttonPreview.setOnClickListener {
            if (validateInput()) {
                showPreviewDialog()
            }
        }
    }

    private fun setupSpinners() {
        // Parking spinner
        val parkingOptions = arrayOf("Yes", "No")
        binding.editTextParking.setOnClickListener {
            showSelectionDialog("Parking Available", parkingOptions) { selected ->
                binding.editTextParking.setText(selected)
            }
        }

        // Difficulty spinner
        val difficultyOptions = arrayOf("Easy", "Moderate", "Hard")
        binding.editTextDifficulty.setOnClickListener {
            showSelectionDialog("Level of Difficulty", difficultyOptions) { selected ->
                binding.editTextDifficulty.setText(selected)
            }
        }
    }

    private fun setupDatePicker() {
        binding.editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.timeInMillis
                    binding.editTextDate.setText(dateFormat.format(Date(selectedDate)))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun showSelectionDialog(title: String, options: Array<String>, onSelected: (String) -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(options) { _, which ->
                onSelected(options[which])
            }
            .show()
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                takePhoto()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }

    private fun savePhoto(bitmap: Bitmap) {
        val imagesDir = File(getExternalFilesDir(null), "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val imageFile = File(imagesDir, "hike_${System.currentTimeMillis()}.jpg")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        photoPath = imageFile.absolutePath
        binding.imageViewPhoto.setImageBitmap(bitmap)
        binding.imageViewPhoto.visibility = android.view.View.VISIBLE
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                latitude = it.latitude
                longitude = it.longitude
                
                // Try to get address from coordinates
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (addresses?.isNotEmpty() == true) {
                        val address = addresses[0]
                        val locationText = "${address.getAddressLine(0) ?: ""}"
                        binding.editTextLocation.setText(locationText)
                    }
                } catch (e: Exception) {
                    binding.editTextLocation.setText("Lat: ${it.latitude}, Lng: ${it.longitude}")
                }
                
                Toast.makeText(this, "Location retrieved successfully", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInput(): Boolean {
        val name = binding.editTextHikeName.text.toString().trim()
        val location = binding.editTextLocation.text.toString().trim()
        val parking = binding.editTextParking.text.toString().trim()
        val length = binding.editTextLength.text.toString().trim()
        val difficulty = binding.editTextDifficulty.text.toString().trim()

        if (name.isEmpty()) {
            binding.editTextHikeName.error = "Name is required"
            return false
        }
        if (location.isEmpty()) {
            binding.editTextLocation.error = "Location is required"
            return false
        }
        if (parking.isEmpty() || (parking != "Yes" && parking != "No")) {
            binding.editTextParking.error = "Please select Yes or No"
            return false
        }
        if (length.isEmpty()) {
            binding.editTextLength.error = "Length is required"
            return false
        }
        try {
            length.toDouble()
        } catch (e: NumberFormatException) {
            binding.editTextLength.error = "Length must be a number"
            return false
        }
        if (difficulty.isEmpty()) {
            binding.editTextDifficulty.error = "Difficulty is required"
            return false
        }

        return true
    }

    private fun saveHike() {
        val name = binding.editTextHikeName.text.toString().trim()
        val location = binding.editTextLocation.text.toString().trim()
        val parking = binding.editTextParking.text.toString().trim() == "Yes"
        val length = binding.editTextLength.text.toString().trim().toDouble()
        val difficulty = binding.editTextDifficulty.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim().takeIf { it.isNotEmpty() }
        val weather = binding.editTextWeather.text.toString().trim().takeIf { it.isNotEmpty() }
        val groupSize = binding.editTextGroupSize.text.toString().trim().takeIf { it.isNotEmpty() }?.toIntOrNull()

        val hike = if (hikeId != null) {
            Hike(
                id = hikeId!!,
                name = name,
                location = location,
                date = selectedDate,
                parkingAvailable = parking,
                length = length,
                difficulty = difficulty,
                description = description,
                weather = weather,
                groupSize = groupSize,
                photoPath = photoPath,
                latitude = latitude,
                longitude = longitude
            )
        } else {
            Hike(
                name = name,
                location = location,
                date = selectedDate,
                parkingAvailable = parking,
                length = length,
                difficulty = difficulty,
                description = description,
                weather = weather,
                groupSize = groupSize,
                photoPath = photoPath,
                latitude = latitude,
                longitude = longitude
            )
        }

        lifecycleScope.launch {
            if (hikeId != null) {
                database.hikeDao().updateHike(hike)
            } else {
                database.hikeDao().insertHike(hike)
            }
            finish()
        }
    }

    private fun showPreviewDialog() {
        val name = binding.editTextHikeName.text.toString().trim()
        val location = binding.editTextLocation.text.toString().trim()
        val parking = binding.editTextParking.text.toString().trim()
        val length = binding.editTextLength.text.toString().trim()
        val difficulty = binding.editTextDifficulty.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val weather = binding.editTextWeather.text.toString().trim()
        val groupSize = binding.editTextGroupSize.text.toString().trim()

        val previewText = buildString {
            append("Name: $name\n")
            append("Location: $location\n")
            append("Date: ${dateFormat.format(Date(selectedDate))}\n")
            append("Parking: $parking\n")
            append("Length: $length km\n")
            append("Difficulty: $difficulty\n")
            if (description.isNotEmpty()) append("Description: $description\n")
            if (weather.isNotEmpty()) append("Weather: $weather\n")
            if (groupSize.isNotEmpty()) append("Group Size: $groupSize\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Preview Hike Details")
            .setMessage(previewText)
            .setPositiveButton("Save") { _, _ ->
                saveHike()
            }
            .setNegativeButton("Edit") { _, _ ->
                // Do nothing, user can continue editing
            }
            .show()
    }

    private fun loadHike() {
        lifecycleScope.launch {
            hikeId?.let { id ->
                database.hikeDao().getHikeById(id)?.let { hike ->
                    binding.editTextHikeName.setText(hike.name)
                    binding.editTextLocation.setText(hike.location)
                    binding.editTextDate.setText(dateFormat.format(Date(hike.date)))
                    selectedDate = hike.date
                    binding.editTextParking.setText(if (hike.parkingAvailable) "Yes" else "No")
                    binding.editTextLength.setText(hike.length.toString())
                    binding.editTextDifficulty.setText(hike.difficulty)
                    hike.description?.let { binding.editTextDescription.setText(it) }
                    hike.weather?.let { binding.editTextWeather.setText(it) }
                    hike.groupSize?.let { binding.editTextGroupSize.setText(it.toString()) }
                    hike.photoPath?.let {
                        photoPath = it
                        val file = File(it)
                        if (file.exists()) {
                            BitmapFactory.decodeFile(it)?.let { bitmap ->
                                binding.imageViewPhoto.setImageBitmap(bitmap)
                                binding.imageViewPhoto.visibility = View.VISIBLE
                            }
                        }
                    }
                    latitude = hike.latitude
                    longitude = hike.longitude
                    supportActionBar?.title = "Edit Hike"
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

