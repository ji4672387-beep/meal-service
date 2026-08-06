package com.example.data.repository

import com.example.data.api.MealItem
import com.example.data.api.NeisApiService
import com.example.data.api.NeisJsonParser
import com.example.data.api.SchoolInfoItem
import com.example.data.db.FavoriteMealDao
import com.example.data.db.FavoriteMealEntity
import com.example.data.db.SchoolDao
import com.example.data.db.SchoolEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MealRepository(
    private val apiService: NeisApiService,
    private val schoolDao: SchoolDao,
    private val favoriteMealDao: FavoriteMealDao
) {

    // Default Fallback School (부산소프트웨어마이스터고등학교)
    val defaultSchool = SchoolEntity(
        schoolCode = "7150089",
        officeCode = "C10",
        officeName = "부산광역시교육청",
        schoolName = "부산소프트웨어마이스터고등학교",
        schoolType = "고등학교",
        locationName = "부산광역시",
        roadAddress = "부산광역시 강서구 가락대로 1393",
        isSelected = true
    )

    val selectedSchool: Flow<SchoolEntity?> = schoolDao.getSelectedSchool()
    val savedSchools: Flow<List<SchoolEntity>> = schoolDao.getAllSavedSchools()
    val favoriteMeals: Flow<List<FavoriteMealEntity>> = favoriteMealDao.getAllFavoriteMeals()

    suspend fun getMealsForDate(
        officeCode: String,
        schoolCode: String,
        dateFormatted: String // YYYYMMDD
    ): List<MealItem> = withContext(Dispatchers.IO) {
        try {
            val responseBody = apiService.getMealInfo(
                officeCode = officeCode,
                schoolCode = schoolCode,
                mealDate = dateFormatted
            )
            val jsonString = responseBody.string()
            val rows = NeisJsonParser.parseMealResponse(jsonString)

            rows.map { row ->
                MealItem(
                    mealCode = row.mealCode,
                    mealName = row.mealName,
                    date = row.mealDate,
                    dishes = NeisJsonParser.parseDishText(row.dishNameHtml),
                    originInfo = NeisJsonParser.parseOriginText(row.originInfoHtml),
                    calorieInfo = row.calorieInfo ?: "",
                    nutritionInfo = NeisJsonParser.parseNutritionText(row.nutritionInfoHtml)
                )
            }.sortedBy { it.mealCode }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getMealsForMonth(
        officeCode: String,
        schoolCode: String,
        fromYmd: String,
        toYmd: String
    ): List<MealItem> = withContext(Dispatchers.IO) {
        try {
            val responseBody = apiService.getMealInfo(
                officeCode = officeCode,
                schoolCode = schoolCode,
                fromDate = fromYmd,
                toDate = toYmd
            )
            val jsonString = responseBody.string()
            val rows = NeisJsonParser.parseMealResponse(jsonString)

            rows.map { row ->
                MealItem(
                    mealCode = row.mealCode,
                    mealName = row.mealName,
                    date = row.mealDate,
                    dishes = NeisJsonParser.parseDishText(row.dishNameHtml),
                    originInfo = NeisJsonParser.parseOriginText(row.originInfoHtml),
                    calorieInfo = row.calorieInfo ?: "",
                    nutritionInfo = NeisJsonParser.parseNutritionText(row.nutritionInfoHtml)
                )
            }.sortedWith(compareBy({ it.date }, { it.mealCode }))
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchSchools(query: String): List<SchoolInfoItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val responseBody = apiService.searchSchool(schoolName = query.trim())
            val jsonString = responseBody.string()
            NeisJsonParser.parseSchoolResponse(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun selectAndSaveSchool(schoolItem: SchoolInfoItem) = withContext(Dispatchers.IO) {
        val entity = SchoolEntity(
            schoolCode = schoolItem.schoolCode,
            officeCode = schoolItem.officeCode,
            officeName = schoolItem.officeName,
            schoolName = schoolItem.schoolName,
            schoolType = schoolItem.schoolType ?: "",
            locationName = schoolItem.locationName ?: "",
            roadAddress = schoolItem.roadAddress ?: "",
            isSelected = true
        )
        schoolDao.clearSelectedSchool()
        schoolDao.insertSchool(entity)
    }

    suspend fun selectSavedSchool(schoolCode: String) = withContext(Dispatchers.IO) {
        schoolDao.clearSelectedSchool()
        schoolDao.setSelectedSchool(schoolCode)
    }

    fun isFavorite(schoolCode: String, mealDate: String, mealCode: String): Flow<Boolean> {
        return favoriteMealDao.isFavorite(schoolCode, mealDate, mealCode)
    }

    suspend fun toggleFavorite(
        schoolCode: String,
        meal: MealItem,
        currentlyFavorite: Boolean
    ) = withContext(Dispatchers.IO) {
        if (currentlyFavorite) {
            favoriteMealDao.deleteFavorite(schoolCode, meal.date, meal.mealCode)
        } else {
            val summary = meal.dishes.take(3).joinToString(", ") { it.cleanName }
            favoriteMealDao.insertFavorite(
                FavoriteMealEntity(
                    schoolCode = schoolCode,
                    mealDate = meal.date,
                    mealCode = meal.mealCode,
                    mealName = meal.mealName,
                    dishSummary = summary,
                    calorieInfo = meal.calorieInfo
                )
            )
        }
    }
}
