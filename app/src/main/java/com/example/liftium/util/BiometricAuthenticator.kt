package com.example.liftium.util
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity


class BiometricAuthenticator(
    private val activity: FragmentActivity
) {

    private val executor = ContextCompat.getMainExecutor(activity)
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo


    fun checkBiometricSupport(): BiometricSupportState {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricSupportState.READY
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricSupportState.NOT_SUPPORTED
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricSupportState.UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricSupportState.NOT_ENROLLED
            else -> BiometricSupportState.NOT_SUPPORTED
        }
    }


    fun authenticate(onResult: (Boolean) -> Unit) {

        when (checkBiometricSupport()) {
            BiometricSupportState.NOT_ENROLLED -> {
                Toast.makeText(
                    activity,
                    "No biometrics enrolled. Please set up a screen lock.",
                    Toast.LENGTH_LONG
                ).show()
                onResult(false)
                return
            }
            BiometricSupportState.NOT_SUPPORTED, BiometricSupportState.UNAVAILABLE -> {
                Toast.makeText(
                    activity,
                    "Biometric authentication is not supported or unavailable.",
                    Toast.LENGTH_LONG
                ).show()
                onResult(false)
                return
            }
            BiometricSupportState.READY -> {
            }
        }

        biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)

                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(
                            activity,
                            "Authentication error: $errString",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    onResult(false)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(true)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        activity, "Authentication failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    onResult(false)
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentication Required")
            .setSubtitle("Log in using your biometric credential to access your progress photos.")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()



        biometricPrompt.authenticate(promptInfo)
    }


    enum class BiometricSupportState {
        READY,
        NOT_SUPPORTED,
        UNAVAILABLE,
        NOT_ENROLLED
    }
}