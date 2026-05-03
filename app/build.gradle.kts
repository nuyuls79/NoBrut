plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.ycngmn.notubetv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ycngmn.notubetv"
        minSdk = 23   // ✅ sudah diturunkan
        targetSdk = 35
        versionCode = 3
        versionName = "0.0.3"

        // ✅ FIX penting untuk API 23 (vector drawable)
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
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

    // ✅ penting untuk stabilitas Compose
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // ✅ Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)

    // ✅ WebView Multiplatform
    api(libs.compose.webview.multiplatform)

    // ✅ AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // ✅ Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ✅ Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // ✅ Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // ✅ Compose Activity
    implementation(libs.androidx.activity.compose)

    // ✅ Material 3
    implementation(libs.androidx.material3.android)

    // ✅ TV (hati-hati di API 23, tapi masih bisa jalan)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    // ✅ Testing
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // ✅ Debug tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
