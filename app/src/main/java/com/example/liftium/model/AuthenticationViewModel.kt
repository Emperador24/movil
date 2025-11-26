package com.example.liftium.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoading: Boolean = false,
    val currentUser: FirebaseUser? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val registrationSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    companion object {
        private const val TAG = "AuthViewModel"
    }

    init {
        checkAuthStatus()
        configureFirebaseAuth()
    }

    private fun configureFirebaseAuth() {
        // Configure Firebase Auth settings for better password reset functionality
        auth.useAppLanguage() // Use device language for emails
        Log.d(TAG, "Firebase Auth initialized")
    }

    private fun checkAuthStatus() {
        val currentUser = auth.currentUser
        _authState.value = _authState.value.copy(
            currentUser = currentUser,
            isAuthenticated = currentUser != null
        )
        Log.d(TAG, "Auth status checked. User authenticated: ${currentUser != null}")
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null,
                    registrationSuccess = false
                )

                val result = auth.signInWithEmailAndPassword(email, password).await()

                // Ensure user document exists in Firestore
                result.user?.let { user ->
                    val userRepository = com.example.liftium.data.repository.UserRepository()
                    val userResult = userRepository.getUser(user.uid)
                    
                    // If user doesn't exist in Firestore, create it
                    if (userResult.getOrNull() == null) {
                        userRepository.createOrUpdateUser(
                            user.uid, 
                            user.email ?: email, 
                            user.displayName ?: "User"
                        )
                    }
                }

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    currentUser = result.user,
                    isAuthenticated = true,
                    errorMessage = null,
                    successMessage = null,
                    registrationSuccess = false
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = getErrorMessage(e),
                    isAuthenticated = false
                )
            }
        }
    }

    fun createUserWithEmailAndPassword(userName: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    registrationSuccess = false
                )

                val result = auth.createUserWithEmailAndPassword(email, password).await()

                // Update user display name
                result.user?.let { user ->
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(userName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                    
                    // Create user document in Firestore
                    val userRepository = com.example.liftium.data.repository.UserRepository()
                    userRepository.createOrUpdateUser(user.uid, email, userName)
                }

                // Sign out the newly created user so they can log in with their credentials
                auth.signOut()
                
                Log.d(TAG, "User created successfully: ${result.user?.email}")

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    currentUser = null,
                    isAuthenticated = false,
                    errorMessage = null,
                    registrationSuccess = true,
                    successMessage = "Account created successfully! Please sign in."
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = getErrorMessage(e),
                    isAuthenticated = false,
                    registrationSuccess = false
                )
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Attempting to send password reset email to: $email")
                
                // Validate email format before sending
                if (email.isBlank()) {
                    throw IllegalArgumentException("Email cannot be empty")
                }
                
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    throw IllegalArgumentException("Invalid email format")
                }

                _authState.value = _authState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )

                // Send password reset email
                auth.sendPasswordResetEmail(email).await()

                Log.d(TAG, "Password reset email sent successfully to: $email")

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    successMessage = "Password reset email sent successfully. Please check your inbox and spam folder.",
                    errorMessage = null
                )
            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "Firebase Auth error sending password reset: ${e.errorCode} - ${e.message}", e)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = getPasswordResetErrorMessage(e),
                    successMessage = null
                )
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Validation error: ${e.message}", e)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Invalid email",
                    successMessage = null
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error sending password reset: ${e.message}", e)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to send reset email. Please try again.",
                    successMessage = null
                )
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = _authState.value.copy(
            currentUser = null,
            isAuthenticated = false,
            errorMessage = null,
            successMessage = null,
            registrationSuccess = false
        )
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _authState.value = _authState.value.copy(
            successMessage = null,
            registrationSuccess = false
        )
    }

    private fun getPasswordResetErrorMessage(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Invalid email address format"
            "ERROR_USER_NOT_FOUND" -> "No account found with this email address"
            "ERROR_USER_DISABLED" -> "This account has been disabled"
            "ERROR_TOO_MANY_REQUESTS" -> "Too many requests. Please try again later"
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your internet connection"
            else -> {
                Log.e(TAG, "Unknown Firebase Auth error code: ${exception.errorCode}")
                "Failed to send reset email. Please try again or contact support."
            }
        }
    }

    private fun getErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("badly formatted") == true ->
                "Invalid email format"
            exception.message?.contains("password is invalid") == true ->
                "Invalid email or password"
            exception.message?.contains("no user record") == true ->
                "No account found with this email"
            exception.message?.contains("already in use") == true ->
                "This email is already registered"
            exception.message?.contains("at least 6 characters") == true ->
                "Password must be at least 6 characters"
            exception.message?.contains("network error") == true ->
                "Network error. Please check your connection"
            exception.message?.contains("too many requests") == true ->
                "Too many attempts. Please try again later"
            else -> exception.message ?: "An error occurred. Please try again."
        }
    }
}
