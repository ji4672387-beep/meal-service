package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    @Query("SELECT * FROM schools ORDER BY savedTimestamp DESC")
    fun getAllSavedSchools(): Flow<List<SchoolEntity>>

    @Query("SELECT * FROM schools WHERE isSelected = 1 LIMIT 1")
    fun getSelectedSchool(): Flow<SchoolEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchool(school: SchoolEntity)

    @Query("UPDATE schools SET isSelected = 0")
    suspend fun clearSelectedSchool()

    @Query("UPDATE schools SET isSelected = 1 WHERE schoolCode = :schoolCode")
    suspend fun setSelectedSchool(schoolCode: String)

    @Query("DELETE FROM schools WHERE schoolCode = :schoolCode")
    suspend fun deleteSchool(schoolCode: String)
}

@Dao
interface FavoriteMealDao {
    @Query("SELECT * FROM favorite_meals ORDER BY mealDate DESC")
    fun getAllFavoriteMeals(): Flow<List<FavoriteMealEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_meals WHERE schoolCode = :schoolCode AND mealDate = :mealDate AND mealCode = :mealCode)")
    fun isFavorite(schoolCode: String, mealDate: String, mealCode: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteMealEntity)

    @Query("DELETE FROM favorite_meals WHERE schoolCode = :schoolCode AND mealDate = :mealDate AND mealCode = :mealCode")
    suspend fun deleteFavorite(schoolCode: String, mealDate: String, mealCode: String)
}
