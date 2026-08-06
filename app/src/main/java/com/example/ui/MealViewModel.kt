package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.MealItem
import com.example.data.api.NeisApiService
import com.example.data.api.SchoolInfoItem
import com.example.data.db.AppDatabase
import com.example.data.db.FavoriteMealEntity
import com.example.data.db.SchoolEntity
import com.example.data.repository.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface MealUiState {
    object Loading : MealUiState
    data class Success(val meals: List<MealItem>) : MealUiState
    object Empty : MealUiState
    data class Error(val message: String) : MealUiState
}

enum class ViewTab {
    DAILY, MONTHLY, FAVORITES
}

class MealViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val apiService = NeisApiService.create()
    private val repository = MealRepository(
        apiService = apiService,
        schoolDao = db.schoolDao(),
        favoriteMealDao = db.favoriteMealDao()
    )

    // Active Date (Default: Today or user selected date)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // View mode tab
    private val _currentTab = MutableStateFlow(ViewTab.DAILY)
    val currentTab: StateFlow<ViewTab> = _currentTab.asStateFlow()

    // Active School
    val selectedSchool: StateFlow<SchoolEntity> = repository.selectedSchool
        .combine(repository.savedSchools) { selected, savedList ->
            selected ?: savedList.firstOrNull() ?: repository.defaultSchool
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.defaultSchool
        )

    val savedSchools: StateFlow<List<SchoolEntity>> = repository.savedSchools
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteMeals: StateFlow<List<FavoriteMealEntity>> = repository.favoriteMeals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Meal UI State for Daily View
    private val _mealUiState = MutableStateFlow<MealUiState>(MealUiState.Loading)
    val mealUiState: StateFlow<MealUiState> = _mealUiState.asStateFlow()

    // Monthly Meals Map (Date string -> List of MealItems)
    private val _monthlyMeals = MutableStateFlow<Map<String, List<MealItem>>>(emptyMap())
    val monthlyMeals: StateFlow<Map<String, List<MealItem>>> = _monthlyMeals.asStateFlow()
    private val _isMonthlyLoading = MutableStateFlow(false)
    val isMonthlyLoading: StateFlow<Boolean> = _isMonthlyLoading.asStateFlow()

    // Allergy Numbers Display Toggle
    private val _showAllergyInfo = MutableStateFlow(true)
    val showAllergyInfo: StateFlow<Boolean> = _showAllergyInfo.asStateFlow()

    // School Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SchoolInfoItem>>(emptyList())
    val searchResults: StateFlow<List<SchoolInfoItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isSearchDialogVisible = MutableStateFlow(false)
    val isSearchDialogVisible: StateFlow<Boolean> = _isSearchDialogVisible.asStateFlow()

    private val _isAllergyGuideVisible = MutableStateFlow(false)
    val isAllergyGuideVisible: StateFlow<Boolean> = _isAllergyGuideVisible.asStateFlow()

    init {
        // Observe school and date changes to load meal info
        viewModelScope.launch {
            combine(selectedSchool, selectedDate) { school, date ->
                Pair(school, date)
            }.collect { (school, date) ->
                loadMealsForDate(school, date)
                if (_currentTab.value == ViewTab.MONTHLY) {
                    loadMealsForMonth(school, date)
                }
            }
        }
    }

    fun setTab(tab: ViewTab) {
        _currentTab.value = tab
        if (tab == ViewTab.MONTHLY) {
            loadMealsForMonth(selectedSchool.value, selectedDate.value)
        }
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun goToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun goToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun goToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun toggleAllergyInfo() {
        _showAllergyInfo.value = !_showAllergyInfo.value
    }

    fun showSearchDialog(show: Boolean) {
        _isSearchDialogVisible.value = show
        if (!show) {
            _searchQuery.value = ""
            _searchResults.value = emptyList()
        }
    }

    fun showAllergyGuide(show: Boolean) {
        _isAllergyGuideVisible.value = show
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.length >= 2) {
            searchSchools(query)
        } else {
            _searchResults.value = emptyList()
        }
    }

    fun searchSchools(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val results = repository.searchSchools(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun selectSchool(schoolItem: SchoolInfoItem) {
        viewModelScope.launch {
            repository.selectAndSaveSchool(schoolItem)
            showSearchDialog(false)
        }
    }

    fun selectSavedSchool(schoolCode: String) {
        viewModelScope.launch {
            repository.selectSavedSchool(schoolCode)
            showSearchDialog(false)
        }
    }

    fun refreshCurrentData() {
        loadMealsForDate(selectedSchool.value, selectedDate.value)
        if (_currentTab.value == ViewTab.MONTHLY) {
            loadMealsForMonth(selectedSchool.value, selectedDate.value)
        }
    }

    private fun loadMealsForDate(school: SchoolEntity, date: LocalDate) {
        viewModelScope.launch {
            _mealUiState.value = MealUiState.Loading
            val dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val meals = repository.getMealsForDate(
                officeCode = school.officeCode,
                schoolCode = school.schoolCode,
                dateFormatted = dateStr
            )
            if (meals.isEmpty()) {
                _mealUiState.value = MealUiState.Empty
            } else {
                _mealUiState.value = MealUiState.Success(meals)
            }
        }
    }

    private fun loadMealsForMonth(school: SchoolEntity, date: LocalDate) {
        viewModelScope.launch {
            _isMonthlyLoading.value = true
            val firstDay = date.withDayOfMonth(1)
            val lastDay = date.withDayOfMonth(date.lengthOfMonth())

            val fromYmd = firstDay.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val toYmd = lastDay.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

            val meals = repository.getMealsForMonth(
                officeCode = school.officeCode,
                schoolCode = school.schoolCode,
                fromYmd = fromYmd,
                toYmd = toYmd
            )

            val grouped = meals.groupBy { it.date }
            _monthlyMeals.value = grouped
            _isMonthlyLoading.value = false
        }
    }

    fun toggleFavorite(meal: MealItem) {
        viewModelScope.launch {
            val school = selectedSchool.value
            val isFav = repository.isFavorite(school.schoolCode, meal.date, meal.mealCode).firstOrNull() ?: false
            repository.toggleFavorite(school.schoolCode, meal, isFav)
        }
    }

    fun getFormattedDateString(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREAN)
        return date.format(formatter)
    }

    fun getFormattedMonthString(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
        return date.format(formatter)
    }
}
