package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppSettings
import com.example.data.model.PatientEntry
import com.example.data.model.TestItem
import com.example.ui.components.OfflineUpiQrCode
import com.example.ui.translation.*
import com.example.ui.viewmodel.LabViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReportsScreen(viewModel: LabViewModel) {
    BackHandler {
        viewModel.navigateTo("dashboard")
    }
    val activeLang by viewModel.activeLanguage.collectAsState()
    val patients by viewModel.allPatients.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Revenue calculations
    val totalRevenueCollected = patients.sumOf { it.amountPaid }
    val totalEstimatedRevenue = patients.sumOf { it.amountPayable }
    val totalPendingPayments = patients.sumOf { it.balanceAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar
        CustomTopAppBar(
            title = translate("reports", activeLang),
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo("dashboard") }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // Summary Card Info
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(translate("revenue_summary", activeLang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Estimated Revenue", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹${totalEstimatedRevenue.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Realized Collected", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹${totalRevenueCollected.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Remaining Pending Balance:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("₹${totalPendingPayments.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.Red)
                            }
                        }
                    }
                }

                // CSV Exporter Action Block
                item {
                    Button(
                        onClick = {
                            scope.launch {
                                val csvData = viewModel.exportToCsvString()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "Accurate_Lab_Collection_Report")
                                    putExtra(Intent.EXTRA_TEXT, csvData)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Clinical CSV Report"))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "CSV Share")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(translate("export_csv", activeLang), fontWeight = FontWeight.Bold)
                    }
                }

                // Header lists for historical register analysis
                item {
                    Text(
                        "Registered Historical Patients Count: ${patients.size}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (patients.isEmpty()) {
                    item {
                        EmptyStatePlaceholder(text = "No collection registers available to prepare analytics reports.", icon = Icons.AutoMirrored.Filled.ListAlt)
                    }
                } else {
                    items(patients, key = { it.id }) { patient ->
                        PatientItemCard(
                            modifier = Modifier.animateItem(),
                            patient = patient,
                            activeLang = activeLang,
                            onSendWhatsApp = { ctx, p -> openWhatsAppWithPatient(ctx, p, viewModel) },
                            onDuplicate = { p -> 
                                viewModel.setTestSelection(viewModel.testsForPatient(p))
                                viewModel.navigateTo("new_entry_dup_${p.id}")
                            },
                            onEdit = { p ->
                                viewModel.setTestSelection(viewModel.testsForPatient(p))
                                viewModel.navigateTo("edit_entry_${p.id}")
                            }
                        )
                    }
                }
            }
    }
}

// ============================================
// 7. LAB CONFIG & SETTINGS SCREEN
// ============================================
