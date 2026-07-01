package com.example.services

import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class UsgsResponse(
    val type: String,
    val features: List<UsgsFeature>
)

@JsonClass(generateAdapter = true)
data class UsgsFeature(
    val properties: UsgsProperties,
    val geometry: UsgsGeometry
)

@JsonClass(generateAdapter = true)
data class UsgsProperties(
    val mag: Double?,
    val place: String?,
    val time: Long?
)

@JsonClass(generateAdapter = true)
data class UsgsGeometry(
    val coordinates: List<Double> // [longitude, latitude, depth]
)

interface UsgsApi {
    @GET("earthquakes/feed/v1.0/summary/all_hour.geojson")
    suspend fun getRecentEarthquakes(): UsgsResponse
}

object ApiAggregator {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://earthquake.usgs.gov/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val usgsApi: UsgsApi = retrofit.create(UsgsApi::class.java)
    
    // Future expansion: NASA 3D Mapping, Web Scraping / ETL endpoints
}
