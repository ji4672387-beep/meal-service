package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schools")
data class SchoolEntity(
    @PrimaryKey val schoolCode: String,
    val officeCode: String,
    val officeName: String,
    val schoolName: String,
    val schoolType: String,
    val locationName: String,
    val roadAddress: String,
    val isSelected: Boolean = false,
    val savedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_meals")
data class FavoriteMealEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val schoolCode: String,
    val mealDate: String, // YYYYMMDD
    val mealCode: String, // 1, 2, 3
    val mealName: String, // 조식, 중식, 석식
    val dishSummary: String,
    val calorieInfo: String,
    val timestamp: Long = System.currentTimeMillis()
)
