// Pin build plugins through the catalogue so developers use the tested toolchain.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Keep package identity and SDK bounds explicit for API 24+ offline execution.
android {
    namespace = "com.electrokit.app"
    compileSdk = 35
    buildToolsVersion = "35.0.0"
    defaultConfig {
        applicationId = "com.electrokit.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 27
        versionName = "4.3.3"
    }
    // Compose owns rendering; BuildConfig supplies the displayed version.
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // Keep all private release credentials outside source and distribution archives.
    signingConfigs {
        create("externalRelease") {
            val path = providers.environmentVariable("ELECTROKIT_KEYSTORE").orNull
            if (path != null) {
                storeFile = file(path)
                storePassword = providers.environmentVariable("ELECTROKIT_STORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("ELECTROKIT_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("ELECTROKIT_KEY_PASSWORD").orNull
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }
    // Production output is non-debuggable, code-shrunk and resource-shrunk.
    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (providers.environmentVariable("ELECTROKIT_KEYSTORE").isPresent)
                signingConfig = signingConfigs.getByName("externalRelease")
        }
    }
    // Align Java and Kotlin targets to avoid incompatible bytecode settings.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Android-resource-aware tests simulate the framework without claiming device coverage.
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { test ->
            test.maxHeapSize = "2g"
            // The Android test runtime downloader uses a separate JVM; honour a build host's Java
            // proxy/TLS configuration.
            listOf(
                    "http.proxyHost",
                    "http.proxyPort",
                    "https.proxyHost",
                    "https.proxyPort",
                    "javax.net.ssl.trustStore",
                    "javax.net.ssl.trustStorePassword",
                )
                .forEach { name -> System.getProperty(name)?.let { test.systemProperty(name, it) } }
            test.systemProperty(
                "robolectric.dependency.repo.url",
                "https://repo.maven.apache.org/maven2",
            )
        }
    }
    // Exclude duplicate dependency licence metadata from the package; credits retain attribution.
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

// No ads, analytics or account SDK. Only GitHub release metadata is fetched when enabled.
dependencies {
    implementation(libs.androidx.core)
    implementation(libs.activity)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    debugImplementation(libs.compose.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.material3)
    implementation(libs.icons)
    implementation(libs.navigation)
    implementation(libs.lifecycle)
    implementation(libs.viewmodel)
    implementation(libs.datastore)
    implementation(libs.serialization)
    testImplementation(libs.junit)
    "testDebugImplementation"(libs.robolectric)
    "testDebugImplementation"(libs.compose.test)
    "testDebugImplementation"(libs.test.core)
}

// JVM 17 matches the Android Java compilation target.
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
