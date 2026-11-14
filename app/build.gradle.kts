plugins {
    alias(libs.plugins.android.application)
    //alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.kohistanbookingapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.kohistanbookingapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Lifecycle components
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.common)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Activity KTX for viewModels() delegate
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Coroutines
    implementation(libs.kotlin.coroutines)
    implementation(libs.coroutines.android)

    // Sdp and Ssp sizes
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)

    // Retrofit
    implementation(libs.retrofit.android)
    implementation(libs.retrofit.gson)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    //JsonSerializable
    implementation (libs.kotlinx.serialization.json)
    //Http Request Logging
    implementation (libs.logging.interceptor)

    implementation(libs.androidx.core.splashscreen)

    //Circular image view
    implementation(libs.circularImageView)

    //Swipe Refresh
    implementation(libs.androidx.swiperefreshlayout)
}