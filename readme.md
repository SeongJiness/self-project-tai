```gradle
rootProject.name = "safebandproject"

plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android' version '1.8.0' apply false
}

android {
    namespace 'com.android.safebandproject'
    compileSdk 33

    defaultConfig {
        applicationId "com.android.safebandproject"
        minSdk 21
        targetSdk 33
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    buildFeatures {
        dataBinding true
        viewBinding true
    }
}

constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.0") {
            because("kotlin-stdlib-jdk7 is now a part of kotlin-stdlib")
        }
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0") {
            because("kotlin-stdlib-jdk8 is now a part of kotlin-stdlib")
        }
    }
```
## Modularization Example

[https://github.com/ad-astra-per-ardua/selfproject_temp](https://github.com/ad-astra-per-ardua/selfproject_temp/tree/main/app)

Repactored to 

[https://github.com/ad-astra-per-ardua/123321/tree/master/app](https://github.com/ad-astra-per-ardua/123321/tree/master/app)https://github.com/ad-astra-per-ardua/123321/tree/master/app
