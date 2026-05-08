package com.lagradost

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class MoviesDrivesProvider : MainAPI() { 
    // Yo hamro extension ko main configuration ho
    override var name = "MoviesDrives" 
    override var mainUrl = "https://new2.moviesdrives.my"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    override var lang = "en"
    override val hasMainPage = true

    // Search garda k dekhaune vanne logic yaha hunchha
    override suspend fun search(query: String): List<SearchResponse> {
        return listOf() // Ahile lai khali rakheko, pachi logic halne
    }
}
