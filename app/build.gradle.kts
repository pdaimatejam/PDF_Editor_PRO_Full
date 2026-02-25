plugins {
    id("com.android.application")
}

android {
    namespace = "com.pdf.editorpro"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pdf.editorpro"
        minSdk = 21
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


    dependencies {
        implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")
       // implementation("com.github.barteksc:android-pdf-viewer:3.2.0-beta.1")
        implementation("com.tom-roush:pdfbox-android:2.0.27.0")
        implementation("androidx.appcompat:appcompat:1.7.1")
        implementation("com.google.android.material:material:1.13.0")
        //implementation("androidx.core:core-splashscreen:1.0.1")
        implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.3")    }
}