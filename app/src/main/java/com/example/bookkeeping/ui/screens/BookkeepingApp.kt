package com.example.bookkeeping.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

private enum class AppTab(val route: String, @StringRes val labelRes: Int) {
    RECORDS("records", R.string.tab_records),
    ADD("add", R.string.tab_add),
    STATS("stats", R.string.tab_stats),
    CALENDAR("calendar", R.string.tab_calendar)
}

private object AppRoute {
    const val RECORDS = "records"
    const val ADD = "add"
    const val STATS = "stats"
    const val CALENDAR = "calendar"
    const val DAY = "calendar/day/{dayStartMillis}"
    const val DAY_ARG = "dayStartMillis"
    const val ACCOUNTS = "accounts"
    const val BUDGETS = "budgets"
    const val RECURRING = "recurring"
    const val DATA = "data"

    fun dayRoute(dayStartMillis: Long): String = "calendar/day/$dayStartMillis"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookkeepingApp(
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoute.RECORDS

    val records by viewModel.records.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val activeAccounts by viewModel.activeAccounts.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val recurringRules by viewModel.recurringRules.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val addRecordState by viewModel.addRecordState.collectAsStateWithLifecycle()
    val addRecordTags by viewModel.addRecordTags.collectAsStateWithLifecycle()
    val recordListUiState by viewModel.recordListUiState.collectAsStateWithLifecycle()
    val statsUiState by viewModel.statsUiState.collectAsStateWithLifecycle()
    val calendarUiState by viewModel.calendarUiState.collectAsStateWithLifecycle()
    val dataManagementUiState by viewModel.dataManagementUiState.collectAsStateWithLifecycle()

    var menuExpanded by remember { mutableStateOf(false) }
    val isMainTabRoute = currentRoute in AppTab.entries.map { it.route }
    val titleText = when {
        currentRoute == AppRoute.ADD -> if (addRecordState.isEditing) "编辑记录" else stringResource(R.string.title_add_record)
        currentRoute == AppRoute.RECORDS -> stringResource(R.string.title_record_list)
        currentRoute == AppRoute.STATS -> stringResource(R.string.title_daily_stats)
        currentRoute == AppRoute.CALENDAR -> stringResource(R.string.title_calendar)
        currentRoute.startsWith("calendar/day/") -> stringResource(R.string.title_day_detail)
        currentRoute == AppRoute.ACCOUNTS -> "账户管理"
        currentRoute == AppRoute.BUDGETS -> "预算管理"
        currentRoute == AppRoute.RECURRING -> "固定收支"
        currentRoute == AppRoute.DATA -> "数据管理"
        else -> stringResource(R.string.app_name)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleText) },
                navigationIcon = {
                    if (!isMainTabRoute) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    if (isMainTabRoute) {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(text = { Text("账户管理") }, onClick = {
                                menuExpanded = false
                                navController.navigate(AppRoute.ACCOUNTS)
                            })
                            DropdownMenuItem(text = { Text("预算管理") }, onClick = {
                                menuExpanded = false
                                navController.navigate(AppRoute.BUDGETS)
                            })
                            DropdownMenuItem(text = { Text("固定收支") }, onClick = {
                                menuExpanded = false
                                navController.navigate(AppRoute.RECURRING)
                            })
                            DropdownMenuItem(text = { Text("数据管理") }, onClick = {
                                menuExpanded = false
                                navController.navigate(AppRoute.DATA)
                            })
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isMainTabRoute) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == AppTab.RECORDS.route,
                        onClick = {
                            navController.navigate(AppRoute.RECORDS) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Outlined.List, contentDescription = null) },
                        label = { Text(stringResource(AppTab.RECORDS.labelRes)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == AppTab.ADD.route,
                        onClick = {
                            viewModel.startCreateRecord()
                            navController.navigate(AppRoute.ADD) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Outlined.AddCircleOutline, contentDescription = null) },
                        label = { Text(stringResource(AppTab.ADD.labelRes)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == AppTab.STATS.route,
                        onClick = {
                            navController.navigate(AppRoute.STATS) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Outlined.BarChart, contentDescription = null) },
                        label = { Text(stringResource(AppTab.STATS.labelRes)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == AppTab.CALENDAR.route,
                        onClick = {
                            navController.navigate(AppRoute.CALENDAR) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                        label = { Text(stringResource(AppTab.CALENDAR.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.RECORDS,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(AppRoute.RECORDS) {
                RecordListScreen(
                    state = recordListUiState,
                    contentPadding = innerPadding,
                    onRecordClick = { record ->
                        viewModel.startEditRecord(record)
                        navController.navigate(AppRoute.ADD)
                    },
                    onKeywordChanged = viewModel::updateFilterKeyword,
                    onTypeChanged = viewModel::updateFilterType,
                    onTagChanged = viewModel::updateFilterTag,
                    onAccountChanged = viewModel::updateFilterAccount,
                    onStartDateChanged = viewModel::updateFilterStartDate,
                    onEndDateChanged = viewModel::updateFilterEndDate,
                    onMinAmountChanged = viewModel::updateFilterMinAmount,
                    onMaxAmountChanged = viewModel::updateFilterMaxAmount,
                    onClearFilters = viewModel::clearFilters,
                    onCreateFromRecurring = { rule ->
                        viewModel.startRecurringReminderEntry(rule)
                        navController.navigate(AppRoute.ADD)
                    }
                )
            }

            composable(AppRoute.ADD) {
                AddRecordScreen(
                    state = addRecordState,
                    tags = addRecordTags,
                    accounts = activeAccounts,
                    contentPadding = innerPadding,
                    onTypeSelected = viewModel::onTypeSelected,
                    onAmountChanged = viewModel::onAmountChanged,
                    onTagSelected = viewModel::onTagSelected,
                    onNoteChanged = viewModel::onNoteChanged,
                    onDateChanged = viewModel::onDateChanged,
                    onAccountSelected = viewModel::onRecordAccountSelected,
                    onSaveClicked = {
                        viewModel.saveRecord {
                            navController.popBackStack()
                        }
                    },
                    onDeleteClicked = if (addRecordState.isEditing) {
                        {
                            viewModel.deleteCurrentRecord {
                                navController.popBackStack()
                            }
                        }
                    } else {
                        null
                    }
                )
            }

            composable(AppRoute.STATS) {
                StatsScreen(
                    state = statsUiState,
                    contentPadding = innerPadding,
                    onPeriodSelected = viewModel::onStatsPeriodSelected,
                    onRecordTypeSelected = viewModel::onStatsRecordTypeSelected,
                    onDaySelected = viewModel::onStatsDaySelected,
                    onMonthSelected = viewModel::onStatsMonthSelected,
                    onYearSelected = viewModel::onStatsYearSelected,
                    onAccountSelected = viewModel::onStatsAccountSelected
                )
            }

            composable(AppRoute.CALENDAR) {
                CalendarScreen(
                    state = calendarUiState,
                    contentPadding = innerPadding,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                    onJumpToMonth = viewModel::jumpToMonth,
                    onAccountSelected = viewModel::onCalendarAccountSelected,
                    onDayClick = viewModel::onCalendarDateSelected,
                    onViewDayDetails = { dayStartMillis -> navController.navigate(AppRoute.dayRoute(dayStartMillis)) },
                    onRecordClick = { record ->
                        viewModel.startEditRecord(record)
                        navController.navigate(AppRoute.ADD)
                    }
                )
            }

            composable(
                route = AppRoute.DAY,
                arguments = listOf(navArgument(AppRoute.DAY_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val dayStartMillis = backStackEntry.arguments?.getLong(AppRoute.DAY_ARG) ?: return@composable
                val dayRecordsFlow = remember(dayStartMillis, calendarUiState.selectedAccountId) {
                    viewModel.observeRecordsForDay(dayStartMillis, calendarUiState.selectedAccountId)
                }
                val dayRecords by dayRecordsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
                val accountNameMap = remember(accounts) { accounts.associateBy({ it.id }, { it.name }) }
                DayDetailScreen(
                    dayStartMillis = dayStartMillis,
                    records = dayRecords,
                    accountNameForRecord = { accountId -> accountNameMap[accountId] ?: "未知账户" },
                    contentPadding = innerPadding,
                    onBackClick = { navController.popBackStack() },
                    onRecordClick = { record ->
                        viewModel.startEditRecord(record)
                        navController.navigate(AppRoute.ADD)
                    }
                )
            }

            composable(AppRoute.ACCOUNTS) {
                AccountManagementScreen(
                    accounts = accounts.sortedBy { it.sortOrder },
                    contentPadding = innerPadding,
                    onAddAccount = viewModel::addAccount,
                    onRenameAccount = viewModel::renameAccount,
                    onToggleArchive = viewModel::toggleAccountArchived,
                    onMoveAccount = viewModel::moveAccount
                )
            }

            composable(AppRoute.BUDGETS) {
                BudgetManagementScreen(
                    budgets = budgets,
                    records = records,
                    availableYears = availableYears,
                    defaultMonth = viewModel.defaultBudgetMonth(),
                    contentPadding = innerPadding,
                    onSaveBudget = viewModel::saveBudget,
                    onDeleteBudget = viewModel::deleteBudget
                )
            }

            composable(AppRoute.RECURRING) {
                RecurringManagementScreen(
                    rules = recurringRules,
                    dueRules = recordListUiState.dueRecurringRules,
                    accounts = activeAccounts,
                    todayDate = viewModel.todayDateString(),
                    contentPadding = innerPadding,
                    onSaveRule = viewModel::saveRecurringRule,
                    onDeleteRule = viewModel::deleteRecurringRule,
                    onToggleRuleActive = viewModel::toggleRecurringRuleActive,
                    onCreateFromRule = { rule ->
                        viewModel.startRecurringReminderEntry(rule)
                        navController.navigate(AppRoute.ADD)
                    }
                )
            }

            composable(AppRoute.DATA) {
                DataManagementScreen(
                    state = dataManagementUiState,
                    contentPadding = innerPadding,
                    onExportBackup = viewModel::exportBackup,
                    onImportBackup = viewModel::importBackup,
                    onClearMessage = viewModel::clearDataManagementMessage
                )
            }
        }
    }
}
