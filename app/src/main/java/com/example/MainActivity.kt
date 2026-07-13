package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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

                    // Rough screen depth so transitions slide the right way:
                    // entering a deeper screen slides in from the right, going
                    // back toward the dashboard slides in from the left.
                    fun depth(dest: String): Int = when {
                        dest == "splash" -> 0
                        dest == "setup" -> 1
                        dest == "dashboard" -> 2
                        else -> 3
                    }

                    AnimatedContent(
                        targetState = currentDest,
                        transitionSpec = {
                            val forward = depth(targetState) >= depth(initialState)
                            val dir = if (forward) 1 else -1
                            (slideInHorizontally(animationSpec = tween(280)) { full -> dir * full / 4 } +
                                fadeIn(animationSpec = tween(220))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(280)) { full -> -dir * full / 4 } +
                                    fadeOut(animationSpec = tween(180)))
                        },
                        label = "screen_transition"
                    ) { dest ->
                        when {
                            dest == "splash" -> SplashScreen(viewModel = viewModel)
                            dest == "setup" -> OnboardingSetupScreen(viewModel = viewModel)
                            dest == "dashboard" -> DashboardScreen(viewModel = viewModel)
                            dest == "new_entry" -> PatientEntryScreen(viewModel = viewModel)
                            dest == "tests" -> TestManagementScreen(viewModel = viewModel)
                            dest == "reports" -> ReportsScreen(viewModel = viewModel)
                            dest == "settings" -> SettingsScreen(viewModel = viewModel)
                            dest.startsWith("new_entry_dup_") -> PatientEntryScreen(
                                viewModel = viewModel,
                                originalPatientIdToDuplicateOrEdit = dest.removePrefix("new_entry_dup_"),
                                isEditMode = false
                            )
                            dest.startsWith("edit_entry_") -> PatientEntryScreen(
                                viewModel = viewModel,
                                originalPatientIdToDuplicateOrEdit = dest.removePrefix("edit_entry_"),
                                isEditMode = true
                            )
                            else -> DashboardScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
