plugins {
    id("aep-library")
}

val mavenCoreVersion: String by project
val mavenPlacesVersion: String by project

aepLibrary {
    namespace = "com.adobe.marketing.mobile"
    enableSpotlessPrettierForJava = true
    enableSpotless = true
 
    publishing {
        gitRepoName = "aepsdk-places-android"
        addCoreDependency(mavenCoreVersion)
    }
}

dependencies {
    implementation("com.adobe.marketing.mobile:core:$mavenCoreVersion-SNAPSHOT")
    implementation ("com.google.android.gms:play-services-location:21.1.0")

    // testImplementation dependencies provided by aep-library:
    // MOCKITO_CORE, MOCKITO_INLINE, JSON

    testImplementation ("com.fasterxml.jackson.core:jackson-databind:2.12.7")

    // androidTestImplementation dependencies provided by aep-library:
    // ANDROIDX_TEST_EXT_JUNIT, ESPRESSO_CORE

    androidTestImplementation ("com.fasterxml.jackson.core:jackson-databind:2.12.7")
}
