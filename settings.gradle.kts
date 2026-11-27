pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val smartWrapperDir = file("../../GitHub/android-app-smartwrapper")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        flatDir {
            dirs("app/libs")
        }
        // Required by the payment library modules, consumed directly.
        //maven { url = uri(smartWrapperDir.resolve("local-maven-repo")) }
    }

    // Required by the payment library modules, consumed directly.
    /*versionCatalogs {
        create("coreLibs") { from(files(smartWrapperDir.resolve("gradle/core.versions.toml"))) }
        create("dataLibs") { from(files(smartWrapperDir.resolve("gradle/data.versions.toml"))) }
        create("uiLibs") { from(files(smartWrapperDir.resolve("gradle/ui.versions.toml"))) }
        create("testLibs") { from(files(smartWrapperDir.resolve("gradle/testing.versions.toml"))) }
    }*/
}

rootProject.name = "SmartPay"
include(":app")

// SmartWrapper payment libraries, consumed directly.
/*listOf(
    "core-payments",
    "core-network",
    "logger",
    "core-emv",
    "core-emv-sale",
    "smartcomponentsuinip",
).forEach { name ->
    include(":$name")
    project(":$name").projectDir = smartWrapperDir.resolve(name)
}*/
