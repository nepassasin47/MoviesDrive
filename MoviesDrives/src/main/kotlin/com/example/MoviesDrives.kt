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
        return try {
            val document = app.get("$mainUrl/?s=${query.replace(" ", "+")}").document
            val items = document.select(".post-item a, article a, .entry a") // Look for links in post items

            items.mapNotNull { link ->
                val title = link.text().trim()
                val href = link.attr("href")

                if (title.isNotEmpty() && href.isNotEmpty() && href.contains(mainUrl)) {
                    val poster = link.selectFirst("img")?.attr("src") ?: ""

                    if (title.contains("Season", ignoreCase = true) || title.contains("Series", ignoreCase = true)) {
                        newTvSeriesSearchResponse(title, href) {
                            this.posterUrl = poster
                        }
                    } else {
                        newMovieSearchResponse(title, href) {
                            this.posterUrl = poster
                        }
                    }
                } else null
            }.distinctBy { it.url }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun loadHomePage(page: Int, timeout: Int): HomePageResponse? {
        return try {
            val document = app.get("$mainUrl${if (page > 1) "/page/$page" else ""}").document
            val items = document.select("a[href*='$mainUrl']") // Links to movie pages

            val searchResponses = items.mapNotNull { link ->
                val title = link.text().trim()
                val href = link.attr("href")

                if (title.isNotEmpty() && href.isNotEmpty() && href.contains(mainUrl) && !href.contains("category")) {
                    val poster = link.selectFirst("img")?.attr("src") ?: ""

                    if (title.contains("Season", ignoreCase = true) || title.contains("Series", ignoreCase = true)) {
                        newTvSeriesSearchResponse(title, href) {
                            this.posterUrl = poster
                        }
                    } else {
                        newMovieSearchResponse(title, href) {
                            this.posterUrl = poster
                        }
                    }
                } else null
            }.distinctBy { it.url }.take(20) // Limit to 20 items

            HomePageResponse(
                listOf(
                    HomePageList("Latest Releases", searchResponses, isHorizontalImages = true)
                )
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun load(url: String, data: String?): LoadResponse? {
        return try {
            val document = app.get(url).document
            val title = document.selectFirst("h1, .entry-title")?.text()?.trim() ?: return null
            val poster = document.selectFirst("img")?.attr("src")
            val description = document.selectFirst(".entry-content p, .post-content p")?.text()?.trim()

            // Try to determine if it's a series or movie
            val isSeries = title.contains("Season", ignoreCase = true) ||
                          title.contains("Series", ignoreCase = true) ||
                          document.selectFirst("a[href*='archive']")?.text()?.contains("Episode", ignoreCase = true) == true

            if (isSeries) {
                newTvSeriesLoadResponse(title, url, TvType.TvSeries, data) {
                    this.posterUrl = poster
                    this.plot = description
                    this.year = Regex("(\\d{4})").find(title)?.value?.toIntOrNull()
                }
            } else {
                newMovieLoadResponse(title, url, TvType.Movie, data) {
                    this.posterUrl = poster
                    this.plot = description
                    this.year = Regex("(\\d{4})").find(title)?.value?.toIntOrNull()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return try {
            val document = app.get(data).document

            // Extract download links - look for links containing "mdrive.lol" or "archive"
            val downloadLinks = document.select("a[href*='mdrive.lol'], a[href*='archive']")

            downloadLinks.forEach { link ->
                val href = link.attr("href")
                val text = link.text().trim()

                if (href.isNotEmpty() && text.isNotEmpty()) {
                    // Determine quality from text or parent elements
                    val parentText = link.parents().firstOrNull()?.text() ?: text
                    val quality = when {
                        parentText.contains("2160p", ignoreCase = true) || parentText.contains("4K", ignoreCase = true) -> "2160p"
                        parentText.contains("1080p", ignoreCase = true) -> "1080p"
                        parentText.contains("720p", ignoreCase = true) -> "720p"
                        parentText.contains("480p", ignoreCase = true) -> "480p"
                        else -> "Unknown"
                    }

                    callback.invoke(
                        ExtractorLink(
                            "MoviesDrives",
                            parentText,
                            href,
                            "",
                            quality,
                            false
                        )
                    )
                }
            }

            downloadLinks.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
