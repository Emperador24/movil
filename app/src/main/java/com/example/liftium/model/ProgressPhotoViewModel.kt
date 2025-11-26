package com.example.liftium.model

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * ViewModel for managing progress photos
 * 
 * Current Implementation: In-memory storage
 * Future: Will integrate with database (Room/Firebase) for persistence
 * 
 * Design considerations for future database integration:
 * - Photos list will come from database queries
 * - Add/update/delete operations will persist to database
 * - UserId will link photos to authenticated users
 * - Image paths will reference actual file storage
 */
data class ProgressPhotoState(
    val photos: List<ProgressPhotoWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val capturedPhotoUri: Uri? = null,
    val tempPhotoFile: File? = null
)

class ProgressPhotoViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(ProgressPhotoState())
    val state: StateFlow<ProgressPhotoState> = _state.asStateFlow()
    
    companion object {
        private const val TAG = "ProgressPhotoViewModel"
        private const val PHOTO_DIRECTORY = "Liftium"
    }
    
    // TODO: Replace with actual user ID from authentication
    private val currentUserId: String
        get() = "temp_user_${System.currentTimeMillis()}" // Placeholder
    
    /**
     * Load photos for the current user
     * TODO: In future, this will query from database
     */
    fun loadPhotos() {
        Log.d(TAG, "Loading photos for user: $currentUserId")
        // Current: Photos already in state (in-memory)
        // Future: Query database and update state
        _state.value = _state.value.copy(isLoading = false)
    }
    
    /**
     * Set the captured photo URI temporarily
     * This is used between camera capture and preview/save
     */
    fun setCapturedPhotoUri(uri: Uri?, file: File?) {
        Log.d(TAG, "Setting captured photo URI: $uri")
        _state.value = _state.value.copy(
            capturedPhotoUri = uri,
            tempPhotoFile = file
        )
    }
    
    /**
     * Save a new progress photo with details
     * 
     * @param photoUri Uri of the captured photo
     * @param weight User's weight (optional)
     * @param notes User's notes (optional)
     * @param context Application context for saving to gallery
     * 
     * Future: This will also save to database with userId reference
     */
    fun saveProgressPhoto(
        photoUri: Uri,
        weight: Double?,
        notes: String?,
        context: Context
    ): Boolean {
        return try {
            Log.d(TAG, "Saving progress photo: $photoUri")
            
            // Save to device gallery
            val savedUri = savePhotoToGallery(photoUri, context)
            
            if (savedUri != null) {
                // Create ProgressPhoto object
                val now = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LocalDate.now()
                } else {
                    LocalDate.now()
                }
                
                val nowTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LocalDateTime.now()
                } else {
                    LocalDateTime.now()
                }
                
                val photo = ProgressPhoto(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId, // Will be actual user ID in future
                    imagePath = savedUri.toString(),
                    weight = weight,
                    notes = notes,
                    date = now,
                    createdAt = nowTime
                )
                
                // Format data for display
                val photoWithDetails = ProgressPhotoWithDetails(
                    photo = photo,
                    formattedDate = formatDate(now),
                    formattedWeight = weight?.let { "${it.toInt()} lbs" } ?: "No weight"
                )
                
                // Add to in-memory list (current implementation)
                val updatedPhotos = _state.value.photos.toMutableList().apply {
                    add(0, photoWithDetails) // Add to beginning (most recent first)
                }
                
                _state.value = _state.value.copy(
                    photos = updatedPhotos,
                    capturedPhotoUri = null,
                    tempPhotoFile = null
                )
                
                Log.d(TAG, "Photo saved successfully. Total photos: ${updatedPhotos.size}")
                
                // TODO: In future, save to database here
                // database.progressPhotoDao().insert(photo)
                
                true
            } else {
                Log.e(TAG, "Failed to save photo to gallery")
                _state.value = _state.value.copy(
                    errorMessage = "Failed to save photo to gallery"
                )
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving progress photo", e)
            _state.value = _state.value.copy(
                errorMessage = "Error saving photo: ${e.message}"
            )
            false
        }
    }
    
    /**
     * Save photo to device gallery (Photos app)
     * Returns the Uri of the saved photo
     */
    private fun savePhotoToGallery(photoUri: Uri, context: Context): Uri? {
        return try {
            val bitmap = if (photoUri.scheme == "file") {
                BitmapFactory.decodeFile(photoUri.path)
            } else {
                context.contentResolver.openInputStream(photoUri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }
            
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI")
                return null
            }
            
            // Generate filename
            val filename = "Liftium_${System.currentTimeMillis()}.jpg"
            
            val savedUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above, use MediaStore
                savePhotoWithMediaStore(bitmap, filename, context)
            } else {
                // For older versions, save to Pictures directory
                savePhotoToFile(bitmap, filename)
            }
            
            Log.d(TAG, "Photo saved to gallery: $savedUri")
            savedUri
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving photo to gallery", e)
            null
        }
    }
    
    /**
     * Save photo using MediaStore API (Android 10+)
     */
    private fun savePhotoWithMediaStore(
        bitmap: Bitmap,
        filename: String,
        context: Context
    ): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/$PHOTO_DIRECTORY")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            }
            
            // Mark as complete
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
        }
        
        return uri
    }
    
    /**
     * Save photo to file (Android 9 and below)
     */
    private fun savePhotoToFile(bitmap: Bitmap, filename: String): Uri? {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val liftiumDir = File(picturesDir, PHOTO_DIRECTORY)
        
        if (!liftiumDir.exists()) {
            liftiumDir.mkdirs()
        }
        
        val file = File(liftiumDir, filename)
        
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
        }
        
        return Uri.fromFile(file)
    }
    
    /**
     * Delete a progress photo
     * 
     * Future: This will also delete from database
     */
    fun deleteProgressPhoto(photoId: String) {
        Log.d(TAG, "Deleting progress photo: $photoId")
        
        val updatedPhotos = _state.value.photos.filterNot { it.photo.id == photoId }
        _state.value = _state.value.copy(photos = updatedPhotos)
        
        // TODO: In future, delete from database
        // database.progressPhotoDao().delete(photoId)
    }
    
    /**
     * Clear any error messages
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
    
    /**
     * Clear captured photo URI (e.g., when user cancels)
     */
    fun clearCapturedPhoto() {
        _state.value.tempPhotoFile?.delete()
        _state.value = _state.value.copy(
            capturedPhotoUri = null,
            tempPhotoFile = null
        )
    }
    
    /**
     * Format date for display
     */
    private fun formatDate(date: LocalDate): String {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        return when (date) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> {
                val daysAgo = java.time.Period.between(date, today).days
                when {
                    daysAgo < 7 -> "$daysAgo days ago"
                    daysAgo < 30 -> "${daysAgo / 7} week${if (daysAgo / 7 > 1) "s" else ""} ago"
                    else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                }
            }
        }
    }
    
    /**
     * Get total number of photos
     * Future: This will query from database
     */
    fun getTotalPhotos(): Int = _state.value.photos.size
    
    /**
     * Get latest photo
     * Future: This will query from database
     */
    fun getLatestPhoto(): ProgressPhoto? = _state.value.photos.firstOrNull()?.photo
}

