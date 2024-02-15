plugins {
    id("aep-library")
}

val mavenCoreVersion: String by project
val mavenPlacesVersion: String by project

aepLibrary {
    namespace = "com.adobe.marketing.mobile.edge.consent"
    enableSpotlessPrettierForJava = true
    enableCheckStyle = true
 
    publishing {
        gitRepoName = "aepsdk-places-android"
        addCoreDependency(mavenCoreVersion)
        addEdgeDependency(mavenPlacesVersion)
    }
}

dependencies {
    implementation("com.adobe.marketing.mobile:core:$mavenCoreVersion-SNAPSHOT")
    implementation("com.adobe.marketing.mobile:places:$mavenPlacesVersion")
    implementation ("com.google.android.gms:play-services-location:21.1.0")

    // testImplementation dependencies provided by aep-library:
    // MOCKITO_CORE, MOCKITO_INLINE, JSON

    testImplementation ("com.fasterxml.jackson.core:jackson-databind:2.12.7")

    // androidTestImplementation dependencies provided by aep-library:
    // ANDROIDX_TEST_EXT_JUNIT, ESPRESSO_CORE

    androidTestImplementation ("com.fasterxml.jackson.core:jackson-databind:2.12.7")
}
