# Cloudstream3 MoviesDrives Plugin Workflow

This document provides an overview of the plugin codebase after recent modifications.

## Project Structure

- **Root build.gradle.kts** – Defines buildscript dependencies and project-wide configurations.
- **MoviesDrives/build.gradle.kts** – Plugin‑specific configuration (metadata, dependencies).
- **MoviesDrives/src/main/kotlin/com/example/MoviesDrives.kt** – Main provider class.
- **MoviesDrives/src/main/kotlin/com/example/ExamplePlugin.kt** – Plugin entry point.
- **MoviesDrives/src/main/kotlin/com/example/BlankFragment.kt** – Settings fragment UI.
- **MoviesDrives/src/main/res/layout/fragment_blank.xml** – Layout for the settings fragment.
- **MoviesDrives/src/main/res/values/strings.xml** – String resources.
- **MoviesDrives/src/main/AndroidManifest.xml** – Empty manifest (plugin‑only).

---

## 1. Root `build.gradle.kts`

```kotlin
import com.android.build.gradle.BaseExtension
import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        classpath("com.github.recloudstream:gradle:1.0-SNAPSHOT")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

fun Project.cloudstream(configuration: CloudstreamExtension.() -> Unit) = extensions.getByName<CloudstreamExtension>("cloudstream").configuration()

fun Project.android(configuration: BaseExtension.() -> Unit) = extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "kotlin-android")
    apply(plugin = "com.lagradost.cloudstream3.gradle")

    cloudstream {
        // Yesle automatic tapai ko GitHub repo ko name nikalcha (nepassasin47/MoviesDrive)
        setRepo(System.getenv("GITHUB_REPOSITORY") ?: "nepassasin47/MoviesDrive")
    }

    android {
        // Namespace must match your package name in MoviesDrivesProvider.kt
        namespace = "com.example"

        defaultConfig {
            minSdk = 21
            compileSdkVersion(35)
            targetSdk = 35
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        tasks.withType<KotlinJvmCompile> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_1_8)
                freeCompilerArgs.addAll(
                    "-Xno-call-assertions",
                    "-Xno-param-assertions",
                    "-Xno-receiver-assertions"
                )
            }
        }
    }

    dependencies {
        val cloudstream by configurations
        val implementation by configurations

        cloudstream("com.lagradost:cloudstream3:pre-release")

        implementation(kotlin("stdlib"))
        implementation("com.github.Blatzar:NiceHttp:0.4.11")
        implementation("org.jsoup:jsoup:1.18.3")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    }
}

task<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
```

---

## 2. Plugin Module `build.gradle.kts`

```kotlin
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
    status = 3 // Beta-only as it's under development

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
```

---

## 3. Main Provider `MoviesDrives.kt`

```kotlin
package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.SubtitleFile
import org.jsoup.Jsoup

class MoviesDrivesProvider : MainAPI() {
    // Yo hamro extension ko main configuration ho
    override var name = "MoviesDrives"
    override var mainUrl = "https://new2.moviesdrives.my"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override var lang = "en"
    override val hasMainPage = true

    // Search garda k dekhaune vanne logic yaha hunchha
    override suspend fun search(query: String): List<SearchResponse> {
        // TODO: Implement actual search logic
        // For now, return dummy data
        return listOf(
            newMovieSearchResponse("Example Movie", "https://via.placeholder.com/300x450") {
                this.posterUrl = "https://via.placeholder.com/300x450"
            },
            newTvSeriesSearchResponse("Example TV Series", "https://via.placeholder.com/300x450") {
                this.posterUrl = "https://via.placeholder.com/300x450"
            }
        )
    }

    override suspend fun loadHomePage(page: Int, timeout: Int): HomePageResponse? {
        // Return a simple home page with a single section
        val items = listOf(
            newMovieSearchResponse("Sample Movie 1", "https://via.placeholder.com/300x450") {
                this.posterUrl = "https://via.placeholder.com/300x450"
            },
            newMovieSearchResponse("Sample Movie 2", "https://via.placeholder.com/300x450") {
                this.posterUrl = "https://via.placeholder.com/300x450"
            },
            newTvSeriesSearchResponse("Sample TV Series 1", "https://via.placeholder.com/300x450") {
                this.posterUrl = "https://via.placeholder.com/300x450"
            }
        )
        return HomePageResponse(
            listOf(
                HomePageList("Latest Movies", items, isHorizontalImages = true)
            )
        )
    }

    override suspend fun load(url: String, data: String?): LoadResponse? {
        // Implement loading of movie/series details
        return null
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        // Implement video link extraction
        return false
    }
}
```

---

## 4. Plugin Entry Point `ExamplePlugin.kt`

```kotlin
package com.example

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class ExamplePlugin: Plugin() {
    private var activity: AppCompatActivity? = null

    override fun load(context: Context) {
        activity = context as? AppCompatActivity

        // All providers should be added in this manner
        registerMainAPI(MoviesDrivesProvider())

        openSettings = {
            val frag = BlankFragment(this)
            activity?.let {
                frag.show(it.supportFragmentManager, "Frag")
            }
        }
    }
}
```

---

## 5. Settings Fragment `BlankFragment.kt`

```kotlin
package com.example

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.utils.UIHelper.colorFromAttribute

/**
 * A simple [Fragment] subclass.
 */
class BlankFragment(private val plugin: ExamplePlugin) : BottomSheetDialogFragment() {

    // Helper function to get a drawable resource by name
    @SuppressLint("DiscouragedApi")
    @Suppress("SameParameterValue")
    private fun getDrawable(name: String): Drawable? {
        val id = plugin.resources?.getIdentifier(name, "drawable", BuildConfig.LIBRARY_PACKAGE_NAME)
        return id?.let { ResourcesCompat.getDrawable(plugin.resources ?: return null, it, null) }
    }

    // Helper function to get a string resource by name
    @SuppressLint("DiscouragedApi")
    @Suppress("SameParameterValue")
    private fun getString(name: String): String? {
        val id = plugin.resources?.getIdentifier(name, "string", BuildConfig.LIBRARY_PACKAGE_NAME)
        return id?.let { plugin.resources?.getString(it) }
    }

    // Generic findView function to find views by name
    @SuppressLint("DiscouragedApi")
    private fun <T : View> View.findViewByName(name: String): T? {
        val id = plugin.resources?.getIdentifier(name, "id", BuildConfig.LIBRARY_PACKAGE_NAME)
        return findViewById(id ?: return null)
    }

    @SuppressLint("DiscouragedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layoutId = plugin.resources?.getIdentifier("fragment_blank", "layout", BuildConfig.LIBRARY_PACKAGE_NAME)
        return layoutId?.let {
            inflater.inflate(plugin.resources?.getLayout(it), container, false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        val imageView: ImageView? = view.findViewByName("imageView")
        val imageView2: ImageView? = view.findViewByName("imageView2")
        val textView: TextView? = view.findViewByName("textView")
        val textView2: TextView? = view.findViewByName("textView2")

        // Set text and styling if the views are found
        textView?.apply {
            text = getString("hello_fragment")
            TextViewCompat.setTextAppearance(this, R.style.ResultInfoText)
        }

        textView2?.text = view.context.resources.getText(R.string.legal_notice_text)

        // Set image resources and tint if the views are found
        imageView?.apply {
            setImageDrawable(getDrawable("ic_android_24dp"))
            imageTintList = ColorStateList.valueOf(view.context.getColor(R.color.white))
        }

        imageView2?.apply {
            setImageDrawable(getDrawable("ic_android_24dp"))
            imageTintList = ColorStateList.valueOf(view.context.colorFromAttribute(R.attr.white))
        }
    }
}
```

---

## 6. Layout `fragment_blank.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/mainFragmentLayout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center_horizontal"
    android:orientation="vertical"
    tools:context=".BlankFragment">

    <TextView
        android:id="@+id/textView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/hello_fragment" />

    <TextView
        android:id="@+id/textView2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/loaded_from_app_trans" />

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:orientation="horizontal">

        <ImageView
            android:id="@+id/imageView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            tools:src="@drawable/ic_android_24dp"
            tools:ignore="ContentDescription" />

        <ImageView
            android:id="@+id/imageView2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            tools:src="@drawable/ic_android_24dp"
            tools:ignore="ContentDescription" />
    </LinearLayout>

</LinearLayout>
```

---

## 7. Strings `strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="hello_fragment">Hello funny fragment!!</string>
    <string name="loaded_from_app_trans">[loaded from app trans]</string>
</resources>
```

---

## 8. Android Manifest `AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

---

## 9. README.md (partial)

Refer to the project README for setup and usage instructions.

---

## Next Steps

1. **Fix build dependency**: The plugin `com.github.recloudstream:gradle:1.0-SNAPSHOT` is currently unavailable. Check the Cloudstream3 repository for the correct coordinates or use a stable version.

2. **Implement actual scraping**: Replace dummy search and home‑page logic with real HTTP requests and HTML parsing for `new2.moviesdrives.my`.

3. **Implement `load` and `loadLinks`**: Add logic to fetch movie/TV series details and extract video links.

4. **Test with Cloudstream3**: After fixing the build, run `./gradlew MoviesDrives:make` to generate the `.cs3` plugin file and deploy it to a Cloudstream3 instance.

5. **Customize UI**: Update `fragment_blank.xml` and `BlankFragment.kt` to provide meaningful settings for the plugin.

---

## Notes

- The plugin is currently in **beta** status (`status = 3`).
- The provider supports both **Movie** and **TvSeries** types.
- The icon URL points to a generic film‑reel icon; replace with a brand‑appropriate image.
- The project uses **NiceHttp** for HTTP requests, **Jsoup** for HTML parsing, and **Jackson** for JSON handling.

