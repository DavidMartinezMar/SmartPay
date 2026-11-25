// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Also used by core-network (same plugin as dataLibs.plugins.serialaization)
    alias(libs.plugins.kotlin.serialization) apply false
    // Also used by core-network (same plugin as dataLibs.plugins.ksp)
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    // Plugins used by the SmartWrapper modules (core-payments & co.)
    alias(libs.plugins.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
}
