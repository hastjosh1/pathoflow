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
fun OnboardingSetupScreen(viewModel: LabViewModel) {
    var labName by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("accuratelab@okhdfcbank") }
    var phoneNo by remember { mutableStateOf("9876543210") }
    var uploadedQrPath by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsState()
    val customQrPath by viewModel.customUpiQrPath.collectAsState()

    LaunchedEffect(settings) {
        if (settings.labUpiName.isNotBlank()) {
            labName = settings.labUpiName
        }
        if (settings.labUpiId.isNotBlank()) {
            upiId = settings.labUpiId
        }
        if (settings.labWhatsAppNumbersString.isNotBlank()) {
            phoneNo = settings.labWhatsAppNumbersString
        }
    }

    LaunchedEffect(customQrPath) {
        if (customQrPath != null) {
            uploadedQrPath = customQrPath
        }
    }
    
    val qrImageLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val localPath = viewModel.copyImageToInternalStorage(context, it)
            if (localPath != null) {
                uploadedQrPath = localPath
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                    )
                )
            )
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.pathoflow_logo),
                    contentDescription = "PathoFlow Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
                
                Text(
                    text = "Configure Your Lab Companion",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Configure your laboratory branding, static payment UPI ID, and primary contact number to initialize the system.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Lab/Owner Name
                OutlinedTextField(
                    value = labName,
                    onValueChange = { labName = it },
                    label = { Text("Laboratory/Phlebotomist Name *") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Lab") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Primary WhatsApp Number
                OutlinedTextField(
                    value = phoneNo,
                    onValueChange = { phoneNo = it },
                    label = { Text("WhatsApp Phone Number *") },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // UPI ID
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("Static Merchant UPI ID *") },
                    leadingIcon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "UPI") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Custom UPI QR Code Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Static Merchant UPI QR Code",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (uploadedQrPath != null) {
                            val imgFile = java.io.File(uploadedQrPath!!)
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
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                        )
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("QR Active", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            OutlinedButton(
                                                onClick = { uploadedQrPath = null },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Remove QR", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Remove", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { qrImageLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Upload QR")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload QR Code Image", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                
                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Submit Button
                Button(
                    onClick = {
                        if (labName.isBlank()) {
                            validationError = "Please enter Laboratory/Owner Name!"
                            return@Button
                        }
                        if (phoneNo.trim().length < 10) {
                            validationError = "Please enter a valid 10-digit WhatsApp phone number!"
                            return@Button
                        }
                        if (upiId.isBlank()) {
                            validationError = "Please enter your UPI ID!"
                            return@Button
                        }
                        
                        viewModel.saveOnboardingSettings(
                            labName = labName,
                            upiId = upiId,
                            phoneNumbers = phoneNo,
                            customQrPath = uploadedQrPath
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Save & Get Started", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
}

// ============================================
// 3. DASHBOARD SCREEN
// ============================================
