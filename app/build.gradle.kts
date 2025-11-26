plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.21"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.liftium"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.liftium"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navegación (Alpha)
    implementation("androidx.navigation3:navigation3-runtime:1.0.0-alpha01")
    implementation("androidx.navigation3:navigation3-ui:1.0.0-alpha01")
    implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:1.0.0-alpha01")

    // Iconos extendidos
    implementation("androidx.compose.material:material-icons-extended")

    // Firebase
    implementation(libs.firebase.auth)
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")

    // Coil para cargar imágenes
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Google Maps y Ubicación
    implementation("com.google.maps.android:maps-compose:4.3.3") // Mapa para Jetpack Compose
    implementation("com.google.android.gms:play-services-location:21.3.0") // Servicios de ubicación (GPS)
    implementation("com.google.maps:google-maps-services:2.2.0") // Cliente de API de Google (Directions)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1") // Corutinas para Play Services

    // CameraX para funcionalidad de cámara
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Accompanist para permisos
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Biometría
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

}