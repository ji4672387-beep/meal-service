package com.example.data.api

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface NeisApiService {

    @GET("hub/mealServiceDietInfo")
    suspend fun getMealInfo(
        @Query("ATPT_OFCDC_SC_CODE") officeCode: String,
        @Query("SD_SCHUL_CODE") schoolCode: String,
        @Query("MLSV_YMD") mealDate: String? = null,
        @Query("MLSV_FROM_YMD") fromDate: String? = null,
        @Query("MLSV_TO_YMD") toDate: String? = null,
        @Query("Type") type: String = "json",
        @Query("pIndex") pIndex: Int = 1,
        @Query("pSize") pSize: Int = 100
    ): ResponseBody

    @GET("hub/schoolInfo")
    suspend fun searchSchool(
        @Query("SCHUL_NM") schoolName: String,
        @Query("Type") type: String = "json",
        @Query("pIndex") pIndex: Int = 1,
        @Query("pSize") pSize: Int = 30
    ): ResponseBody

    companion object {
        private const val BASE_URL = "https://open.neis.go.kr/"

        fun create(): NeisApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .build()
                .create(NeisApiService::class.java)
        }
    }
}

object NeisJsonParser {

    fun parseMealResponse(jsonString: String): List<NeisMealRow> {
        val resultList = mutableListOf<NeisMealRow>()
        try {
            val rootObj = JSONObject(jsonString)
            if (!rootObj.has("mealServiceDietInfo")) return emptyList()

            val mainArray = rootObj.getJSONArray("mealServiceDietInfo")
            for (i in 0 until mainArray.length()) {
                val item = mainArray.getJSONObject(i)
                if (item.has("row")) {
                    val rowArray = item.getJSONArray("row")
                    for (j in 0 until rowArray.length()) {
                        val row = rowArray.getJSONObject(j)
                        resultList.add(
                            NeisMealRow(
                                officeCode = row.optString("ATPT_OFCDC_SC_CODE", ""),
                                officeName = row.optString("ATPT_OFCDC_SC_NM", ""),
                                schoolCode = row.optString("SD_SCHUL_CODE", ""),
                                schoolName = row.optString("SCHUL_NM", ""),
                                mealCode = row.optString("MMEAL_SC_CODE", "2"),
                                mealName = row.optString("MMEAL_SC_NM", "중식"),
                                mealDate = row.optString("MLSV_YMD", ""),
                                dishNameHtml = row.optString("DDISH_NM", ""),
                                originInfoHtml = row.optString("ORGC_INFO", ""),
                                calorieInfo = row.optString("CAL_INFO", ""),
                                nutritionInfoHtml = row.optString("NTR_INFO", "")
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }

    fun parseSchoolResponse(jsonString: String): List<SchoolInfoItem> {
        val resultList = mutableListOf<SchoolInfoItem>()
        try {
            val rootObj = JSONObject(jsonString)
            if (!rootObj.has("schoolInfo")) return emptyList()

            val mainArray = rootObj.getJSONArray("schoolInfo")
            for (i in 0 until mainArray.length()) {
                val item = mainArray.getJSONObject(i)
                if (item.has("row")) {
                    val rowArray = item.getJSONArray("row")
                    for (j in 0 until rowArray.length()) {
                        val row = rowArray.getJSONObject(j)
                        resultList.add(
                            SchoolInfoItem(
                                officeCode = row.optString("ATPT_OFCDC_SC_CODE", ""),
                                officeName = row.optString("ATPT_OFCDC_SC_NM", ""),
                                schoolCode = row.optString("SD_SCHUL_CODE", ""),
                                schoolName = row.optString("SCHUL_NM", ""),
                                schoolType = row.optString("SCHUL_KND_SC_NM", ""),
                                locationName = row.optString("LCTN_SC_NM", ""),
                                roadAddress = row.optString("ORG_RDNMA", "")
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return resultList
    }

    fun parseDishText(dishHtml: String): List<DishDetail> {
        if (dishHtml.isBlank()) return emptyList()

        // Replace <br/> or <br> or <br /> with newlines
        val lines = dishHtml
            .replace("<br/>", "\n")
            .replace("<br>", "\n")
            .replace("<br />", "\n")
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val dishDetails = mutableListOf<DishDetail>()

        // Regex to extract allergy numbers at end of string or trailing pattern like "5.6.10.13." or "1.2."
        val allergyRegex = Regex("""[\d\.]+$""")

        for (line in lines) {
            // Unescape HTML entities if any
            val cleanLine = line.replace("&amp;", "&")
            val match = allergyRegex.find(cleanLine)

            val allergyNumbers = mutableListOf<Int>()
            var cleanName = cleanLine

            if (match != null) {
                val allergyStr = match.value
                val numbers = allergyStr.split(".").mapNotNull { it.trim().toIntOrNull() }
                if (numbers.isNotEmpty()) {
                    allergyNumbers.addAll(numbers)
                    cleanName = cleanLine.substring(0, match.range.first).trim()
                }
            }

            // Remove trailing dot or asterisk if left over
            cleanName = cleanName.trimEnd('.', '*', ' ')

            if (cleanName.isNotBlank()) {
                dishDetails.add(
                    DishDetail(
                        rawName = line,
                        cleanName = cleanName,
                        allergyNumbers = allergyNumbers.distinct().sorted()
                    )
                )
            }
        }

        return dishDetails
    }

    fun parseOriginText(originHtml: String?): String {
        if (originHtml.isNullOrBlank()) return ""
        return originHtml
            .replace("<br/>", "\n")
            .replace("<br>", "\n")
            .replace("<br />", "\n")
            .trim()
    }

    fun parseNutritionText(nutritionHtml: String?): List<String> {
        if (nutritionHtml.isNullOrBlank()) return emptyList()
        return nutritionHtml
            .replace("<br/>", "\n")
            .replace("<br>", "\n")
            .replace("<br />", "\n")
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}
