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
fun SettingsScreen(viewModel: LabViewModel) {
    BackHandler {
        viewModel.navigateTo("dashboard")
    }
    val activeLang by viewModel.activeLanguage.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appVersionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.3"
        } catch (e: Exception) {
            "1.3"
        }
    }

    var upiId by remember { mutableStateOf("") }
    var upiName by remember { mutableStateOf("") }
    var whatsappNumbers by remember { mutableStateOf("") }

    val currentUpdateUrl by viewModel.updateServerUrl.collectAsState()
    var updateServerUrlInput by remember { mutableStateOf("") }
    
    LaunchedEffect(currentUpdateUrl) {
        updateServerUrlInput = currentUpdateUrl
    }
    
    // Toast Notification triggers
    var saveTriggeredAlert by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        upiId = settings.labUpiId
        upiName = settings.labUpiName
        whatsappNumbers = settings.labWhatsAppNumbersString
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar
        CustomTopAppBar(
            title = translate("settings", activeLang),
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo("dashboard") }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Language Toggle Option Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("App Language / ભાષા", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(if (activeLang == Language.ENGLISH) "Current: English" else "ચાલુ ભાષા: ગુજરાતી", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { viewModel.toggleLanguage() }) {
                        Icon(Icons.Filled.Language, contentDescription = "Toggle Lang")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (activeLang == Language.ENGLISH) "ગુજરાતી કરો" else "Switch to English", fontSize = 11.sp)
                    }
                }
            }

            // Centralized Server Sync & Hardware Printer Note block
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CloudQueue, contentDescription = "Future Ready", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(translate("future_ready_title", activeLang), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Bluetooth Label & Thermal Printers / Cloud Database Sync APIs built into local schemas automatically.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // QR & Numbers Configuration Fields
            Text("Admin Configurations", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it },
                label = { Text(translate("upi_id_config", activeLang)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("upi_id_config_input")
            )

            OutlinedTextField(
                value = upiName,
                onValueChange = { upiName = it },
                label = { Text(translate("upi_name_config", activeLang)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = whatsappNumbers,
                onValueChange = { whatsappNumbers = it },
                label = { Text(translate("lab_whatsapp", activeLang)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("whatsapp_config_input")
            )

            // ----------------------------------------------------
            // CUSTOM UPI QR UPLOAD CARD
            // ----------------------------------------------------
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("custom_qr_upload_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Static Merchant UPI QR Code",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Upload a static merchant QR code image from your gallery. If uploaded, this custom image will be displayed on patient checkout screens instead of generating a dynamic UPI QR.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val customQrPath by viewModel.customUpiQrPath.collectAsState()
                    val qrImageLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                    ) { uri: android.net.Uri? ->
                        uri?.let {
                            val localPath = viewModel.copyImageToInternalStorage(context, it)
                            if (localPath != null) {
                                viewModel.saveCustomUpiQrPath(localPath)
                            }
                        }
                    }

                    if (customQrPath != null) {
                        val imgFile = java.io.File(customQrPath!!)
                        if (imgFile.exists()) {
                            val bitmap = android.graphics.BitmapFactory.decodeFile(imgFile.absolutePath)
                            if (bitmap != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Uploaded QR Code",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                    )
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Merchant QR Active", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        OutlinedButton(
                                            onClick = { viewModel.saveCustomUpiQrPath(null) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Remove QR", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Remove QR", fontSize = 11.sp)
                                        }
                                    }
                                }
                            } else {
                                Text("Error loading image file", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }
                        } else {
                            Text("Saved QR code file not found", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = { qrImageLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Upload QR")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload Merchant QR Code Image", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // OTA UPDATES SERVER CONFIG CARD
            // ----------------------------------------------------
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("ota_update_config_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "OTA App Updates Server",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Specify your internal secure cloud directory URL hosting the `version.json` file. PathoFlow checks this URL on startup to prompt phlebotomists with instant updates offline/online.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = updateServerUrlInput,
                        onValueChange = { updateServerUrlInput = it },
                        label = { Text("Update Check URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    var isCheckingForUpdate by remember { mutableStateOf(false) }
                    var checkStatusMsg by remember { mutableStateOf<String?>(null) }
                    var hasUpdateAvailable by remember { mutableStateOf(false) }
                    var remoteUpdateVersion by remember { mutableStateOf("") }
                    var remoteUpdateUrl by remember { mutableStateOf("") }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (updateServerUrlInput.isNotBlank()) {
                                    isCheckingForUpdate = true
                                    checkStatusMsg = "Checking..."
                                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        try {
                                            val url = java.net.URL(updateServerUrlInput)
                                            val connection = url.openConnection() as java.net.HttpURLConnection
                                            connection.connectTimeout = 4000
                                            connection.readTimeout = 4000
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

                                                if (remoteCode != null && remoteCode > localCode && remoteName != null && remoteUrl != null) {
                                                    hasUpdateAvailable = true
                                                    remoteUpdateVersion = remoteName
                                                    remoteUpdateUrl = remoteUrl
                                                    checkStatusMsg = "Update Available: Version $remoteName!"
                                                } else {
                                                    hasUpdateAvailable = false
                                                    checkStatusMsg = "Up to date (v$appVersionName)"
                                                }
                                            } else {
                                                checkStatusMsg = "Server error ${connection.responseCode}"
                                            }
                                        } catch (e: Exception) {
                                            checkStatusMsg = "Network connection failed"
                                        } finally {
                                            isCheckingForUpdate = false
                                        }
                                    }
                                } else {
                                    checkStatusMsg = "Please enter a valid URL."
                                }
                            },
                            enabled = !isCheckingForUpdate,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudDownload,
                                contentDescription = "Check Update",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Check for Update", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (hasUpdateAvailable) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(remoteUpdateUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.SystemUpdate, contentDescription = "Install Update", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Install Update", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    if (checkStatusMsg != null) {
                        Text(
                            text = checkStatusMsg!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasUpdateAvailable) Color(0xFF2E7D32) else if (checkStatusMsg!!.startsWith("Up to date")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            if (saveTriggeredAlert) {
                Text("Configurations Saved Successfully", color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Button(
                onClick = {
                    viewModel.saveAppSettings(upiId, upiName, whatsappNumbers)
                    viewModel.saveUpdateServerUrl(updateServerUrlInput)
                    saveTriggeredAlert = true
                    scope.launch {
                        delay(2000)
                        saveTriggeredAlert = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_settings_btn")
            ) {
                Icon(Icons.Filled.Save, contentDescription = "Save Configurations")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Configurations", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))


            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


// ============================================
// REUSABLE DRAWERS & SMALL UTILITIES
// ============================================

