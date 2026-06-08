package com.zayyan.dormfood.ui.utils

import com.zayyan.dormfood.ui.model.PrediksiMenuResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PrediksiApiService {
    @GET("prediksi/{warungId}")
    suspend fun getPrediksi(
        @Path("warungId") warungId: String,
        @Query("days") days: Int = 30
    ): PrediksiMenuResponse

    @GET("prediksi/{warungId}/dummy")
    suspend fun getPrediksiDummy(
        @Path("warungId") warungId: String
    ): PrediksiMenuResponse
}

object PrediksiRepository {
    private const val BASE_URL = "https://web-production-ebbb1.up.railway.app/"

    private val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PrediksiApiService::class.java)

    suspend fun getPrediksi(warungId: String): Result<PrediksiMenuResponse> {
        return try {
            Result.success(api.getPrediksi(warungId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPrediksiDummy(warungId: String): Result<PrediksiMenuResponse> {
        return try {
            Result.success(api.getPrediksiDummy(warungId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}