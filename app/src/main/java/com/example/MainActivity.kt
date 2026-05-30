package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LabViewModel
import com.example.ui.viewmodel.LabViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: LabViewModel by viewModels {
        LabViewModelFactory(
            application,
            (application as LabApplication).repository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val currentDest by viewModel.currentNavDestination.collectAsState()
                    
                    when {
                        currentDest == "splash" -> {
                            SplashScreen(viewModel = viewModel)
                        }
                        currentDest == "setup" -> {
                            OnboardingSetupScreen(viewModel = viewModel)
                        }
                        currentDest == "dashboard" -> {
                            DashboardScreen(viewModel = viewModel)
                        }
                        currentDest == "new_entry" -> {
                            PatientEntryScreen(viewModel = viewModel)
                        }
                        currentDest == "tests" -> {
                            TestManagementScreen(viewModel = viewModel)
                        }
                        currentDest == "reports" -> {
                            ReportsScreen(viewModel = viewModel)
                        }
                        currentDest == "settings" -> {
                            SettingsScreen(viewModel = viewModel)
                        }
                        currentDest.startsWith("new_entry_dup_") -> {
                            val originalId = currentDest.removePrefix("new_entry_dup_")
                            PatientEntryScreen(
                                viewModel = viewModel,
                                originalPatientIdToDuplicateOrEdit = originalId,
                                isEditMode = false
                            )
                        }
                        currentDest.startsWith("edit_entry_") -> {
                            val originalId = currentDest.removePrefix("edit_entry_")
                            PatientEntryScreen(
                                viewModel = viewModel,
                                originalPatientIdToDuplicateOrEdit = originalId,
                                isEditMode = true
                            )
                        }
                        else -> {
                            DashboardScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
