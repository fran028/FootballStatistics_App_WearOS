
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    id("kotlin-kapt")
}

android {
    namespace = "com.example.footballstatistics_app_wearos"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.footballstatistics_app_wearos"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["permissions"] = listOf(
            "android.permission.BODY_SENSORS",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACTIVITY_RECOGNITION",
            "android.permission.FOREGROUND_SERVICE",
            "android.permission.WAKE_LOCK"
        )

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
        freeCompilerArgs += "-Xjvm-default=all"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.material3.windowsize)
    implementation(libs.compose.material3)
    implementation(libs.ui.text.google.fonts)
    implementation(libs.material3.android)
    implementation(libs.datastore.core.android)
    implementation(libs.room.common)
    implementation(libs.material)
    implementation(libs.lifecycle.service)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.material)
    implementation(libs.compose.navigation)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose.v287)
    implementation(libs.kotlinx.serialization.json.v163)

    implementation(libs.datastore)
    implementation(libs.datastore.preferences)
    implementation(libs.protobuf.javalite)

    implementation(libs.play.services.location)
    implementation("androidx.health:health-services-client:1.1.0-alpha05")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")


    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:$room_version")
    implementation("androidx.room:room-rxjava2:$room_version")
    implementation("androidx.room:room-rxjava3:$room_version")
    implementation("androidx.room:room-guava:$room_version")
    testImplementation("androidx.room:room-testing:$room_version")
    implementation("androidx.room:room-paging:$room_version")

    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")


}
