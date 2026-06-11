plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.learnlab.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.learnlab.android"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }

    sourceSets {
        named("main") {
            assets.srcDirs("src/main/assets", "${rootDir}/content-json")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

// Filament materials (.mat) must be compiled to .filamat by `matc` before they
// can be loaded at runtime. Wire this once the Filament tools package is on the
// build machine / CI (download from the Filament release matching the lib
// version above). Until then the .filamat is produced manually with the command
// documented in src/main/assets/materials/unlit.mat.
//
// val matc = "/path/to/filament/bin/matc"
// tasks.register<Exec>("compileFilamentMaterials") {
//     commandLine(matc, "-a", "opengl", "-p", "mobile",
//         "-o", "src/main/assets/materials/unlit.filamat",
//         "src/main/assets/materials/unlit.mat")
// }
// tasks.named("preBuild") { dependsOn("compileFilamentMaterials") }

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Google Filament — real-time PBR renderer for the 3D simulation layer.
    // GLES 3.0 baseline with Filament's own GLES fallback; ships Swappy frame
    // pacing inside filament-android. Pin the version; bump deliberately.
    val filamentVersion = "1.54.5"
    implementation("com.google.android.filament:filament-android:$filamentVersion")
    implementation("com.google.android.filament:filament-utils-android:$filamentVersion")

    // Supabase backend — slide-override sync, image storage.
    val supabaseBom = platform("io.github.jan-tennert.supabase:bom:3.0.2")
    implementation(supabaseBom)
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.ktor:ktor-client-android:3.0.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
