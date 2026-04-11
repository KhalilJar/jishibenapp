package com.example.bookkeeping.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookkeeping.data.backup.BackupManager
import com.example.bookkeeping.data.repository.RecordRepository

class MainViewModelFactory(
    private val repository: RecordRepository,
    private val backupManager: BackupManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, backupManager) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
