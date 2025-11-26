package com.example.liftium.data.repository

import android.util.Log
import com.example.liftium.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneOffset

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = firestore.collection("users")
    
    companion object {
        private const val TAG = "UserRepository"
    }
    
    /**
     * Creates or updates a user document in Firestore
     */
    suspend fun createOrUpdateUser(userId: String, email: String, userName: String): Result<User> {
        return try {
            val user = User(
                id = userId,
                email = email,
                userName = userName,
                createdAt = LocalDateTime.now()
            )
            
            val userMap = hashMapOf(
                "id" to user.id,
                "email" to user.email,
                "userName" to user.userName,
                "createdAt" to user.createdAt.toEpochSecond(ZoneOffset.UTC)
            )
            
            usersCollection.document(userId).set(userMap).await()
            Log.d(TAG, "User created/updated successfully: $userId")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating/updating user", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets a user from Firestore by their ID
     */
    suspend fun getUser(userId: String): Result<User?> {
        return try {
            val document = usersCollection.document(userId).get().await()
            
            if (document.exists()) {
                val user = User(
                    id = document.getString("id") ?: userId,
                    email = document.getString("email") ?: "",
                    userName = document.getString("userName") ?: "",
                    createdAt = document.getLong("createdAt")?.let {
                        LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
                    } ?: LocalDateTime.now()
                )
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user", e)
            Result.failure(e)
        }
    }
    
    /**
     * Gets the currently authenticated user's data
     */
    suspend fun getCurrentUser(): Result<User?> {
        val currentUserId = auth.currentUser?.uid
        return if (currentUserId != null) {
            getUser(currentUserId)
        } else {
            Result.success(null)
        }
    }
}

