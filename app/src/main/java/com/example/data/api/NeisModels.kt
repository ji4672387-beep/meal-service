package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// School info search response
@JsonClass(generateAdapter = true)
data class SchoolInfoItem(
    @Json(name = "ATPT_OFCDC_SC_CODE") val officeCode: String,
    @Json(name = "ATPT_OFCDC_SC_NM") val officeName: String,
    @Json(name = "SD_SCHUL_CODE") val schoolCode: String,
    @Json(name = "SCHUL_NM") val schoolName: String,
    @Json(name = "SCHUL_KND_SC_NM") val schoolType: String? = null,
    @Json(name = "LCTN_SC_NM") val locationName: String? = null,
    @Json(name = "ORG_RDNMA") val roadAddress: String? = null
)

// Raw Meal item row from NEIS
@JsonClass(generateAdapter = true)
data class NeisMealRow(
    @Json(name = "ATPT_OFCDC_SC_CODE") val officeCode: String,
    @Json(name = "ATPT_OFCDC_SC_NM") val officeName: String? = null,
    @Json(name = "SD_SCHUL_CODE") val schoolCode: String,
    @Json(name = "SCHUL_NM") val schoolName: String,
    @Json(name = "MMEAL_SC_CODE") val mealCode: String, // 1: 조식, 2: 중식, 3: 석식
    @Json(name = "MMEAL_SC_NM") val mealName: String, // 조식, 중식, 석식
    @Json(name = "MLSV_YMD") val mealDate: String, // YYYYMMDD
    @Json(name = "DDISH_NM") val dishNameHtml: String, // Dish names separated by <br/>, with allergy numbers
    @Json(name = "ORGC_INFO") val originInfoHtml: String? = null, // Origin info separated by <br/>
    @Json(name = "CAL_INFO") val calorieInfo: String? = null, // e.g., "750.2 Kcal"
    @Json(name = "NTR_INFO") val nutritionInfoHtml: String? = null // e.g. "탄수화물(g) : 100.2 <br/>..."
)

// Processed Clean Meal Model for UI
data class MealItem(
    val mealCode: String,
    val mealName: String, // 조식, 중식, 석식
    val date: String, // YYYYMMDD
    val dishes: List<DishDetail>,
    val originInfo: String,
    val calorieInfo: String,
    val nutritionInfo: List<String>,
    val isBookmarked: Boolean = false
)

data class DishDetail(
    val rawName: String,
    val cleanName: String,
    val allergyNumbers: List<Int>
)

// Allergy dictionary mapping
object AllergyGuide {
    val allergyMap = mapOf(
        1 to "난류",
        2 to "우유",
        3 to "메밀",
        4 to "땅콩",
        5 to "대두",
        6 to "밀",
        7 to "고등어",
        8 to "게",
        9 to "새우",
        10 to "돼지고기",
        11 to "복숭아",
        12 to "토마토",
        13 to "아황산류",
        14 to "호두",
        15 to "닭고기",
        16 to "쇠고기",
        17 to "오징어",
        18 to "조개류(굴,전복,홍합 포함)",
        19 to "잣"
    )
}
