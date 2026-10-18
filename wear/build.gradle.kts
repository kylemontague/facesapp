import java.io.File
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "watch.facesapp.wear"
    compileSdk = 34

    defaultConfig {
        applicationId = "watch.facesapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 5
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystoreConfigFile = rootProject.file("keystore.properties")
    val hasKeystore = keystoreConfigFile.exists()
    val keystoreProps = Properties().apply {
        if (hasKeystore) {
            FileInputStream(keystoreConfigFile).use { load(it) }
        }
    }

    signingConfigs {
        create("release") {
            if (hasKeystore) {
                val storeFilePath = keystoreProps.getProperty("storeFile", "release.jks")
                storeFile = if (File(storeFilePath).isAbsolute) File(storeFilePath) else rootProject.file(storeFilePath)
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            } else if (System.getenv("KEYSTORE_FILE") != null) {
                storeFile = file(System.getenv("KEYSTORE_FILE"))
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (hasKeystore || System.getenv("KEYSTORE_FILE") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.wear)
    implementation(libs.androidx.wear.watchface)
    implementation(libs.androidx.wear.watchface.complications.data)
    implementation(libs.androidx.wear.watchface.complications.data.source)
    implementation(libs.androidx.wear.watchface.complications.rendering)
    implementation(libs.play.services.wearable)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
