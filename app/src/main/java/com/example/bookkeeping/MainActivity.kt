package com.example.bookkeeping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.bookkeeping.ui.main.MainViewModel
import com.example.bookkeeping.ui.main.MainViewModelFactory
import com.example.bookkeeping.ui.screens.BookkeepingApp
import com.example.bookkeeping.ui.theme.BookkeepingTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val app = application as BookkeepingApplication
        MainViewModelFactory(
            repository = app.recordRepository,
            backupManager = app.backupManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookkeepingTheme {
                BookkeepingApp(viewModel = viewModel)
            }
        }
    }
}
