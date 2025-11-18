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
- Multiple observations per hike
- View all observations for a hike
- Edit observations
- Delete observations
- All observations stored in SQLite database

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
- **Camera:** For taking photos of hikes
- **Location:** For automatic location detection
- **Internet & Network State:** For downloading OpenStreetMap tiles
- **Storage:** For saving photos (Android 12 and below)

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
├── ObservationFormActivity.kt  # Observation entry/edit form
├── SearchActivity.kt           # Search functionality
└── MapActivity.kt              # OpenStreetMap (osmdroid) integration
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
   - Save

5. **Search:**
   - Click search icon in menu
   - Enter search terms
   - Use advanced search for multiple criteria
   - Click on result to view details

6. **View on Map:**
   - Open hike details
   - Click "View on Map" (if location data available)
   - Hike location will be marked on map

## Notes

- Photos are stored in the app's external files directory
- Location services require GPS/Wi-Fi to be enabled
- OpenStreetMap tiles require an active internet connection for the first load and are cached for reuse
- All data is stored locally on the device
- Database can be reset from the menu (deletes all hikes)

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
- Add photos to observations
- Offline map support
- Social features

## License

This project is created for educational purposes.

