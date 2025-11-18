plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.gestionreparacionesapp"
    compileSdk = 36
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.gestionreparacionesapp"
        minSdk = 26
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.cast.framework)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

// --- LIBRERÍAS AÑADIDAS POR NOSOTROS ---

// 1. MATERIAL DESIGN
// (Esta línea de 'material' ya debería estar, si no, usa esta)
// La sugerencia 'libs.material.v1120' estaba mal, el nombre suele ser 'libs.material'
    implementation(libs.material)

// 2. ARQUITECTURA MVVM (ViewModel + LiveData)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    annotationProcessor(libs.lifecycle.compiler) // Para Java

// 3. Base de Datos Local (Room)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler) // Para Java

// 4. NAVEGACIÓN (Navigation Component)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

// 5. Cliente de API (Retrofit y Gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
}