/*
 * Base Ads Module - Build Configuration
 * 
 * Module for managing AdMob and mediation ads with smart show policies,
 * VIP gate functionality, and Firebase Analytics integration.
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.tinhtx.baseads.library"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "DEBUG_LOG_ENABLED", "true")
            buildConfigField("String", "MEDIATION_TYPE", "\"bidding\"")
            buildConfigField("String", "VUNGLE_PARTNER", "\"vungle_liftoff\"")
            buildConfigField("String", "IRONSOURCE_PARTNER", "\"ironsource\"")
            buildConfigField("String", "META_PARTNER", "\"meta_audience_network\"")
        }
        release {
            buildConfigField("boolean", "DEBUG_LOG_ENABLED", "false")
            buildConfigField("String", "MEDIATION_TYPE", "\"bidding\"")
            buildConfigField("String", "VUNGLE_PARTNER", "\"vungle_liftoff\"")
            buildConfigField("String", "IRONSOURCE_PARTNER", "\"ironsource\"")
            buildConfigField("String", "META_PARTNER", "\"meta_audience_network\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Google Mobile Ads SDK (with bidding mediation support)
    implementation(libs.play.services.ads.v2460)
    
    // Bidding mediation partners (for real-time bidding)
    // Vungle Liftoff Monetize - Bidding adapter
    implementation("com.google.ads.mediation:vungle:7.4.0.0")
    // ironSource Bidding adapter
    implementation(libs.ironsource.mediation.adapter)
    // Meta Audience Network - Bidding adapter
    implementation(libs.meta.mediation.adapter)
    
    // Firebase Analytics
    implementation(platform(libs.firebase.bom.v3351))
    implementation(libs.google.firebase.analytics.ktx)
    
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android.v173)
    
    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}