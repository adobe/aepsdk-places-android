import com.adobe.marketing.mobile.gradle.BuildConstants

plugins {
    id("com.android.application")
}

android {
      namespace = "com.adobe.placestestapp"

    defaultConfig {
        applicationId = "com.adobe.placestestapp"
        compileSdk = BuildConstants.Versions.COMPILE_SDK_VERSION
        minSdk = BuildConstants.Versions.MIN_SDK_VERSION
        targetSdk = BuildConstants.Versions.TARGET_SDK_VERSION
        versionCode = BuildConstants.Versions.VERSION_CODE
        versionName = BuildConstants.Versions.VERSION_NAME
    }

    buildTypes {
        getByName(BuildConstants.BuildTypes.RELEASE)  {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":places"))
    implementation("com.adobe.marketing.mobile:core:2.+")
    implementation("com.adobe.marketing.mobile:assurance:2.+")
    implementation("com.adobe.marketing.mobile:edge:2.+")

    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}