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

fun downloadAndInstallApk(
    context: Context,
    downloadUrl: String,
    onProgress: (Float) -> Unit,
    onFinished: () -> Unit,
    onError: (String) -> Unit
) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val url = java.net.URL(downloadUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()
            
            if (connection.responseCode != 200) {
                onError("Server error: ${connection.responseCode}")
                return@launch
            }
            
            val fileLength = connection.contentLength
            val inputStream = connection.inputStream
            
            val outputFile = java.io.File(context.cacheDir, "update.apk")
            if (outputFile.exists()) {
                outputFile.delete()
            }
            
            val outputStream = java.io.FileOutputStream(outputFile)
            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            while (inputStream.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    onProgress(total.toFloat() / fileLength.toFloat())
                }
                outputStream.write(data, 0, count)
            }
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            
            onFinished()
            
            val apkUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            
        } catch (e: Exception) {
            android.util.Log.e("OTAUpdate", "Download/Install failed", e)
            onError(e.localizedMessage ?: "Unknown error")
        }
    }
}


@Composable
fun CustomTopAppBar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    Surface(
        color = containerColor,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth().height(64.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                navigationIcon()
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (actions != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    actions()
                }
            }
        }
    }
}

@Composable
fun HeaderToolbar(viewModel: LabViewModel, titleKey: String) {
    val activeLang by viewModel.activeLanguage.collectAsState()
    
    CustomTopAppBar(
        title = translate(titleKey, activeLang),
        actions = {
            Row(
                modifier = androidx.compose.ui.Modifier.padding(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language Fast Switcher Header Tool
                IconButton(
                    onClick = { viewModel.toggleLanguage() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(Icons.Filled.Language, contentDescription = "Switch Fast Language", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = { viewModel.navigateTo("settings") },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Configure Lab", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
        }
    )
}

@Composable
fun EmptyStatePlaceholder(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = "Blank List", modifier = Modifier.size(54.dp), tint = Color.Gray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = text, fontSize = 12.sp, textAlign = TextAlign.Center, color = Color.Gray, modifier = Modifier.padding(horizontal = 24.dp))
    }
}


@Composable
fun PatientItemCard(
    patient: PatientEntry,
    activeLang: Language,
    onSendWhatsApp: (Context, PatientEntry) -> Unit,
    onDuplicate: (PatientEntry) -> Unit,
    onEdit: (PatientEntry) -> Unit
) {
    val context = LocalContext.current
    
    // Setup Badge styling color indicators depending on collectionType and payment tracking
    val statusColor = when(patient.collectionStatus) {
        "Sample Collected" -> Color(0xFF1565C0)
        "Sent to Lab" -> Color(0xFFEF6C00)
        "Report Ready" -> Color(0xFF2E7D32)
        else -> Color.DarkGray
    }

    val payColor = when(patient.paymentStatus) {
        "Paid" -> Color(0xFF2E7D32)
        "Partial" -> Color(0xFFE65100)
        else -> Color(0xFFC62828)
    }

    val printStatus = when(patient.collectionStatus) {
        "Sample Collected" -> translate("sample_collected", activeLang)
        "Sent to Lab" -> translate("sent_to_lab", activeLang)
        "Report Ready" -> translate("report_ready", activeLang)
        else -> translate("delivered", activeLang)
    }

    val printPayStatus = when(patient.paymentStatus) {
        "Paid" -> translate("paid", activeLang)
        "Partial" -> translate("partial", activeLang)
        else -> translate("pending", activeLang)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("patient_card_${patient.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(patient.id, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                // Date time tag
                Text(patient.dateString, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(patient.name, fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1)
            
            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Age/Sex: ${patient.age}Y/${patient.sex.take(1)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Filled.MedicalServices, contentDescription = "Doc", modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(2.dp))
                Text("Dr. ${patient.referredDoctor}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (patient.address.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PinDrop, contentDescription = "Home Map Pin", modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(patient.address, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Collection Status Badge
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Collection: $printStatus",
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Payment Status Badge
                Surface(
                    color = payColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Payment: $printPayStatus (₹${patient.amountPayable.toInt()})",
                        color = payColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Action triggers bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Update patient details (edit)
                    IconButton(
                        onClick = { onEdit(patient) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
                            .testTag("action_edit_${patient.id}")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Details", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    // Quick Recopy/Duplicate
                    IconButton(
                        onClick = { onDuplicate(patient) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate Patient", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    }
                }

                // WhatsApp sender
                IconButton(
                    onClick = { onSendWhatsApp(context, patient) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF25D366), CircleShape)
                        .testTag("action_whatsapp_${patient.id}")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = "Export to Lab WhatsApp", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// Global WhatsApp intent execution
fun openWhatsAppWithPatient(context: Context, patient: PatientEntry, viewModel: LabViewModel, imageUri: android.net.Uri? = null) {
    try {
        // Build readable layout copy
        val listTestsNames = if (patient.testsSnapshotJson != "[]" && patient.testsSnapshotJson.isNotEmpty()) {
            val regex = "\"name\":\"([^\"]+)\"".toRegex()
            regex.findAll(patient.testsSnapshotJson).map { it.groupValues[1] }.toList()
        } else {
            viewModel.allTests.value.filter { test ->
                patient.selectedTestIdsJson.contains(test.id.toString())
            }.map { it.name }
        }
        
        val bodyContent = viewModel.formatWhatsAppMessage(patient, listTestsNames)
        
        // WhatsApp numbers
        val numbersList = viewModel.settingsState.value.labWhatsAppNumbersString.split(",")
        val targetNum = numbersList.firstOrNull()?.trim() ?: "919876543210"
        
        val cleanNumber = targetNum.replace("+", "").replace(" ", "").trim()

        if (imageUri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_TEXT, bodyContent)
                putExtra(Intent.EXTRA_STREAM, imageUri)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra("jid", "$cleanNumber@s.whatsapp.net")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share confirmation image via WhatsApp"))
        } else {
            val encodedMsg = java.net.URLEncoder.encode(bodyContent, "UTF-8")
            val sendUri = "https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMsg"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(sendUri)
            }
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        Log.e("LabWhatsApp", "Error opening WhatsApp intent", e)
        if (imageUri != null) {
            try {
                val numbersList = viewModel.settingsState.value.labWhatsAppNumbersString.split(",")
                val targetNum = numbersList.firstOrNull()?.trim() ?: "919876543210"
                val cleanNumber = targetNum.replace("+", "").replace(" ", "").trim()
                val listTestsNames = if (patient.testsSnapshotJson != "[]" && patient.testsSnapshotJson.isNotEmpty()) {
                    val regex = "\"name\":\"([^\"]+)\"".toRegex()
                    regex.findAll(patient.testsSnapshotJson).map { it.groupValues[1] }.toList()
                } else {
                    viewModel.allTests.value.filter { test ->
                        patient.selectedTestIdsJson.contains(test.id.toString())
                    }.map { it.name }
                }
                val bodyContent = viewModel.formatWhatsAppMessage(patient, listTestsNames)
                val encodedMsg = java.net.URLEncoder.encode(bodyContent, "UTF-8")
                val sendUri = "https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMsg"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse(sendUri)
                }
                context.startActivity(intent)
            } catch (fallbackEx: Exception) {
                Log.e("LabWhatsApp", "Fallback failed as well", fallbackEx)
            }
        }
    }
}
