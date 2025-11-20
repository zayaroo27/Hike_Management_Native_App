package com.finalyear.hikemanagementapp

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.finalyear.hikemanagementapp.data.PhotoStorage
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PhotoStorageTest {
    
    private lateinit var context: Context
    private lateinit var photoStorage: PhotoStorage
    private val testPhotoPaths = mutableListOf<String>()
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        photoStorage = PhotoStorage(context)
    }
    
    @After
    fun cleanup() {
        // Clean up test photos
        runBlocking {
            photoStorage.deletePhotos(testPhotoPaths)
        }
        testPhotoPaths.clear()
    }
    
    @Test
    fun testSaveBitmap() = runBlocking {
        // Create a test bitmap
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        
        // Save bitmap
        val photoPath = photoStorage.saveBitmap(bitmap)
        
        // Verify result
        assertNotNull("Photo path should not be null", photoPath)
        photoPath?.let {
            testPhotoPaths.add(it)
            assertTrue("Photo file should exist", File(it).exists())
            assertTrue("Photo path should end with .jpg", it.endsWith(".jpg"))
        }
    }
    
    @Test
    fun testPhotoExists() = runBlocking {
        // Create and save a test bitmap
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val photoPath = photoStorage.saveBitmap(bitmap)
        
        assertNotNull("Photo path should not be null", photoPath)
        photoPath?.let {
            testPhotoPaths.add(it)
            
            // Test photoExists
            assertTrue("Photo should exist", photoStorage.photoExists(it))
            
            // Test with non-existent path
            assertFalse("Non-existent photo should return false", 
                photoStorage.photoExists("/non/existent/path.jpg"))
        }
    }
    
    @Test
    fun testDeletePhoto() = runBlocking {
        // Create and save a test bitmap
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val photoPath = photoStorage.saveBitmap(bitmap)
        
        assertNotNull("Photo path should not be null", photoPath)
        photoPath?.let {
            assertTrue("Photo should exist before deletion", File(it).exists())
            
            // Delete photo
            val deleted = photoStorage.deletePhoto(it)
            
            assertTrue("Delete should return true", deleted)
            assertFalse("Photo should not exist after deletion", File(it).exists())
        }
    }
    
    @Test
    fun testCreateTempPhotoFile() {
        val tempFile = photoStorage.createTempPhotoFile()
        
        assertNotNull("Temp file should not be null", tempFile)
        assertTrue("Temp file name should start with TEMP_", tempFile.name.startsWith("TEMP_"))
        assertTrue("Temp file name should end with .jpg", tempFile.name.endsWith(".jpg"))
        
        // Clean up
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
    
    @Test
    fun testDeletePhotos() = runBlocking {
        // Create multiple test bitmaps
        val bitmap1 = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        
        val photoPath1 = photoStorage.saveBitmap(bitmap1)
        val photoPath2 = photoStorage.saveBitmap(bitmap2)
        
        assertNotNull("Photo path 1 should not be null", photoPath1)
        assertNotNull("Photo path 2 should not be null", photoPath2)
        
        photoPath1?.let { path1 ->
            photoPath2?.let { path2 ->
                assertTrue("Photo 1 should exist", File(path1).exists())
                assertTrue("Photo 2 should exist", File(path2).exists())
                
                // Delete all photos
                photoStorage.deletePhotos(listOf(path1, path2))
                
                assertFalse("Photo 1 should not exist after deletion", File(path1).exists())
                assertFalse("Photo 2 should not exist after deletion", File(path2).exists())
            }
        }
    }
}
