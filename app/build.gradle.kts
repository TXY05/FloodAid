plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.map.secret)

    //id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

android {
    namespace = "com.example.floodaid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.floodaid"
        minSdk = 28
        targetSdk = 35
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
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.material.icons.extended)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    implementation(libs.google.firebase.analytics)
    implementation(libs.google.map)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.room.runtime)
    ksp("androidx.room:room-compiler:${rootProject.extra["room_version"]}")
    implementation(libs.androidx.room.ktx)

    implementation (libs.androidx.lifecycle.viewmodel.ktx.v270)
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.gson)
    implementation (libs.play.services.location)
    implementation(libs.androidx.runtime.livedata)
    // Firebase Authentication
    implementation (libs.firebase.auth.ktx)
    implementation (libs.firebase.auth)

    //Splash Screen
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.coil3.coil.compose)
    implementation(libs.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

}
