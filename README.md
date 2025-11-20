# Hike Management App

An Android application for managing hiking trips with features for tracking hikes, observations, and locations.

## Features

### Part A - Hike Entry (10%)
- **Required Fields:**
  - Name of hike
  - Location
  - Date of the hike
  - Parking available (Yes/No)
  - Length of hike (km)
  - Level of difficulty (Easy/Moderate/Hard)
  
- **Optional Fields:**
  - Description
  - Weather conditions (custom field)
  - Group size (custom field)

- Input validation with error messages for required fields
- Preview/confirmation dialog before saving
- Uses appropriate Android controls (DatePicker, dropdowns, etc.)

### Part B - Database Operations (15%)
- SQLite database using Room persistence library
- View all hikes in a list
- Create new hikes
- Edit existing hikes
- Delete individual hikes
- Reset database (delete all hikes)
- All data persisted locally on device

### Part C - Observations (15%)
- Add observations to hikes
- Required fields:
  - Observation text
  - Time of observation (defaults to current date/time)
- Optional fields:
  - Additional comments
  - Photo attachments (multiple photos per observation)
- Multiple observations per hike
- View all observations for a hike
- Edit observations
- Delete observations
- All observations stored in SQLite database
- **Photo Attachments:**
  - Take photos with camera or select from gallery
  - Multiple photos per observation
  - Thumbnails displayed in observation list
  - Full-screen photo viewer with swipe and zoom
  - Photos stored in app-specific external files directory

### Part D - Search (10%)
- Simple search by name (searches as you type)
- Advanced search with multiple criteria:
  - Name
  - Location
  - Length (min/max)
  - Date range (from/to)
- Search results displayed in a list
- Click on search result to view full hike details

### Part G - Additional Features (10%)
- **Camera Integration:** Take photos and attach to hikes
- **Location Services:** Automatic location detection using GPS
- **OpenStreetMap Integration (osmdroid):** View hike location on an interactive map without external API keys
- Photos stored locally on device
- Location coordinates (latitude/longitude) stored with hikes

## Setup Instructions

### Prerequisites
- Android Studio (latest version)
- Android SDK (API 33 or higher)

### Installation

1. Clone or download this project

2. Open the project in Android Studio

3. **Sync Gradle:**
   - Click "Sync Now" when prompted
   - Wait for dependencies to download

4. **Run the app:**
   - Connect an Android device or start an emulator
   - Click "Run" button or press Shift+F10

### Permissions

The app requires the following permissions:
- **Camera:** For taking photos of hikes and observations
- **Location:** For automatic location detection
- **Internet & Network State:** For downloading OpenStreetMap tiles
- **Storage (Android 12 and below):** For saving photos
- **READ_MEDIA_IMAGES (Android 13+):** For accessing photos from gallery

These permissions are requested at runtime when needed.

## Project Structure

```
app/src/main/java/com/finalyear/hikemanagementapp/
├── data/
│   ├── Hike.kt                 # Hike entity
│   ├── Observation.kt          # Observation entity
│   ├── HikeDao.kt              # Hike database access
│   ├── ObservationDao.kt       # Observation database access
│   └── HikeDatabase.kt         # Room database
├── adapter/
│   ├── HikeAdapter.kt          # RecyclerView adapter for hikes
│   └── ObservationAdapter.kt   # RecyclerView adapter for observations
├── MainActivity.kt             # Main activity (hike list)
├── HikeFormActivity.kt         # Hike entry/edit form
├── HikeDetailActivity.kt       # Hike details view
├── ObservationFormActivity.kt  # Observation entry/edit form with photo support
├── SearchActivity.kt           # Search functionality
├── MapActivity.kt              # OpenStreetMap (osmdroid) integration
└── PhotoViewerActivity.kt      # Full-screen photo viewer
```

## Database Schema

### Hikes Table
- `id` (Primary Key)
- `name` (Required)
- `location` (Required)
- `date` (Required, timestamp)
- `parkingAvailable` (Required, boolean)
- `length` (Required, double)
- `difficulty` (Required, string)
- `description` (Optional)
- `weather` (Optional, custom field)
- `groupSize` (Optional, custom field)
- `photoPath` (Optional, for storing photo path)
- `latitude` (Optional, for location)
- `longitude` (Optional, for location)

### Observations Table
- `id` (Primary Key)
- `hikeId` (Foreign Key, references hikes.id)
- `observation` (Required)
- `observedAt` (Required, timestamp)
- `comments` (Optional)
- `photoUris` (Optional, comma-separated list of photo file paths)

## Usage

1. **Add a Hike:**
   - Click the floating action button (+) on the main screen
   - Fill in required fields (marked with *)
   - Optionally add description, weather, group size
   - Optionally take a photo
   - Optionally get current location
   - Click "Save Hike" or "Preview" to review before saving

2. **View Hikes:**
   - All hikes are displayed on the main screen
   - Click on a hike to view details

3. **Edit/Delete Hike:**
   - Open hike details
   - Click "Edit Hike" to modify
   - Click "Delete Hike" to remove

4. **Add Observation:**
   - Open hike details
   - Click "Add Observation"
   - Enter observation details
   - Optionally add photos (camera or gallery)
   - Tap photo thumbnails to view full-screen
   - Save

5. **View and Manage Photos:**
   - In observation form: thumbnails shown horizontally
   - In observation list: first photo shown as thumbnail
   - Tap any photo to open full-screen viewer
   - In viewer: swipe left/right, pinch to zoom
   - Remove photos in edit mode

6. **Search:**
   - Click search icon in menu
   - Enter search terms
   - Use advanced search for multiple criteria
   - Click on result to view details

7. **View on Map:**
   - Open hike details
   - Click "View on Map" (if location data available)
   - Hike location will be marked on map

## Notes

- Photos are stored in the app's external files directory
- Location services require GPS/Wi-Fi to be enabled
- OpenStreetMap tiles require an active internet connection for the first load and are cached for reuse
- All data is stored locally on the device
- Database can be reset from the menu (deletes all hikes)

## Photo Storage Details

- **Storage Location:** Photos are stored in app-specific external files directory under `Pictures/`
- **File Format:** JPEG with 85% compression
- **Naming Convention:** `OBS_YYYYMMDD_HHMMSS_[UUID].jpg`
- **Persistence:** Photo file paths are stored in the database as comma-separated strings
- **Cleanup:** Photos are automatically deleted when their associated observation is deleted
- **Migration:** Existing observations without photos are compatible with the new schema

## Database Migration

The app uses Room database version 2 with the following migration:
- **Version 1 → 2:** Adds `photoUris` column to observations table
- Existing data is preserved during migration
- New installations start with version 2

## Manual QA Testing Steps

### Camera Flow
1. Open or create an observation
2. Tap "Take Photo" button
3. Grant camera permission if prompted
4. Take a photo with the device camera
5. Photo should appear as thumbnail in horizontal list
6. Tap thumbnail to view full-screen
7. Verify swipe and zoom work in viewer
8. Save observation
9. Verify photo persists after reopening

### Gallery Flow
1. Open or create an observation
2. Tap "Choose from Gallery" button
3. Grant storage/media permission if prompted (Android 13+)
4. Select a photo from gallery
5. Photo should appear as thumbnail
6. Verify photo persists after save

### Photo Management
1. Add multiple photos to an observation
2. Tap X button on thumbnail to remove
3. Verify removed photos are deleted
4. Edit observation with photos
5. Add/remove photos and save
6. Delete observation
7. Verify all photos are cleaned up

### Permissions
1. Test on Android 13+ device: READ_MEDIA_IMAGES permission
2. Test on Android 12 and below: READ_EXTERNAL_STORAGE permission
3. Test permission denial and re-request flow
4. Verify rationale dialogs appear appropriately

## Technologies Used

- **Kotlin** - Programming language
- **Room** - SQLite database abstraction
- **Material Design Components** - UI components
- **osmdroid / OpenStreetMap** - Map functionality
- **Location Services** - GPS location
- **Camera API** - Photo capture
- **RecyclerView** - List display
- **ViewBinding** - View binding
- **Coroutines** - Asynchronous operations
- **LiveData/Flow** - Data observation

## Future Enhancements

Possible improvements:
- Export/import hike data
- Share hikes with other users
- Add weather API integration
- Track hike statistics
- Offline map support
- Social features
- Video attachments for observations
- Voice notes for observations

## License

This project is created for educational purposes.

