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

/**
 * Activity for creating and editing hike entries.
 * Allows users to input hike details including name, location, date, parking availability,
 * length, difficulty, and optional fields like description, weather, and group size.
 * Supports capturing photos and getting current GPS location.
 */
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

    /**
     * Activity result launcher for capturing photos.
     * Handles the result from the camera intent and saves the captured photo.
     */
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                savePhoto(it)
            }
        }
    }

    /**
     * Activity result launcher for requesting permissions.
     * Handles the result of camera and location permission requests.
     */
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

    /**
     * Called when the activity is first created.
     * Initializes the UI components, sets up spinners and date picker,
     * configures button click listeners, and loads existing hike data if editing.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *        being shut down, this contains the most recent data. Otherwise, it is null.
     */
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

    /**
     * Sets up click listeners for parking and difficulty fields as dropdown selectors.
     * Shows a selection dialog when the field is clicked.
     */
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

    /**
     * Sets up the date picker dialog for the hike date field.
     * Shows a DatePickerDialog when the date field is clicked and updates
     * the selected date and text field when a date is chosen.
     */
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

    /**
     * Shows a selection dialog with a list of options.
     *
     * @param title The title to display in the dialog.
     * @param options Array of options to display.
     * @param onSelected Callback function called when an option is selected.
     */
    private fun showSelectionDialog(title: String, options: Array<String>, onSelected: (String) -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(options) { _, which ->
                onSelected(options[which])
            }
            .show()
    }

    /**
     * Requests camera permission if not already granted.
     * If permission is granted, proceeds to take a photo.
     * Otherwise, requests the permission from the user.
     */
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

    /**
     * Launches the camera intent to capture a photo.
     * The result is handled by takePictureLauncher.
     */
    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }

    /**
     * Saves the captured photo bitmap to external storage.
     * Creates an images directory if it doesn't exist and saves the photo as a JPEG file.
     * Updates the photoPath and displays the image in the preview.
     *
     * @param bitmap The bitmap image to save.
     */
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

    /**
     * Requests fine location permission if not already granted.
     * If permission is granted, proceeds to get the current location.
     * Otherwise, requests the permission from the user.
     */
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

    /**
     * Gets the current GPS location using the fused location provider.
     * Attempts to reverse geocode the coordinates to get an address.
     * Updates the location field, latitude, and longitude with the result.
     * Shows a toast message indicating success or failure.
     */
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

    /**
     * Validates the required input fields in the hike form.
     * Checks that name, location, parking, length, and difficulty are not empty,
     * and that length is a valid number.
     *
     * @return true if all required fields are valid, false otherwise.
     */
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

    /**
     * Saves the hike to the database.
     * Creates a new Hike object from form fields and either inserts or updates it
     * based on whether hikeId is set. Finishes the activity after successful save.
     */
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

    /**
     * Shows a preview dialog with all the hike details before saving.
     * Displays a summary of all entered information and offers options
     * to save or continue editing.
     */
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

    /**
     * Loads an existing hike from the database for editing.
     * Populates all form fields with the hike data, displays the photo if available,
     * and updates the action bar title to indicate edit mode.
     */
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

