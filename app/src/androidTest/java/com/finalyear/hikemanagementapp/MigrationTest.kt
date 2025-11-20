package com.finalyear.hikemanagementapp

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.finalyear.hikemanagementapp.data.HikeDatabase
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    
    private val TEST_DB = "migration-test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        HikeDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            // Create a hike
            execSQL(
                "INSERT INTO hikes (name, location, date, parkingAvailable, length, difficulty) " +
                "VALUES ('Test Hike', 'Test Location', 1234567890000, 1, 5.5, 'Easy')"
            )
            
            // Create an observation without photoUris
            execSQL(
                "INSERT INTO observations (hikeId, observation, observedAt, comments) " +
                "VALUES (1, 'Test Observation', 1234567890000, 'Test Comment')"
            )
            
            close()
        }
        
        // Re-open the database with version 2 and provide MIGRATION_1_2
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true)
        
        // MigrationTestHelper automatically verifies the schema changes
        // We can query to ensure data is still present
        val cursor = db.query("SELECT * FROM observations WHERE id = 1")
        assertTrue("Observation should exist after migration", cursor.moveToFirst())
        
        // Check that photoUris column exists (it should be null for migrated data)
        val photoUrisColumnIndex = cursor.getColumnIndex("photoUris")
        assertTrue("photoUris column should exist", photoUrisColumnIndex >= 0)
        
        cursor.close()
    }
    
    @Test
    fun testDatabaseCreationWithVersion2() {
        // Test that a fresh database with version 2 can be created
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.databaseBuilder(
            context,
            HikeDatabase::class.java,
            "test-db-v2"
        ).build()
        
        assertNotNull("Database should be created", db)
        
        // Verify version
        assertEquals("Database version should be 2", 2, db.openHelper.readableDatabase.version)
        
        db.close()
        context.deleteDatabase("test-db-v2")
    }
}
