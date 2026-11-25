import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    // Needed because android.builtInKotlin=false (required by the SmartWrapper modules)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Type-safe navigation routes (@Serializable)
    alias(libs.plugins.kotlin.serialization)
    // Room + Hilt code generation
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.smart.smartpay"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.smart.smartpay"
        minSdk = 26 // core-payments requires 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0.0"

        // core-payments & co. declare flavors; pick which one to consume
        missingDimensionStrategy("version", "quality")
        missingDimensionStrategy("mdm", "v0")

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Ui libraries
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.bundles.core)
    implementation(libs.bundles.lifecycle)

    // Dependency injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    // Transaction history database (Room)
    implementation(libs.bundles.database)
    ksp(libs.androidx.room.compiler)

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)

    // Networking
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.moshi)

    // Smart dependencies, consumed directly
    /*implementation(project(":core-network"))
        implementation(project(":core-payments"))
        implementation(project(":core-emv"))
        implementation(project(":core-emv-sale"))*/
    // Smart Payment Libraries (consumed directly)


    // Third party integrations Smart Payment Libraries
    implementation(group = "", name = "smart-core-emv-library-1.0.0.0-quality-v0-debug", ext = "aar")
    implementation(group = "", name = "smart-core-emv-sale-library-1.0.0.0-quality-v0-debug", ext = "aar")
    implementation(group = "", name = "smart-core-network-library-1.0.0.0-quality-debug", ext = "aar")
    implementation(group = "", name = "smart-core-payments-library-1.0.0.0-quality-v0-debug", ext = "aar")
    implementation(group = "", name = "smart-logger-library-1.0.0.0-quality-v0-debug", ext = "aar")
}