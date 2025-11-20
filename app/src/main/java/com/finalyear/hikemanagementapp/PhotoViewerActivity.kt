package com.finalyear.hikemanagementapp

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Full-screen photo viewer with swipe and pinch-to-zoom support
 */
class PhotoViewerActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var textViewCounter: TextView
    private lateinit var photoUris: List<String>
    private var currentPhotoIndex = 0
    
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetectorCompat
    private val matrix = Matrix()
    private var scaleFactor = 1.0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f
    
    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Photo Viewer"
        
        imageView = findViewById(R.id.imageViewFullScreen)
        textViewCounter = findViewById(R.id.textViewPhotoCounter)
        
        // Get photo URIs and starting index from intent
        val photosArray = intent.getStringArrayExtra("photo_uris")
        currentPhotoIndex = intent.getIntExtra("photo_index", 0)
        
        if (photosArray == null || photosArray.isEmpty()) {
            finish()
            return
        }
        
        photoUris = photosArray.toList()
        
        // Setup gesture detectors
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        gestureDetector = GestureDetectorCompat(this, GestureListener())
        
        // Load first photo
        displayPhoto(currentPhotoIndex)
        
        // Toggle UI on tap
        imageView.setOnClickListener {
            toggleSystemUI()
        }
    }
    
    private fun displayPhoto(index: Int) {
        if (index < 0 || index >= photoUris.size) return
        
        val photoPath = photoUris[index]
        val file = File(photoPath)
        
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(photoPath)
            imageView.setImageBitmap(bitmap)
            
            // Reset transformations
            scaleFactor = 1.0f
            posX = 0f
            posY = 0f
            matrix.reset()
            imageView.imageMatrix = matrix
            
            // Update counter
            textViewCounter.text = "${index + 1} / ${photoUris.size}"
        }
        
        currentPhotoIndex = index
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleGestureDetector.isInProgress) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    
                    posX += dx
                    posY += dy
                    
                    matrix.set(imageView.imageMatrix)
                    matrix.postTranslate(dx, dy)
                    imageView.imageMatrix = matrix
                    
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
        }
        
        return true
    }
    
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(0.5f, min(scaleFactor, 5.0f))
            
            matrix.set(imageView.imageMatrix)
            matrix.postScale(
                detector.scaleFactor,
                detector.scaleFactor,
                detector.focusX,
                detector.focusY
            )
            imageView.imageMatrix = matrix
            
            return true
        }
    }
    
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y
            
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // Swipe right - previous photo
                        if (currentPhotoIndex > 0) {
                            displayPhoto(currentPhotoIndex - 1)
                        }
                    } else {
                        // Swipe left - next photo
                        if (currentPhotoIndex < photoUris.size - 1) {
                            displayPhoto(currentPhotoIndex + 1)
                        }
                    }
                    return true
                }
            }
            
            return false
        }
    }
    
    private fun toggleSystemUI() {
        val decorView = window.decorView
        val currentVisibility = decorView.systemUiVisibility
        
        if (currentVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
            // Hide UI
            decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
            supportActionBar?.hide()
            textViewCounter.visibility = View.GONE
        } else {
            // Show UI
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            supportActionBar?.show()
            textViewCounter.visibility = View.VISIBLE
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
