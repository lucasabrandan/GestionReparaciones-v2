plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.gestionreparacionesapp"
    compileSdk = 36 // Asegúrate que tu compileSdk sea 34 o superior
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.gestionreparacionesapp"
        minSdk = 26
        targetSdk = 36 // Asegúrate que tu targetSdk sea 34 o superior
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
    // Si usas Kotlin en tu proyecto, añade esto:
    // kotlinOptions {
    //     jvmTarget = "11"
    // }
}

dependencies {
    // Librerías base (estas ya no deberían dar error)
    implementation(libs.core.ktx)
    implementation(libs.fragment)
    implementation(libs.activity)

    // Resto de tus implementaciones...
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Arquitectura MVVM
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    annotationProcessor(libs.lifecycle.compiler) // Si usas Java. Si usas Kotlin, es 'kapt'

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler) // Si usas Java. Si usas Kotlin, es 'kapt'

    // Navegación
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
}