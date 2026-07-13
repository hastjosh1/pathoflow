package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.LabRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class LabApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { 
        LabRepository(
            database.userDao(),
            database.testItemDao(),
            database.patientEntryDao(),
            database.appSettingsDao(),
            database.priceListDao()
        )
    }
}
