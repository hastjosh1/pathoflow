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
import com.example.data.util.PatientJson
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
    containerColor: Color = MaterialTheme.colorScheme.background
) {
    // Flat, quiet toolbar: sits on the canvas with no tint or elevation,
    // large confident title.
    Surface(
        color = containerColor,
        modifier = Modifier.fillMaxWidth().height(64.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                navigationIcon()
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
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
                // Language Fast Switcher Header Tool — bare icons, no chips
                IconButton(onClick = { viewModel.toggleLanguage() }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Filled.Language, contentDescription = "Switch Fast Language", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { viewModel.navigateTo("settings") }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Filled.Settings, contentDescription = "Configure Lab", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
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
        Icon(imageVector = icon, contentDescription = "Blank List", modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = text, fontSize = 12.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp))
    }
}


@Composable
fun PatientItemCard(
    patient: PatientEntry,
    activeLang: Language,
    onSendWhatsApp: (Context, PatientEntry) -> Unit,
    onDuplicate: (PatientEntry) -> Unit,
    onEdit: (PatientEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Only payment carries color (it's the actionable signal); collection
    // status stays neutral to keep the card quiet.
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
        modifier = modifier
            .fillMaxWidth()
            .testTag("patient_card_${patient.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        patient.id,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(patient.dateString, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(patient.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${patient.age}Y · ${patient.sex.take(1)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Filled.MedicalServices, contentDescription = "Doc", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dr. ${patient.referredDoctor}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            if (patient.address.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PinDrop, contentDescription = "Home Map Pin", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(patient.address, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Collection Status Badge — neutral chip
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = printStatus,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Payment Status Badge
                Surface(
                    color = payColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "$printPayStatus · ₹${patient.amountPayable.toInt()}",
                        color = payColor,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

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
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), CircleShape)
                            .testTag("action_edit_${patient.id}")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Details", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    // Quick Recopy/Duplicate
                    IconButton(
                        onClick = { onDuplicate(patient) },
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f), CircleShape)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate Patient", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    }
                }

                // WhatsApp sender
                Surface(
                    onClick = { onSendWhatsApp(context, patient) },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF25D366),
                    modifier = Modifier.testTag("action_whatsapp_${patient.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Export to Lab WhatsApp", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

// Global WhatsApp intent execution
fun openWhatsAppWithPatient(context: Context, patient: PatientEntry, viewModel: LabViewModel, imageUri: android.net.Uri? = null) {
    try {
        // Build readable layout copy
        val snapshotNames = PatientJson.decodeSnapshots(patient.testsSnapshotJson).map { it.name }
        val listTestsNames = if (snapshotNames.isNotEmpty()) {
            snapshotNames
        } else {
            val ids = PatientJson.decodeIds(patient.selectedTestIdsJson).toSet()
            viewModel.allTests.value.filter { it.id in ids }.map { it.name }
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
                val snapshotNames = PatientJson.decodeSnapshots(patient.testsSnapshotJson).map { it.name }
                val listTestsNames = if (snapshotNames.isNotEmpty()) {
                    snapshotNames
                } else {
                    val ids = PatientJson.decodeIds(patient.selectedTestIdsJson).toSet()
                    viewModel.allTests.value.filter { it.id in ids }.map { it.name }
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
