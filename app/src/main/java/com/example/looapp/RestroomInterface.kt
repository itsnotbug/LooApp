package com.example.looapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RestroomInterface {
    @GET("https://zylalabs.com/api/2086/available+public+bathrooms+api/1869/get+public+bathrooms/")
    open fun getPublicBathrooms(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Call<Restroom1>

}