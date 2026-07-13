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
fun DashboardScreen(viewModel: LabViewModel) {
    val activeLang by viewModel.activeLanguage.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val patients by viewModel.allPatients.collectAsState()

    val context = LocalContext.current
    val appVersionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.3"
        } catch (e: Exception) {
            "1.3"
        }
    }
    val currentUpdateUrl by viewModel.updateServerUrl.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }
    var latestVersionName by remember { mutableStateOf("") }
    var apkDownloadUrl by remember { mutableStateOf("") }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUpdateUrl) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                if (currentUpdateUrl.isNotBlank()) {
                    val url = java.net.URL(currentUpdateUrl)
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 3000
                    connection.readTimeout = 3000
                    connection.requestMethod = "GET"
                    if (connection.responseCode == 200) {
                        val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                        val codeRegex = "\"versionCode\"\\s*:\\s*(\\d+)".toRegex()
                        val nameRegex = "\"versionName\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                        val urlRegex = "\"apkUrl\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                        
                        val remoteCode = codeRegex.find(jsonText)?.groupValues?.get(1)?.toIntOrNull()
                        val remoteName = nameRegex.find(jsonText)?.groupValues?.get(1)
                        val remoteUrl = urlRegex.find(jsonText)?.groupValues?.get(1)
                        
                        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        val localCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            packageInfo.longVersionCode.toInt()
                        } else {
                            @Suppress("DEPRECATION")
                            packageInfo.versionCode
                        }
                        
                        android.util.Log.d("OTAUpdate", "Check - Remote: $remoteCode, Local: $localCode")
                        if (remoteCode != null && remoteCode > localCode && remoteName != null && remoteUrl != null) {
                            latestVersionName = remoteName
                            apkDownloadUrl = remoteUrl
                            showUpdateDialog = true
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("OTAUpdateCheck", "Failed to check for updates", e)
            }
        }
    }
    
    // Compute Daily Metrics
    val todayDateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    val todayPatients = patients.filter { it.dateString.startsWith(todayDateStr) }
    val todayRevenue = todayPatients.sumOf { it.amountPayable }
    val pendingPayments = patients.filter { it.paymentStatus == "Pending" || it.paymentStatus == "Partial" }
    val pendingCount = pendingPayments.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header Bar
        HeaderToolbar(viewModel = viewModel, titleKey = "dashboard")

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (settings.labUpiName.ifBlank { "L" }).take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hello, ${settings.labUpiName.ifBlank { "Phlebotomist" }}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Accurate Lab Field Companion • Offline Ready • v$appVersionName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Metric Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = translate("today_patients", activeLang),
                        value = "${todayPatients.size}",
                        icon = Icons.Filled.People,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = translate("today_revenue", activeLang),
                        value = "₹${todayRevenue.toInt()}",
                        icon = Icons.Filled.AccountBalanceWallet,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = translate("pending_payments", activeLang),
                        value = "$pendingCount Cases",
                        icon = Icons.Filled.HourglassEmpty,
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                    // Quick Action Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(128.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { viewModel.navigateTo("new_entry") }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "New Patient",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = translate("new_entry", activeLang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Quick Actions Segment
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickButton(
                        text = translate("manage_tests", activeLang),
                        icon = Icons.Filled.Biotech,
                        onClick = { viewModel.navigateTo("tests") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    QuickButton(
                        text = translate("reports", activeLang),
                        icon = Icons.Filled.Analytics,
                        onClick = { viewModel.navigateTo("reports") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    QuickButton(
                        text = translate("settings", activeLang),
                        icon = Icons.Filled.Settings,
                        onClick = { viewModel.navigateTo("settings") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Entries Segment Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = translate("recent_entries", activeLang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = { viewModel.navigateTo("reports") }) {
                        Text("View All", fontSize = 12.sp)
                    }
                }
            }

            // List of patients
            if (patients.isEmpty()) {
                item {
                    EmptyStatePlaceholder(
                        text = "No patient collection entries registered yet. Tap 'New Patient Entry' to get started.",
                        icon = Icons.Filled.AssignmentLate
                    )
                }
            } else {
                items(patients.take(10), key = { it.id }) { patient ->
                    PatientItemCard(
                        modifier = Modifier.animateItem(),
                        patient = patient,
                        activeLang = activeLang,
                        onSendWhatsApp = { context, p ->
                            openWhatsAppWithPatient(context, p, viewModel)
                        },
                        onDuplicate = { p ->
                            // Preload test selections & navigate to fill form
                            viewModel.setTestSelection(viewModel.testsForPatient(p))
                            // We can use a shared state or navigations to copy fields.
                            // To keep it clean, let's provide duplicating on the patient screen
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

        if (showUpdateDialog) {
            AlertDialog(
                onDismissRequest = { 
                    if (!isDownloading) {
                        showUpdateDialog = false 
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CloudQueue, 
                            contentDescription = "System Update", 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDownloading) "Downloading Update..." else "New Update Available!", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 16.sp
                        )
                    }
                },
                text = {
                    Column {
                        if (isDownloading) {
                            Text("Downloading Accurate Lab App Version $latestVersionName directly to your device...", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${(downloadProgress * 100).toInt()}% completed", 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.End)
                            )
                            
                            if (downloadError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Error: $downloadError", 
                                    color = MaterialTheme.colorScheme.error, 
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text("A fresh new update (Version $latestVersionName) is ready for the Accurate Lab app.", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("This internal update includes critical workflow optimizations, logo assets, and new diagnostic test listings.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                confirmButton = {
                    if (isDownloading) {
                        if (downloadError != null) {
                            Button(
                                onClick = {
                                    downloadError = null
                                    downloadProgress = 0f
                                    downloadAndInstallApk(
                                        context = context,
                                        downloadUrl = apkDownloadUrl,
                                        onProgress = { downloadProgress = it },
                                        onFinished = { 
                                            isDownloading = false
                                            showUpdateDialog = false
                                        },
                                        onError = { downloadError = it }
                                    )
                                }
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                isDownloading = true
                                downloadError = null
                                downloadProgress = 0f
                                downloadAndInstallApk(
                                    context = context,
                                    downloadUrl = apkDownloadUrl,
                                    onProgress = { downloadProgress = it },
                                    onFinished = { 
                                        isDownloading = false
                                        showUpdateDialog = false
                                    },
                                    onError = { downloadError = it }
                                )
                            }
                        ) {
                            Text("Update Now", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    if (isDownloading) {
                        if (downloadError != null) {
                            TextButton(
                                onClick = { 
                                    isDownloading = false
                                    showUpdateDialog = false 
                                }
                            ) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        TextButton(onClick = { showUpdateDialog = false }) {
                            Text("Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(128.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun QuickButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(84.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
