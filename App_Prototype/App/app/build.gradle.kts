plugins {
    id ("com.android.application")
    id("com.chaquo.python")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    flavorDimensions ("pyVersion")
    productFlavors {
        create("py310") { dimension = "pyVersion" }
        create("py311") { dimension = "pyVersion" }
    }

    buildFeatures {
        viewBinding = true
    }

    chaquopy {
        productFlavors {
            getByName("py310") { version = "3.10" }
            getByName("py311") { version = "3.11" }
        }

        defaultConfig {
            version = "3.8"
            buildPython("C:/Users/cxwin/AppData/Local/Programs/Python/Python311/python.exe")

            pip {
                // A requirement specifier, with or without a version number:
                install("numpy")
                install("matplotlib")
            }
        }

        sourceSets {
            getByName("main") {
                srcDir("src/main/python")
            }
        }
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

