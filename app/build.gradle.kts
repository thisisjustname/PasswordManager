plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.passwordmanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.passwordmanager"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.google.android.flexbox:flexbox:3.0.0")
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")
    implementation ("androidx.paging:paging-runtime-ktx:3.1.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")

    implementation("androidx.biometric:biometric:1.1.0")
    implementation ("com.google.android.material:material:1.8.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("androidx.cardview:cardview:1")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation ("androidx.core:core-ktx:1.10.0")

}