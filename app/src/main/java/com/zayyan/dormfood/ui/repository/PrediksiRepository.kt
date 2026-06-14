    package com.zayyan.dormfood.ui.repository

    import com.google.firebase.firestore.FirebaseFirestore
    import com.zayyan.dormfood.ui.model.DataGrafikMingguan
    import com.zayyan.dormfood.ui.model.PrediksiMenuResponse
    import kotlinx.coroutines.tasks.await
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory
    import retrofit2.http.GET
    import retrofit2.http.Path
    import retrofit2.http.Query
    import java.util.Calendar

    interface IPrediksiRepository {
        suspend fun getPrediksi(warungId: String): Result<PrediksiMenuResponse>
        suspend fun getPrediksiDummy(warungId: String): Result<PrediksiMenuResponse>
        suspend fun getGrafikMingguan(
            warungId: String,
            tahun: Int,
            bulan: Int,
            namaMenu: String
        ): Result<List<DataGrafikMingguan>>
    }

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

    object PrediksiRepository : IPrediksiRepository {
        private const val BASE_URL = "https://web-production-ebbb1.up.railway.app/"

        private val api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PrediksiApiService::class.java)

        override suspend fun getPrediksi(warungId: String): Result<PrediksiMenuResponse> {
            return try {
                Result.success(api.getPrediksi(warungId))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        override suspend fun getPrediksiDummy(warungId: String): Result<PrediksiMenuResponse> {
            return try {
                Result.success(api.getPrediksiDummy(warungId))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        override suspend fun getGrafikMingguan(
            warungId: String,
            tahun: Int,
            bulan: Int,
            namaMenu: String
        ): Result<List<DataGrafikMingguan>> {
            return try {
                val firestore = FirebaseFirestore.getInstance()

                val calStart = Calendar.getInstance().apply {
                    set(tahun, bulan - 1, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val calEnd = Calendar.getInstance().apply {
                    set(tahun, bulan - 1, 1, 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                }

                val startTs = com.google.firebase.Timestamp(calStart.time)
                val endTs = com.google.firebase.Timestamp(calEnd.time)

                val snapshot = firestore.collection("orders")
                    .whereEqualTo("warungId", warungId)
                    .whereEqualTo("statusPesanan", "Selesai")
                    .whereGreaterThanOrEqualTo("createdAt", startTs)
                    .whereLessThanOrEqualTo("createdAt", endTs)
                    .get()
                    .await()

                val weekTotals = IntArray(4)

                for (doc in snapshot.documents) {
                    val ts = doc.getTimestamp("createdAt") ?: continue
                    val cal = Calendar.getInstance().apply {
                        time = ts.toDate()
                        minimalDaysInFirstWeek = 1
                        firstDayOfWeek = Calendar.MONDAY
                    }
                    val weekIndex = (cal.get(Calendar.WEEK_OF_MONTH) - 1).coerceIn(0, 3)

                    @Suppress("UNCHECKED_CAST")
                    val items = doc.get("items") as? List<Map<String, Any>> ?: continue
                    for (item in items) {
                        if ((item["namaMenu"] as? String) == namaMenu) {
                            val qty = (item["quantity"] as? Long)?.toInt() ?: 0
                            weekTotals[weekIndex] += qty
                        }
                    }
                }

                val result = (0 until 4).map { i ->
                    DataGrafikMingguan(
                        label = "Mg ${i + 1}",
                        jumlah = weekTotals[i]
                    )
                }

                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }