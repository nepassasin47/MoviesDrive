dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}

// Use an integer for version numbers
version = 1

cloudstream {
    // All of these properties are optional, you can safely remove any of them.

    description = "MoviesDrives provider for Cloudstream3 - Stream movies and TV series from new2.moviesdrives.my"
    authors = listOf("nepassasin47")

    /**
    * Status int as one of the following:
    * 0: Down
    * 1: Ok
    * 2: Slow
    * 3: Beta-only
    **/
    status = 1 // Ok - plugin is now functional

    tvTypes = listOf("Movie", "TvSeries")

    requiresResources = true
    language = "en"

    // Use a relevant icon (maybe a film reel or movie icon)
    iconUrl = "https://cdn-icons-png.flaticon.com/512/1179/1179120.png"
}

android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}