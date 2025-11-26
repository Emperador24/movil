package com.example.liftium

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.example.liftium.navigation.NavigationHost
import com.example.liftium.ui.theme.LiftiumTheme

class MainActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle deep links for password reset
        handleIntent(intent)
        
        setContent {
            LiftiumTheme {
                LiftiumApp()
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        
        if (data != null) {
            Log.d(TAG, "Deep link received: $data")
            
            // Check if this is a Firebase password reset link
            val mode = data.getQueryParameter("mode")
            val oobCode = data.getQueryParameter("oobCode")
            
            when (mode) {
                "resetPassword" -> {
                    Log.d(TAG, "Password reset link detected with code: $oobCode")
                    // The Firebase Auth SDK will automatically handle the password reset
                    // when the user opens the link in the app
                }
                "verifyEmail" -> {
                    Log.d(TAG, "Email verification link detected")
                }
                else -> {
                    Log.d(TAG, "Unknown deep link mode: $mode")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LiftiumApp() {
    NavigationHost()
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun LiftiumAppPreview() {
    LiftiumTheme {
        LiftiumApp()
    }
}