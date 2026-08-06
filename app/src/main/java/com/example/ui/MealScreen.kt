package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.NoMeals
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.api.DishDetail
import com.example.data.api.MealItem
import com.example.data.db.FavoriteMealEntity
import com.example.data.db.SchoolEntity
import com.example.ui.components.AllergyGuideDialog
import com.example.ui.components.SchoolSearchDialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealScreen(
    viewModel: MealViewModel
) {
    val context = LocalContext.current

    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedSchool by viewModel.selectedSchool.collectAsStateWithLifecycle()
    val savedSchools by viewModel.savedSchools.collectAsStateWithLifecycle()
    val mealUiState by viewModel.mealUiState.collectAsStateWithLifecycle()
    val monthlyMeals by viewModel.monthlyMeals.collectAsStateWithLifecycle()
    val isMonthlyLoading by viewModel.isMonthlyLoading.collectAsStateWithLifecycle()
    val showAllergyInfo by viewModel.showAllergyInfo.collectAsStateWithLifecycle()
    val favoriteMeals by viewModel.favoriteMeals.collectAsStateWithLifecycle()

    val isSearchDialogVisible by viewModel.isSearchDialogVisible.collectAsStateWithLifecycle()
    val isAllergyGuideVisible by viewModel.isAllergyGuideVisible.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    // Native DatePicker Dialog Helper
    val datePickerDialog = remember(selectedDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                viewModel.setDate(LocalDate.of(year, month + 1, dayOfMonth))
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "급식 알리미",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Active School Chip Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.showSearchDialog(true) }
                            .testTag("school_selector_btn"),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedSchool.schoolName.take(8) + if (selectedSchool.schoolName.length > 8) "..." else "",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { viewModel.showSearchDialog(true) },
                        modifier = Modifier.testTag("search_school_icon_btn")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "학교 검색")
                    }

                    IconButton(
                        onClick = { viewModel.refreshCurrentData() },
                        modifier = Modifier.testTag("refresh_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // View Mode Tab Navigation
            PrimaryTabRow(
                selectedTabIndex = currentTab.ordinal,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = currentTab == ViewTab.DAILY,
                    onClick = { viewModel.setTab(ViewTab.DAILY) },
                    text = { Text("일별 급식", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_daily")
                )
                Tab(
                    selected = currentTab == ViewTab.MONTHLY,
                    onClick = { viewModel.setTab(ViewTab.MONTHLY) },
                    text = { Text("월간 식단표", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_monthly")
                )
                Tab(
                    selected = currentTab == ViewTab.FAVORITES,
                    onClick = { viewModel.setTab(ViewTab.FAVORITES) },
                    text = { Text("즐겨찾기", fontWeight = FontWeight.Bold) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (favoriteMeals.isNotEmpty()) {
                                    Badge { Text(favoriteMeals.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    },
                    modifier = Modifier.testTag("tab_favorites")
                )
            }

            when (currentTab) {
                ViewTab.DAILY -> DailyMealView(
                    viewModel = viewModel,
                    selectedSchool = selectedSchool,
                    selectedDate = selectedDate,
                    mealUiState = mealUiState,
                    showAllergyInfo = showAllergyInfo,
                    favoriteMeals = favoriteMeals,
                    onOpenDatePicker = { datePickerDialog.show() }
                )

                ViewTab.MONTHLY -> MonthlyMealView(
                    viewModel = viewModel,
                    selectedSchool = selectedSchool,
                    selectedDate = selectedDate,
                    monthlyMeals = monthlyMeals,
                    isLoading = isMonthlyLoading,
                    onSelectDateAndSwitch = { date ->
                        viewModel.setDate(date)
                        viewModel.setTab(ViewTab.DAILY)
                    }
                )

                ViewTab.FAVORITES -> FavoritesMealView(
                    favoriteMeals = favoriteMeals,
                    onSelectFavoriteDate = { fav ->
                        try {
                            val year = fav.mealDate.substring(0, 4).toInt()
                            val month = fav.mealDate.substring(4, 6).toInt()
                            val day = fav.mealDate.substring(6, 8).toInt()
                            viewModel.setDate(LocalDate.of(year, month, day))
                            viewModel.setTab(ViewTab.DAILY)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                )
            }
        }

        // Search School Dialog
        if (isSearchDialogVisible) {
            SchoolSearchDialog(
                searchQuery = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                searchResults = searchResults,
                savedSchools = savedSchools,
                activeSchoolCode = selectedSchool.schoolCode,
                isSearching = isSearching,
                onSelectSearchSchool = { viewModel.selectSchool(it) },
                onSelectSavedSchool = { viewModel.selectSavedSchool(it) },
                onDismiss = { viewModel.showSearchDialog(false) }
            )
        }

        // Allergy Guide Dialog
        if (isAllergyGuideVisible) {
            AllergyGuideDialog(
                onDismiss = { viewModel.showAllergyGuide(false) }
            )
        }
    }
}

@Composable
fun DailyMealView(
    viewModel: MealViewModel,
    selectedSchool: SchoolEntity,
    selectedDate: LocalDate,
    mealUiState: MealUiState,
    showAllergyInfo: Boolean,
    favoriteMeals: List<FavoriteMealEntity>,
    onOpenDatePicker: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Hero Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_meal_banner_1785979515220),
                        contentDescription = "School Meal Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Dark Gradient Overlay Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = selectedSchool.schoolName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.getFormattedDateString(selectedDate),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        )
                    }
                }
            }
        }

        // Date Control Bar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.goToPreviousDay() },
                        modifier = Modifier.testTag("prev_day_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "이전 날짜",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenDatePicker)
                            .testTag("date_picker_btn"),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = viewModel.getFormattedDateString(selectedDate),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.goToNextDay() },
                            modifier = Modifier.testTag("next_day_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "다음 날짜",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Button(
                            onClick = { viewModel.goToToday() },
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("today_btn"),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text("오늘", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Allergy Options Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = showAllergyInfo,
                        onClick = { viewModel.toggleAllergyInfo() },
                        label = {
                            Text(
                                text = if (showAllergyInfo) "알레르기 번호 표시 중" else "알레르기 번호 숨김",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("toggle_allergy_chip")
                    )
                }

                TextButton(
                    onClick = { viewModel.showAllergyGuide(true) },
                    modifier = Modifier.testTag("allergy_guide_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("1~19번 안내", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Meal Content according to State
        when (mealUiState) {
            is MealUiState.Loading -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "급식 정보를 불러오고 있습니다...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            is MealUiState.Empty -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NoMeals,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "선택하신 날짜에는 급식 정보가 없습니다.",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "주말, 공휴일 또는 방학기간일 수 있습니다.\n상단 날짜 변경 버튼으로 다른 날짜를 선택해보세요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            is MealUiState.Success -> {
                items(mealUiState.meals) { meal ->
                    val dateFormatted = meal.date
                    val isFav = favoriteMeals.any {
                        it.schoolCode == selectedSchool.schoolCode && it.mealDate == dateFormatted && it.mealCode == meal.mealCode
                    }
                    MealCardItem(
                        meal = meal,
                        showAllergyInfo = showAllergyInfo,
                        isFavorite = isFav,
                        onToggleFavorite = { viewModel.toggleFavorite(meal) }
                    )
                }
            }

            is MealUiState.Error -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("오류가 발생했습니다: ${mealUiState.message}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.refreshCurrentData() }) {
                                Text("다시 시도")
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealCardItem(
    meal: MealItem,
    showAllergyInfo: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row (Meal Code/Type, Calorie, Favorite)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (meal.mealCode) {
                                    "1" -> MaterialTheme.colorScheme.secondaryContainer
                                    "2" -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = meal.mealName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = when (meal.mealCode) {
                                "1" -> MaterialTheme.colorScheme.secondary
                                "2" -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.tertiary
                            }
                        )
                    }

                    if (meal.calorieInfo.isNotBlank()) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = meal.calorieInfo,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("favorite_meal_btn_${meal.mealCode}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "즐겨찾기",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            // Dishes List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                meal.dishes.forEach { dish ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = dish.cleanName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (showAllergyInfo && dish.allergyNumbers.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                dish.allergyNumbers.forEach { num ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = num.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Accordion for Nutrition & Origin Info
            if (meal.originInfo.isNotBlank() || meal.nutritionInfo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "원산지 및 영양정보 접기" else "원산지 및 영양정보 보기",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (meal.originInfo.isNotBlank()) {
                            Text(
                                text = "원산지 정보",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = meal.originInfo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (meal.nutritionInfo.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "영양 정보",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = meal.nutritionInfo.joinToString("\n"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyMealView(
    viewModel: MealViewModel,
    selectedSchool: SchoolEntity,
    selectedDate: LocalDate,
    monthlyMeals: Map<String, List<MealItem>>,
    isLoading: Boolean,
    onSelectDateAndSwitch: (LocalDate) -> Unit
) {
    val monthString = viewModel.getFormattedMonthString(selectedDate)
    val yearMonth = remember(selectedDate) { YearMonth.of(selectedDate.year, selectedDate.monthValue) }
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7 // 0 for Sun, 1 for Mon...

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Month Selector Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.setDate(selectedDate.minusMonths(1)) },
                        modifier = Modifier.testTag("prev_month_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "이전 달")
                    }

                    Text(
                        text = monthString,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )

                    IconButton(
                        onClick = { viewModel.setDate(selectedDate.plusMonths(1)) },
                        modifier = Modifier.testTag("next_month_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "다음 달")
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            item {
                // Calendar Grid Header (일 ~ 토)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("일", "월", "화", "수", "목", "금", "토").forEachIndexed { idx, day ->
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = when (idx) {
                                0 -> Color.Red
                                6 -> Color.Blue
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Days Grid
            item {
                val totalGridCells = firstDayOfWeek + daysInMonth
                val totalRows = (totalGridCells + 6) / 7

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (rowIndex in 0 until totalRows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (colIndex in 0..6) {
                                val cellIndex = rowIndex * 7 + colIndex
                                val dayNum = cellIndex - firstDayOfWeek + 1

                                if (dayNum in 1..daysInMonth) {
                                    val dateObj = LocalDate.of(selectedDate.year, selectedDate.monthValue, dayNum)
                                    val dateStr = dateObj.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                    val mealsForDay = monthlyMeals[dateStr] ?: emptyList()

                                    val isSelectedDay = dateObj == selectedDate
                                    val isToday = dateObj == LocalDate.now()

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(82.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onSelectDateAndSwitch(dateObj) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when {
                                                isSelectedDay -> MaterialTheme.colorScheme.primaryContainer
                                                isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                                else -> MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                        border = if (isToday) androidx.compose.foundation.BorderStroke(
                                            1.5.dp,
                                            MaterialTheme.colorScheme.primary
                                        ) else null
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = dayNum.toString(),
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = if (isToday || isSelectedDay) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = when (colIndex) {
                                                    0 -> Color.Red
                                                    6 -> Color.Blue
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            if (mealsForDay.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            MaterialTheme.colorScheme.primary,
                                                            shape = RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "${mealsForDay.size}식단",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun FavoritesMealView(
    favoriteMeals: List<FavoriteMealEntity>,
    onSelectFavoriteDate: (FavoriteMealEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Text(
                text = "북마크된 즐겨찾기 식단 (${favoriteMeals.size}개)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (favoriteMeals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "저장된 즐겨찾기 식단이 없습니다.",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "일별 급식 화면에서 북마크 아이콘을 눌러 자주 보는 식단을 저장하세요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(favoriteMeals) { fav ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectFavoriteDate(fav) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = fav.mealName,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = fav.mealDate,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            if (fav.calorieInfo.isNotBlank()) {
                                Text(
                                    text = fav.calorieInfo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = fav.dishSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
