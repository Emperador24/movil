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
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * ViewModel for managing progress photos with Firebase Storage integration
 */
data class ProgressPhotoState(
    val photos: List<ProgressPhotoWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val errorMessage: String? = null,
    val capturedPhotoUri: Uri? = null,
    val tempPhotoFile: File? = null
)

class ProgressPhotoViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProgressPhotoState())
    val state: StateFlow<ProgressPhotoState> = _state.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private const val TAG = "ProgressPhotoViewModel"
        private const val PHOTO_DIRECTORY = "Liftium"
        private const val STORAGE_PATH = "progress_photos"
        private const val COLLECTION_NAME = "progress_photos"
    }

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    init {
        loadPhotos()
    }

    /**
     * Load photos for the current user from Firestore
     */
    fun loadPhotos() {
        val userId = currentUserId
        if (userId == null) {
            Log.w(TAG, "User not authenticated")
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "User not authenticated"
            )
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)

                Log.d(TAG, "Loading photos for user: $userId")

                val snapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val photos = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.getString("id") ?: return@mapNotNull null
                        val imagePath = doc.getString("imagePath") ?: return@mapNotNull null
                        val weight = doc.getDouble("weight")
                        val notes = doc.getString("notes")
                        val dateEpoch = doc.getLong("date") ?: return@mapNotNull null
                        val createdAtEpoch = doc.getLong("createdAt") ?: return@mapNotNull null

                        val photo = ProgressPhoto(
                            id = id,
                            userId = userId,
                            imagePath = imagePath,
                            weight = weight,
                            notes = notes,
                            date = LocalDate.ofEpochDay(dateEpoch),
                            createdAt = LocalDateTime.ofEpochSecond(createdAtEpoch, 0, ZoneOffset.UTC)
                        )

                        ProgressPhotoWithDetails(
                            photo = photo,
                            formattedDate = formatDate(photo.date),
                            formattedWeight = photo.weight?.let { "${it.toInt()} lbs" } ?: "No weight"
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing photo document: ${doc.id}", e)
                        null
                    }
                }

                // Sort by date descending (newest first)
                val sortedPhotos = photos.sortedByDescending { it.photo.date }

                _state.value = _state.value.copy(
                    photos = sortedPhotos,
                    isLoading = false
                )

                Log.d(TAG, "Loaded ${sortedPhotos.size} photos")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading photos", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load photos: ${e.message}"
                )
            }
        }
    }

    /**
     * Set the captured photo URI temporarily
     */
    fun setCapturedPhotoUri(uri: Uri?, file: File?) {
        Log.d(TAG, "Setting captured photo URI: $uri")
        _state.value = _state.value.copy(
            capturedPhotoUri = uri,
            tempPhotoFile = file
        )
    }

    /**
     * Save a new progress photo - uploads to Firebase Storage and saves metadata to Firestore
     */
    fun saveProgressPhoto(
        photoUri: Uri,
        weight: Double?,
        notes: String?,
        context: Context
    ): Boolean {
        val userId = currentUserId
        if (userId == null) {
            Log.e(TAG, "User not authenticated")
            _state.value = _state.value.copy(
                errorMessage = "User not authenticated"
            )
            return false
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isUploading = true,
                    uploadProgress = 0f,
                    errorMessage = null
                )

                Log.d(TAG, "Starting photo upload to Firebase Storage")

                // Generate unique ID for the photo
                val photoId = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis()

                // Create storage reference
                val storageRef = storage.reference
                    .child(STORAGE_PATH)
                    .child(userId)
                    .child("${photoId}_${timestamp}.jpg")

                Log.d(TAG, "Storage path: ${storageRef.path}")

                // Upload the file to Firebase Storage
                val uploadTask = storageRef.putFile(photoUri)

                // Monitor upload progress
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat()
                    _state.value = _state.value.copy(uploadProgress = progress / 100f)
                    Log.d(TAG, "Upload progress: $progress%")
                }

                // Wait for upload to complete
                uploadTask.await()

                Log.d(TAG, "Upload completed, getting download URL")

                // Get the download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()

                Log.d(TAG, "Download URL: $downloadUrl")

                // Save to device gallery (optional, for user convenience)
                val localUri = savePhotoToGallery(photoUri, context)

                // Create photo document for Firestore
                val now = LocalDate.now()
                val nowTime = LocalDateTime.now()

                val photoData = hashMapOf(
                    "id" to photoId,
                    "userId" to userId,
                    "imagePath" to downloadUrl, // Store Firebase Storage URL
                    "weight" to weight,
                    "notes" to notes,
                    "date" to now.toEpochDay(),
                    "createdAt" to nowTime.toEpochSecond(ZoneOffset.UTC)
                )

                // Save to Firestore
                firestore.collection(COLLECTION_NAME)
                    .document(photoId)
                    .set(photoData)
                    .await()

                Log.d(TAG, "Photo metadata saved to Firestore")

                // Clear temporary state
                _state.value = _state.value.copy(
                    isUploading = false,
                    uploadProgress = 0f,
                    capturedPhotoUri = null,
                    tempPhotoFile = null
                )

                // Delete temp file
                _state.value.tempPhotoFile?.delete()

                // Reload photos to show the new one
                loadPhotos()

                Log.d(TAG, "Photo saved successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error saving progress photo", e)
                _state.value = _state.value.copy(
                    isUploading = false,
                    uploadProgress = 0f,
                    errorMessage = "Failed to save photo: ${e.message}"
                )
            }
        }

        return true
    }

    /**
     * Save photo to device gallery (optional)
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

            val filename = "Liftium_${System.currentTimeMillis()}.jpg"

            val savedUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savePhotoWithMediaStore(bitmap, filename, context)
            } else {
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
     * Delete a progress photo - removes from both Firebase Storage and Firestore
     */
    fun deleteProgressPhoto(photoId: String) {
        val userId = currentUserId
        if (userId == null) {
            Log.e(TAG, "User not authenticated")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Deleting photo: $photoId")

                // Get photo document to retrieve storage path
                val doc = firestore.collection(COLLECTION_NAME)
                    .document(photoId)
                    .get()
                    .await()

                val imagePath = doc.getString("imagePath")

                // Delete from Firebase Storage if path exists
                if (imagePath != null && imagePath.startsWith("https://")) {
                    try {
                        val storageRef = storage.getReferenceFromUrl(imagePath)
                        storageRef.delete().await()
                        Log.d(TAG, "Photo deleted from Storage")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deleting from Storage (continuing anyway)", e)
                    }
                }

                // Delete from Firestore
                firestore.collection(COLLECTION_NAME)
                    .document(photoId)
                    .delete()
                    .await()

                Log.d(TAG, "Photo deleted from Firestore")

                // Reload photos
                loadPhotos()

            } catch (e: Exception) {
                Log.e(TAG, "Error deleting photo", e)
                _state.value = _state.value.copy(
                    errorMessage = "Failed to delete photo: ${e.message}"
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    /**
     * Clear captured photo URI
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
     */
    fun getTotalPhotos(): Int = _state.value.photos.size

    /**
     * Get latest photo
     */
    fun getLatestPhoto(): ProgressPhoto? = _state.value.photos.firstOrNull()?.photo
}
