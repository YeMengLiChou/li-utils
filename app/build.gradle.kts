@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("maven-publish")
}

publishing {
    group = "com.github.YeMengLiChou"

    publications {
        create("maven_public", MavenPublication::class) {
            groupId = "com.github.YeMengLiChou"
            artifactId = "li-utils"
            version = "0.0.0"
//            from(components.getByName("library"))
        }
    }
}

android {
    namespace = "com.li.utils"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake.cppFlags.add("-std=c++17")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        viewBinding = true
    }

    externalNativeBuild {
        cmake {
            path  = file("src/main/cpp/CMakeLists.txt")
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)

    api(libs.activity.ktx)
    api(libs.fragment.ktx)
    api(libs.bundles.viewModel)
    api(libs.bundles.coroutines)
    api(libs.glide)
    api(libs.bundles.retrofit)
    api(libs.fastKv)

    implementation(libs.lifecycle.runtime)
    implementation(libs.moshi)
    implementation(libs.xlog)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}