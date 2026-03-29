package com.example.bookkeeping.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookkeeping.R
import com.example.bookkeeping.ui.main.MainViewModel

private enum class AppTab(@StringRes val labelRes: Int) {
    RECORDS(R.string.tab_records),
    ADD(R.string.tab_add),
    STATS(R.string.tab_stats),
    CALENDAR(R.string.tab_calendar)
}

private object CalendarRoute {
    const val MONTH = "calendar/month"
    const val DAY = "calendar/day/{dayStartMillis}"
    const val DAY_ARG = "dayStartMillis"

    fun dayRoute(dayStartMillis: Long): String {
        return "calendar/day/$dayStartMillis"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookkeepingApp(
    viewModel: MainViewModel
) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val addRecordState by viewModel.addRecordState.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()

    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val calendarDailySummary by viewModel.calendarDailySummary.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(AppTab.RECORDS) }

    val calendarNavController = rememberNavController()
    val calendarBackStack by calendarNavController.currentBackStackEntryAsState()
    val currentCalendarRoute = calendarBackStack?.destination?.route

    val titleRes = when (currentTab) {
        AppTab.RECORDS -> R.string.title_record_list
        AppTab.ADD -> R.string.title_add_record
        AppTab.STATS -> R.string.title_daily_stats
        AppTab.CALENDAR -> {
            if (currentCalendarRoute?.startsWith("calendar/day/") == true) {
                R.string.title_day_detail
            } else {
                R.string.title_calendar
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(titleRes))
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == AppTab.RECORDS,
                    onClick = { currentTab = AppTab.RECORDS },
                    icon = { Icon(Icons.AutoMirrored.Outlined.List, contentDescription = null) },
                    label = { Text(stringResource(AppTab.RECORDS.labelRes)) }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.ADD,
                    onClick = { currentTab = AppTab.ADD },
                    icon = { Icon(Icons.Outlined.AddCircleOutline, contentDescription = null) },
                    label = { Text(stringResource(AppTab.ADD.labelRes)) }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.STATS,
                    onClick = { currentTab = AppTab.STATS },
                    icon = { Icon(Icons.Outlined.BarChart, contentDescription = null) },
                    label = { Text(stringResource(AppTab.STATS.labelRes)) }
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.CALENDAR,
                    onClick = { currentTab = AppTab.CALENDAR },
                    icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                    label = { Text(stringResource(AppTab.CALENDAR.labelRes)) }
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            AppTab.RECORDS -> RecordListScreen(
                modifier = Modifier,
                records = records,
                contentPadding = innerPadding
            )

            AppTab.ADD -> AddRecordScreen(
                modifier = Modifier,
                state = addRecordState,
                tags = viewModel.tags,
                contentPadding = innerPadding,
                onTypeSelected = viewModel::onTypeSelected,
                onAmountChanged = viewModel::onAmountChanged,
                onTagSelected = viewModel::onTagSelected,
                onNoteChanged = viewModel::onNoteChanged,
                onDateChanged = viewModel::onDateChanged,
                onSaveClicked = viewModel::saveRecord
            )

            AppTab.STATS -> StatsScreen(
                modifier = Modifier,
                dailySummary = dailySummary,
                contentPadding = innerPadding
            )

            AppTab.CALENDAR -> {
                NavHost(
                    navController = calendarNavController,
                    startDestination = CalendarRoute.MONTH,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(route = CalendarRoute.MONTH) {
                        CalendarScreen(
                            yearMonth = currentMonth,
                            dailySummary = calendarDailySummary,
                            contentPadding = innerPadding,
                            onPreviousMonth = viewModel::previousMonth,
                            onNextMonth = viewModel::nextMonth,
                            onDayClick = { dayStartMillis ->
                                calendarNavController.navigate(CalendarRoute.dayRoute(dayStartMillis))
                            }
                        )
                    }

                    composable(
                        route = CalendarRoute.DAY,
                        arguments = listOf(
                            navArgument(CalendarRoute.DAY_ARG) {
                                type = NavType.LongType
                            }
                        )
                    ) { backStackEntry ->
                        val dayStartMillis = backStackEntry.arguments?.getLong(CalendarRoute.DAY_ARG)
                            ?: return@composable
                        val dayRecordsFlow = remember(dayStartMillis) {
                            viewModel.observeRecordsForDay(dayStartMillis)
                        }
                        val dayRecords by dayRecordsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

                        DayDetailScreen(
                            dayStartMillis = dayStartMillis,
                            records = dayRecords,
                            contentPadding = innerPadding,
                            onBackClick = {
                                calendarNavController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
