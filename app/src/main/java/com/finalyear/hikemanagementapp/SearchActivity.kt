package com.finalyear.hikemanagementapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finalyear.hikemanagementapp.adapter.HikeAdapter
import com.finalyear.hikemanagementapp.data.Hike
import com.finalyear.hikemanagementapp.data.HikeDatabase
import com.finalyear.hikemanagementapp.databinding.ActivitySearchBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity for searching hikes with various filter criteria.
 * Supports simple search by name/location and advanced search with
 * filters for location, length range, and date range.
 */
class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var database: HikeDatabase
    private lateinit var hikeAdapter: HikeAdapter
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private var fromDate: Long? = null
    private var toDate: Long? = null

    /**
     * Called when the activity is first created.
     * Initializes the UI components, sets up the RecyclerView for search results,
     * configures date pickers, and sets up click listeners for search and clear buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *        being shut down, this contains the most recent data. Otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Search Hikes"

        database = HikeDatabase.getDatabase(this)

        hikeAdapter = HikeAdapter { hike ->
            val intent = Intent(this, HikeDetailActivity::class.java)
            intent.putExtra("hike_id", hike.id)
            startActivity(intent)
        }

        binding.recyclerViewSearchResults.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSearchResults.adapter = hikeAdapter

        setupDatePickers()

        binding.editTextSearchName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSimpleSearch()
                true
            } else {
                false
            }
        }

        binding.buttonSearch.setOnClickListener {
            performAdvancedSearch()
        }

        binding.buttonClearSearch.setOnClickListener {
            clearSearch()
        }
    }

    /**
     * Sets up the date picker dialogs for the from and to date fields.
     * When a date is selected, updates the corresponding timestamp and text field.
     * For the end date, sets time to 23:59:59 to include the entire day in the range.
     */
    private fun setupDatePickers() {
        binding.editTextFromDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth)
                    fromDate = cal.timeInMillis
                    binding.editTextFromDate.setText(dateFormat.format(Date(fromDate!!)))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.editTextToDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    toDate = cal.timeInMillis
                    binding.editTextToDate.setText(dateFormat.format(Date(toDate!!)))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    /**
     * Performs a simple search by hike name or location.
     * Searches for hikes where the name or location contains the search query.
     * Shows a toast message if no results are found or if search term is empty.
     */
    private fun performSimpleSearch() {
        val searchQuery = binding.editTextSearchName.text.toString().trim()
        if (searchQuery.isEmpty()) {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val results = database.hikeDao().searchHikesByNameOrLocation("%$searchQuery%")
            if (results.isEmpty()) {
                Toast.makeText(this@SearchActivity, "No hikes found", Toast.LENGTH_SHORT).show()
            }
            hikeAdapter.submitList(results)
        }
    }

    /**
     * Performs an advanced search with multiple filter criteria.
     * Filters hikes by name, location, length range (min/max), and date range.
     * Empty filter fields are treated as wildcards (no restriction).
     * Shows a toast message with the number of results found.
     */
    private fun performAdvancedSearch() {
        val nameQuery = binding.editTextSearchName.text.toString().trim()
        val locationQuery = binding.editTextSearchLocation.text.toString().trim()
        val minLengthText = binding.editTextMinLength.text.toString().trim()
        val maxLengthText = binding.editTextMaxLength.text.toString().trim()

        val namePattern = if (nameQuery.isNotEmpty()) "%$nameQuery%" else "%"
        val locationPattern = if (locationQuery.isNotEmpty()) "%$locationQuery%" else "%"
        val minLength = minLengthText.toDoubleOrNull() ?: 0.0
        val maxLength = maxLengthText.toDoubleOrNull() ?: Double.MAX_VALUE
        val minDate = fromDate ?: 0L
        val maxDate = toDate ?: Long.MAX_VALUE

        lifecycleScope.launch {
            val results = database.hikeDao().advancedSearch(
                nameQuery = namePattern,
                locationQuery = locationPattern,
                minLength = minLength,
                maxLength = maxLength,
                minDate = minDate,
                maxDate = maxDate
            )
            
            if (results.isEmpty()) {
                Toast.makeText(this@SearchActivity, "No hikes found", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@SearchActivity, "Found ${results.size} hike(s)", Toast.LENGTH_SHORT).show()
            }
            hikeAdapter.submitList(results)
        }
    }

    /**
     * Clears all search fields and resets the search results.
     * Resets text fields to empty strings, clears date selections,
     * and clears the results list in the adapter.
     */
    private fun clearSearch() {
        binding.editTextSearchName.setText("")
        binding.editTextSearchLocation.setText("")
        binding.editTextMinLength.setText("")
        binding.editTextMaxLength.setText("")
        binding.editTextFromDate.setText("")
        binding.editTextToDate.setText("")
        fromDate = null
        toDate = null
        hikeAdapter.submitList(emptyList())
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

