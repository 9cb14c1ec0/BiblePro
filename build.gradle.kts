import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
    id("com.android.application")
}

group = "com.oss.simplycharge"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    androidTarget {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }
    }
    
    jvm("desktop")
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation("org.jetbrains.compose.material:material-icons-extended:${extra["compose.version"]}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.8.2")
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation("androidx.core:core-ktx:1.12.0")
            }
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.compose.material:material-icons-extended:${extra["compose.version"]}")
            }
        }
    }
}

// Task to copy resources from commonMain to androidMain assets
val copyResourcesToAndroidAssets by tasks.registering(Copy::class) {
    from("src/commonMain/resources")
    into("src/androidMain/assets")
    include("*.xml", "*.json", "*.txt")
}

// Make sure the copy task runs before Android compilation
tasks.named("preBuild") {
    dependsOn(copyResourcesToAndroidAssets)
}

android {
    namespace = "com.oss.biblepro"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.oss.biblepro"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.1"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    
    flavorDimensions += "mode"
    productFlavors {
        create("normal") {
            dimension = "mode"
            applicationId = "com.oss.biblepro"
            versionNameSuffix = ""
        }
        create("hidden") {
            dimension = "mode"
            applicationId = "com.flashlight.app"
            versionNameSuffix = "-hidden"
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = extra["compose.version"] as String
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        buildTypes.release.proguard {
            version.set("7.4.0")
            isEnabled.set(false)
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.Msi)
            packageName = "BiblePro"
            packageVersion = "1.0.1"
            
            // App icon for all platforms
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/bible.png"))
            }
            windows {
                iconFile.set(project.file("src/desktopMain/resources/bible.png"))
                menu = true
                menuGroup = "BiblePro"
                perUserInstall = true
            }
            linux {
                iconFile.set(project.file("src/desktopMain/resources/bible.png"))
            }
        }
    }
}

