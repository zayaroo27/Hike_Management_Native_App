package com.finalyear.hikemanagementapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.finalyear.hikemanagementapp.data.HikeDatabase
import com.finalyear.hikemanagementapp.databinding.ActivityMapBinding
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    private lateinit var database: HikeDatabase
    private lateinit var mapView: MapView
    private var hikeId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Hike Location"

        database = HikeDatabase.getDatabase(this)
        hikeId = intent.getLongExtra("hike_id", -1)

        if (hikeId == -1L) {
            Toast.makeText(this, "Invalid hike ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        loadHikeLocation()
    }

    private fun loadHikeLocation() {
        lifecycleScope.launch {
            val hike = database.hikeDao().getHikeById(hikeId)
            if (hike == null) {
                Toast.makeText(this@MapActivity, "Unable to find hike", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            val latitude = hike.latitude
            val longitude = hike.longitude

            if (latitude != null && longitude != null) {
                val geoPoint = GeoPoint(latitude, longitude)
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(geoPoint)

                mapView.overlays.clear()
                val marker = Marker(mapView).apply {
                    position = geoPoint
                    title = hike.name
                    subDescription = hike.location
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(marker)
                mapView.invalidate()
            } else {
                Toast.makeText(this@MapActivity, "No location data available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::mapView.isInitialized) {
            mapView.onResume()
        }
    }

    override fun onPause() {
        if (::mapView.isInitialized) {
            mapView.onPause()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (::mapView.isInitialized) {
            mapView.onDetach()
        }
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

