package com.finalyear.hikemanagementapp.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for managing photo storage in the app-specific external files directory
 */
class PhotoStorage(private val context: Context) {
    
    private val photoDirectory: File
        get() = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: context.filesDir
    
    /**
     * Save a photo from URI to app-specific storage
     * @param sourceUri The source URI of the photo (from camera or gallery)
     * @return The file path of the saved photo, or null if save failed
     */
    suspend fun savePhoto(sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            // Generate unique filename
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "OBS_${timestamp}_${UUID.randomUUID().toString().substring(0, 8)}.jpg"
            val destinationFile = File(photoDirectory, filename)
            
            // Ensure directory exists
            if (!photoDirectory.exists()) {
                photoDirectory.mkdirs()
            }
            
            // Copy and compress image
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                val bitmap = BitmapFactory.decodeStream(input)
                
                // Compress and save
                FileOutputStream(destinationFile).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                }
                
                bitmap.recycle()
            }
            
            destinationFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Save a bitmap directly to storage
     * @param bitmap The bitmap to save
     * @return The file path of the saved photo, or null if save failed
     */
    suspend fun saveBitmap(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "OBS_${timestamp}_${UUID.randomUUID().toString().substring(0, 8)}.jpg"
            val destinationFile = File(photoDirectory, filename)
            
            if (!photoDirectory.exists()) {
                photoDirectory.mkdirs()
            }
            
            FileOutputStream(destinationFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
            }
            
            destinationFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Delete a photo from storage
     * @param photoPath The absolute path of the photo to delete
     * @return true if deletion was successful, false otherwise
     */
    suspend fun deletePhoto(photoPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(photoPath)
            file.exists() && file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Delete multiple photos from storage
     * @param photoPaths List of absolute paths to delete
     */
    suspend fun deletePhotos(photoPaths: List<String>) = withContext(Dispatchers.IO) {
        photoPaths.forEach { path ->
            try {
                File(path).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Create a temporary file for camera capture
     * @return File object for the camera to write to
     */
    fun createTempPhotoFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "TEMP_${timestamp}.jpg"
        return File(photoDirectory, filename)
    }
    
    /**
     * Check if a photo file exists
     * @param photoPath The absolute path of the photo
     * @return true if file exists, false otherwise
     */
    fun photoExists(photoPath: String): Boolean {
        return File(photoPath).exists()
    }
}
