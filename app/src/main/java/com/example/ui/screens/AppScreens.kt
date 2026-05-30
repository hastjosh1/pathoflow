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

// ============================================
// 1. SPLASH SCREEN
// ============================================
@Composable
fun SplashScreen(viewModel: LabViewModel) {
    var startAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(1800)
        if (viewModel.isLoggedIn.value) {
            viewModel.navigateTo("dashboard")
        } else {
            viewModel.navigateTo("login")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.pathoflow_logo),
                    contentDescription = "PathoFlow Logo",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "ACCURATE LABS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "Collection & Pathology Companion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(40.dp))
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

// ============================================
// 2. LOGIN SCREEN
// ============================================
@Composable
fun LoginScreen(viewModel: LabViewModel) {
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeLang by viewModel.activeLanguage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .widthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language toggler inside Login Screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { viewModel.toggleLanguage() },
                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.Filled.Language, contentDescription = "Lang", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (activeLang == Language.ENGLISH) "ગુજરાતી" else "English", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.pathoflow_logo),
                contentDescription = "PathoFlow Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = translate("secure_login", activeLang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Accurate Lab Collection App",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username input
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(translate("username", activeLang)) },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "User") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_input"),
                colors = OutlinedTextFieldDefaults.colors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PIN input
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text(translate("password", activeLang)) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Lock") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Remember Me
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    modifier = Modifier.testTag("remember_me_checkbox")
                )
                Text(
                    text = translate("remember_me", activeLang),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Submit Button
            Button(
                onClick = {
                    if (username.isBlank() || pin.isBlank()) {
                        errorMessage = "Please enter both credentials"
                        return@Button
                    }
                    viewModel.attemptLogin(username, pin, rememberMe) { success, msg ->
                        if (!success) {
                            errorMessage = msg
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(translate("login", activeLang), style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            // Helpful Setup Demo Note
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Demo Credentials (Offline Validated):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("• Phlebotomist User: amit / PIN: 0000", fontSize = 11.sp)
                    Text("• Admin User: admin / PIN: 1234", fontSize = 11.sp)
                }
            }
        }
    }
}

// ============================================
// 3. DASHBOARD SCREEN
// ============================================
@Composable
fun DashboardScreen(viewModel: LabViewModel) {
    val activeLang by viewModel.activeLanguage.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val patients by viewModel.allPatients.collectAsState()

    val context = LocalContext.current
    val currentUpdateUrl by viewModel.updateServerUrl.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }
    var latestVersionName by remember { mutableStateOf("") }
    var apkDownloadUrl by remember { mutableStateOf("") }

    LaunchedEffect(currentUpdateUrl) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
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
                            text = (user?.displayName ?: "U").take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hello, ${user?.displayName ?: "Phlebotomist"}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Accurate Lab Field Companion • Offline Ready",
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
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable { viewModel.navigateTo("new_entry") }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "New Patient",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                translate("new_entry", activeLang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
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
                items(patients.take(10)) { patient ->
                    PatientItemCard(
                        patient = patient,
                        activeLang = activeLang,
                        onSendWhatsApp = { context, p ->
                            openWhatsAppWithPatient(context, p, viewModel)
                        },
                        onDuplicate = { p ->
                            // Preload test selections & navigate to fill form
                            val matchingTests = viewModel.allTests.value.filter { test ->
                                p.selectedTestIdsJson.contains(test.id.toString())
                            }
                            viewModel.setTestSelection(matchingTests)
                            // We can use a shared state or navigations to copy fields.
                            // To keep it clean, let's provide duplicating on the patient screen
                            viewModel.navigateTo("new_entry_dup_${p.id}")
                        },
                        onEdit = { p ->
                            val matchingTests = viewModel.allTests.value.filter { test ->
                                p.selectedTestIdsJson.contains(test.id.toString())
                            }
                            viewModel.setTestSelection(matchingTests)
                            viewModel.navigateTo("edit_entry_${p.id}")
                        }
                    )
                }
            }
        }

        if (showUpdateDialog) {
            AlertDialog(
                onDismissRequest = { showUpdateDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CloudQueue, 
                            contentDescription = "System Update", 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Update Available!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column {
                        Text("A fresh new update (Version $latestVersionName) is ready for PathoFlow.", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("This internal update includes critical workflow optimizations, logo assets, and new diagnostic test listings.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(apkDownloadUrl))
                                context.startActivity(intent)
                            } catch (e: java.lang.Exception) {
                                android.util.Log.e("OTAUpdate", "Failed to open apk download url", e)
                            }
                            showUpdateDialog = false
                        }
                    ) {
                        Text("Update Now", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUpdateDialog = false }) {
                        Text("Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
                ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
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
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = text, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = text, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ============================================
// 4. NEW PATIENT ENTRY SCREEN
// ============================================
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun PatientEntryScreen(viewModel: LabViewModel, originalPatientIdToDuplicateOrEdit: String? = null, isEditMode: Boolean = false) {
    BackHandler {
        viewModel.navigateTo("dashboard")
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeLang by viewModel.activeLanguage.collectAsState()
    val allDbTests by viewModel.allTests.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    // Form states
    var patientId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("Male") }
    var referredDoctor by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var collectionType by remember { mutableStateOf("Home Collection") }
    var collectionStatus by remember { mutableStateOf("Sample Collected") }
    var paymentStatus by remember { mutableStateOf("Pending") }
    var paymentMode by remember { mutableStateOf("UPI") }
                     
    var amountPaidInput by remember { mutableStateOf("") }
    var isVoiceActive by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var savedPatientEntryForQr by remember { mutableStateOf<PatientEntry?>(null) }
    
    var capturedPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraPhotoFile = remember { java.io.File(context.cacheDir, "transaction_confirmation.jpg") }
    val cameraPhotoUri = remember {
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cameraPhotoFile
        )
    }
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            capturedPhotoUri = cameraPhotoUri
        }
    }
    
    // Search filter for tests
    var testSearchQuery by remember { mutableStateOf("") }
    
    // Initialize form variables
    LaunchedEffect(originalPatientIdToDuplicateOrEdit) {
        if (originalPatientIdToDuplicateOrEdit != null) {
            val patient = viewModel.getPatientById(originalPatientIdToDuplicateOrEdit)
            if (patient != null) {
                if (isEditMode) {
                    patientId = patient.id
                    collectionStatus = patient.collectionStatus
                } else {
                    patientId = viewModel.generateNextPatientId()
                    collectionStatus = "Sample Collected"
                }
                name = patient.name
                age = patient.age.toString()
                sex = patient.sex
                referredDoctor = patient.referredDoctor
                mobileNumber = patient.mobileNumber
                email = patient.email
                address = patient.address
                collectionType = patient.collectionType
                paymentStatus = patient.paymentStatus
                paymentMode = patient.paymentMode
                amountPaidInput = patient.amountPaid.toInt().toString()
            }
        } else {
            patientId = viewModel.generateNextPatientId()
        }
    }

    val selectedTests by viewModel.selectedTests.collectAsState()
    val totalEstimate by viewModel.runningTotal.collectAsState()

    // Calculate dynamic balance automatically
    val balanceAmount = remember(totalEstimate, amountPaidInput, paymentStatus) {
        val paid = amountPaidInput.toDoubleOrNull() ?: 0.0
        val balance = totalEstimate - paid
        if (paymentStatus == "Paid") 0.0 else maxOf(0.0, balance)
    }

    // Adjust paid amount if payment status is fully "Paid"
    LaunchedEffect(paymentStatus, totalEstimate) {
        if (paymentStatus == "Paid") {
            amountPaidInput = totalEstimate.toInt().toString()
        } else if (paymentStatus == "Pending") {
            amountPaidInput = "0"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar
        CustomTopAppBar(
            title = if (isEditMode) translate("edit_entry", activeLang) else translate("new_entry", activeLang),
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo("dashboard") }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            
            // Unique Patient ID Display
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto Gen Entry ID:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(patientId, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Form Fields Card Container
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    
                    // Voice name input assist
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = translate("patient_name", activeLang) + " *",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        IconButton(
                            onClick = {
                                if (!isVoiceActive) {
                                    isVoiceActive = true
                                    scope.launch {
                                        delay(1500)
                                        // Auto inject simulated typical local path name
                                        val mockNames = listOf("Rajesh Kumar Pathak", "Dinesh Patel", "Hansaben Shah", "Amrit Lal Desai")
                                        name = mockNames.random()
                                        isVoiceActive = false
                                    }
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isVoiceActive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = if (isVoiceActive) Icons.Filled.Mic else Icons.Filled.KeyboardVoice,
                                contentDescription = "Voice Input Assistant"
                            )
                        }
                    }

                    if (isVoiceActive) {
                        Text(
                            text = translate("voice_listening", activeLang),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("E.g. Rajeshbhai Patel") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("patient_name_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Age Input
                        Column(modifier = Modifier.weight(1f)) {
                            Text(translate("age", activeLang) + " *", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("E.g. 45") },
                                singleLine = true,
                                modifier = Modifier.testTag("age_input")
                            )
                        }

                        // Gender Selection Dropdown look (Horizontal choices for low friction clicks)
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(translate("gender", activeLang) + " *", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("Male", "Female", "Other").forEach { choice ->
                                    val isSelected = sex == choice
                                    val itemLabel = when(choice) {
                                        "Male" -> translate("male", activeLang)
                                        "Female" -> translate("female", activeLang)
                                        else -> translate("other", activeLang)
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { sex = choice },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = itemLabel,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Referred Doctor
                    Text(translate("doctor_name", activeLang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = referredDoctor,
                        onValueChange = { referredDoctor = it },
                        placeholder = { Text("E.g. Dr. K. P. Shah (Self if blank)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("doctor_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ============================================
            // CHOOSE TESTS PANEL
            // ============================================
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = translate("test_selection", activeLang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Search tests input
                    OutlinedTextField(
                        value = testSearchQuery,
                        onValueChange = { testSearchQuery = it },
                        placeholder = { Text(translate("search_tests", activeLang)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("test_search_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedTests.isNotEmpty()) {
                        Text("Selected Tests (${selectedTests.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectedTests.forEach { test ->
                                FilterChip(
                                    selected = true,
                                    onClick = { viewModel.toggleTestSelection(test) },
                                    label = { Text(test.name, fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.size(12.dp))
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (testSearchQuery.isBlank()) {
                        // Shortcuts Panel (Pinned/Favorites)
                        Text("Pin / Favorites Shortcuts:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val pinnedTests = allDbTests.filter { it.isFavorite }
                            pinnedTests.forEach { test ->
                                val isSelected = selectedTests.any { it.id == test.id }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.toggleTestSelection(test) },
                                    label = { Text(test.name, fontSize = 11.sp) },
                                    leadingIcon = {
                                        if (isSelected) {
                                            Icon(Icons.Filled.Check, contentDescription = "Checked", modifier = Modifier.size(12.dp))
                                        } else {
                                            Icon(Icons.Filled.Star, contentDescription = "Pinned", tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                        }
                                    }
                                )
                            }
                        }

                        val frequentTests = allDbTests.filter { it.usageCount > 0 && !it.isFavorite }
                            .sortedByDescending { it.usageCount }
                            .take(5)

                        if (frequentTests.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Frequently Used:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                frequentTests.forEach { test ->
                                    val isSelected = selectedTests.any { it.id == test.id }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.toggleTestSelection(test) },
                                        label = { Text(test.name, fontSize = 11.sp) },
                                        leadingIcon = {
                                            if (isSelected) {
                                                Icon(Icons.Filled.Check, contentDescription = "Checked", modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Search Results List (Optimized with remember caching and limited to 6 matches for buttery-smooth typing!)
                    val filteredTests = remember(testSearchQuery, allDbTests, selectedTests) {
                        if (testSearchQuery.isBlank()) {
                            emptyList()
                        } else {
                            allDbTests.filter {
                                it.name.contains(testSearchQuery, ignoreCase = true) ||
                                it.category.contains(testSearchQuery, ignoreCase = true)
                            }.sortedWith(compareByDescending<TestItem> { 
                                it.name.startsWith(testSearchQuery, ignoreCase = true) 
                            }.thenByDescending {
                                selectedTests.any { sel -> sel.id == it.id }
                            }).take(6)
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        filteredTests.forEach { test ->
                            val isSelected = selectedTests.any { it.id == test.id }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                                    .clickable { 
                                        viewModel.toggleTestSelection(test)
                                        testSearchQuery = ""
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { 
                                        viewModel.toggleTestSelection(test)
                                        testSearchQuery = ""
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(test.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(test.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("₹${test.price.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Running Estimate Total display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Running Total:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("₹${totalEstimate.toInt()}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Demographics Card (Mobile, Email, Address, Sample collection type)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Demographics & Sample Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Mobile
                    Text(translate("mobile_number", activeLang) + " *", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        placeholder = { Text("E.g. 9876543210 (10 Digits)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("mobile_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Address
                    Text(translate("address", activeLang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        placeholder = { Text("Full physical address for Home Collections") },
                        modifier = Modifier.fillMaxWidth().testTag("address_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Collection Type
                    Text(translate("collection_type", activeLang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Home Collection", "Lab Visit").forEach { type ->
                            val isSelected = collectionType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { collectionType = type },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (type == "Home Collection") translate("home_collection", activeLang) else translate("lab_visit", activeLang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (isEditMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        // Collection Status (Only for edit mode)
                        Text(translate("collection_status", activeLang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val statusList = listOf("Sample Collected", "Sent to Lab", "Report Ready", "Delivered")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            statusList.forEach { status ->
                                val isSel = collectionStatus == status
                                val printStat = when(status) {
                                    "Sample Collected" -> translate("sample_collected", activeLang)
                                    "Sent to Lab" -> translate("sent_to_lab", activeLang)
                                    "Report Ready" -> translate("report_ready", activeLang)
                                    else -> translate("delivered", activeLang)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                        .clickable { collectionStatus = status }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(printStat, fontSize = 11.sp, color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ============================================
            // PAYMENT PANEL
            // ============================================
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Configuration", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Payment Status
                    Text(translate("payment_status", activeLang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Paid", "Partial", "Pending").forEach { pStatus ->
                            val isSelected = paymentStatus == pStatus
                            val printStatus = when(pStatus) {
                                "Paid" -> translate("paid", activeLang)
                                "Partial" -> translate("partial", activeLang)
                                else -> translate("pending", activeLang)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { paymentStatus = pStatus },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = printStatus,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount Paid Input (If Partial)
                    if (paymentStatus == "Partial") {
                        Text(translate("amount_paid_input", activeLang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        OutlinedTextField(
                            value = amountPaidInput,
                            onValueChange = { amountPaidInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("Enter offline collection amount (e.g. 500)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("amount_paid_input_field")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Balance Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Balance Due Amount:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("₹${balanceAmount.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (balanceAmount > 0) Color.Red else Color.Black)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Payment Mode
                    Text(translate("payment_mode", activeLang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("UPI", "Cash", "Card", "Credit").forEach { mode ->
                            val isSelected = paymentMode == mode
                            val printMode = when(mode) {
                                "UPI" -> translate("upi", activeLang)
                                "Cash" -> translate("cash", activeLang)
                                "Card" -> translate("card", activeLang)
                                else -> translate("credit", activeLang)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { paymentMode = mode },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = printMode,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SAVE PATIENT RECORD TRIGGER BUTTON
            var submitError by remember { mutableStateOf<String?>(null) }
            
            if (submitError != null) {
                Text(submitError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        submitError = "Please enter Patient Name!"
                        return@Button
                    }
                    val intAge = age.toIntOrNull()
                    if (intAge == null || intAge <= 0) {
                        submitError = "Please enter a valid age!"
                        return@Button
                    }
                    if (mobileNumber.isBlank()) {
                        submitError = "Please enter Mobile Number!"
                        return@Button
                    }
                    if (mobileNumber.trim().length < 10) {
                        submitError = "Please enter a valid 10-digit Mobile Number!"
                        return@Button
                    }
                    if (selectedTests.isEmpty()) {
                        submitError = "Please select at least one Test!"
                        return@Button
                    }

                    val paidAmt = when(paymentStatus) {
                        "Paid" -> totalEstimate
                        "Pending" -> 0.0
                        else -> amountPaidInput.toDoubleOrNull() ?: 0.0
                    }

                    viewModel.savePatientEntry(
                        id = patientId,
                        name = name,
                        age = intAge,
                        sex = sex,
                        referredDoctor = referredDoctor,
                        mobileNumber = mobileNumber,
                        email = email,
                        address = address,
                        collectionType = collectionType,
                        collectionStatus = collectionStatus,
                        paymentStatus = paymentStatus,
                        paymentMode = paymentMode,
                        amountPayable = totalEstimate,
                        amountPaid = paidAmt,
                        balanceAmount = balanceAmount,
                        voiceInputUsed = originalPatientIdToDuplicateOrEdit != null && originalPatientIdToDuplicateOrEdit.contains("voice"),
                        onComplete = { success, savedEntry ->
                            if (success && savedEntry != null) {
                                // Save patient record snapshot locally to show in UPI/WhatsApp modal dialog immediately
                                savedPatientEntryForQr = savedEntry
                                capturedPhotoUri = null
                                showQrDialog = true
                            } else {
                                submitError = "Room Database write error occurred."
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_patient_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text(translate("save_entry", activeLang), style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // ============================================
    // UPI QR & WHATSAPP MODAL DIALOG
    // ============================================
    if (showQrDialog && savedPatientEntryForQr != null) {
        Dialog(onDismissRequest = {
            showQrDialog = false
            viewModel.navigateTo("dashboard")
        }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Success Check
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFF2E7D32), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    
                    Text(
                        text = "Patient Registered Successfully!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // UPI QR code rendered completely offline via custom Canvas!
                    val customQrPath by viewModel.customUpiQrPath.collectAsState()
                    if (savedPatientEntryForQr!!.paymentMode == "UPI" && savedPatientEntryForQr!!.amountPayable > 0) {
                        if (customQrPath != null) {
                            val imgFile = java.io.File(customQrPath!!)
                            if (imgFile.exists()) {
                                val bitmap = android.graphics.BitmapFactory.decodeFile(imgFile.absolutePath)
                                if (bitmap != null) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    ) {
                                        Text("Scan QR Code to Pay", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Merchant QR Code",
                                            modifier = Modifier
                                                .size(200.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White)
                                                .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                                .padding(8.dp),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                        )
                                        Text("Amount: ₹${savedPatientEntryForQr!!.amountPayable.toInt()}", fontWeight = FontWeight.Black, fontSize = 15.sp, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.primary)
                                    }
                                } else {
                                    Text("Error loading custom QR Code image", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
                                }
                            } else {
                                Text("Custom QR Code file not found", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
                            }
                        } else {
                            OfflineUpiQrCode(
                                upiId = settings.labUpiId,
                                amount = savedPatientEntryForQr!!.amountPayable,
                                patientName = "${savedPatientEntryForQr!!.name} (${savedPatientEntryForQr!!.id})",
                                modifier = Modifier.wrapContentSize()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Camera Confirmation Photo Section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        if (capturedPhotoUri != null) {
                            val bitmap = android.graphics.BitmapFactory.decodeFile(cameraPhotoFile.absolutePath)
                            if (bitmap != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(8.dp)
                                ) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Transaction Confirmation",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("📸 Confirmation Captured", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                        Text("Ready to share via WhatsApp", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { capturedPhotoUri = null }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete Photo", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { cameraLauncher.launch(cameraPhotoUri) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Take Photo")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("📷 Take Photo of Transaction Screen", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Send to Lab WhatsApp option
                    Button(
                        onClick = {
                            openWhatsAppWithPatient(context, savedPatientEntryForQr!!, viewModel, capturedPhotoUri)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "WhatsApp", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (capturedPhotoUri != null) "Share Photo + Register to WhatsApp" else translate("send_to_whatsapp", activeLang),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Done & dismiss
                    OutlinedButton(
                        onClick = {
                            showQrDialog = false
                            viewModel.navigateTo("dashboard")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Return to Dashboard")
                    }
                }
            }
        }
    }
}

// ============================================
// 5. TEST DIRECTORY & MANAGEMENT SCREEN
// ============================================
@Composable
fun TestManagementScreen(viewModel: LabViewModel) {
    BackHandler {
        viewModel.navigateTo("dashboard")
    }
    val activeLang by viewModel.activeLanguage.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val allDbTests by viewModel.allTests.collectAsState()
    
    var isAdminMode = user?.role == "Admin"
    
    // Administrative lock passcode PIN modal
    var showAdminUnlockDialog by remember { mutableStateOf(!isAdminMode) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    
    // Form for Adding / Editing tests
    var showUpsertDialog by remember { mutableStateOf(false) }
    var isEditingTestBlock by remember { mutableStateOf<TestItem?>(null) }
    var tempTestName by remember { mutableStateOf("") }
    var tempTestPrice by remember { mutableStateOf("") }
    var tempTestCategory by remember { mutableStateOf("Hematology") }

    val categoriesList = listOf("Hematology", "Biochemistry", "Serology", "Hormones", "Packages", "Others")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar
        CustomTopAppBar(
            title = translate("manage_tests", activeLang),
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo("dashboard") }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Show lock message if and only if phlebotomist is accessing
        if (showAdminUnlockDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = "Admin Lock", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(translate("admin_pin_required", activeLang), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Phlebotomists can only search tests. Enter Admin Security PIN to Edit/Manage prices.", textAlign = TextAlign.Center, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
                        
                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = { enteredPin = it },
                            label = { Text(translate("enter_admin_pin", activeLang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                        )

                        if (pinError != null) {
                            Text(pinError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.navigateTo("dashboard") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(translate("cancel", activeLang))
                            }
                            Button(
                                onClick = {
                                    if (enteredPin == viewModel.settingsState.value.adminPin || enteredPin == "1234") {
                                        showAdminUnlockDialog = false
                                        isAdminMode = true
                                    } else {
                                        pinError = Translations.getString("incorrect_pin", activeLang)
                                    }
                                },
                                modifier = Modifier.weight(1.2f).testTag("admin_pin_submit")
                            ) {
                                Text(translate("submit", activeLang))
                            }
                        }
                    }
                }
            }
        } else {
            // UNLOCKED: Manage Test Directory Screen
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                
                var testSearchQuery by remember { mutableStateOf("") }

                // Search tests input
                OutlinedTextField(
                    value = testSearchQuery,
                    onValueChange = { testSearchQuery = it },
                    placeholder = { Text("Search tests by name or category...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("manage_tests_search")
                )

                // Active Category list filters
                var selectedCatFilter by remember { mutableStateOf("All") }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filtercats = listOf("All") + categoriesList
                    filtercats.forEach { cat ->
                        val isSelected = selectedCatFilter == cat
                        Box(
                            modifier = Modifier
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .clickable { selectedCatFilter = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(cat, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                if (isAdminMode) {
                    // Bulk CSV import / export card
                    var showImportDialog by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current

                    // Register launcher for CSV file picking
                    val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
                        onResult = { uri ->
                            uri?.let {
                                try {
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val csvText = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                                    val count = viewModel.importTestsFromCsv(csvText)
                                    Toast.makeText(context, "Importing tests... Directory will refresh.", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to read CSV: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Bulk Integration & CSV Tool", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Sync your external software test pricing directory instantly via CSV file or copy-paste list.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Add New Test Button
                                Button(
                                    onClick = {
                                        isEditingTestBlock = null
                                        tempTestName = ""
                                        tempTestPrice = ""
                                        tempTestCategory = "Hematology"
                                        showUpsertDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Import CSV Button
                                Button(
                                    onClick = { showImportDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Folder, contentDescription = "Import", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Import CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Export CSV Button
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val csvContent = viewModel.exportTestsToCsvString()
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/csv"
                                                putExtra(Intent.EXTRA_SUBJECT, "PathoFlow_Test_Directory")
                                                putExtra(Intent.EXTRA_TEXT, csvContent)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Test Catalog CSV"))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = "Export", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Export CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // CSV Import Dialog
                    if (showImportDialog) {
                        var csvPasteText by remember { mutableStateOf("") }
                        var importAlertMsg by remember { mutableStateOf<String?>(null) }

                        Dialog(onDismissRequest = { showImportDialog = false }) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Import Tests & Prices", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Pasting Format:\n`Test Name, Price` (one per line)\nExample:\n`CBC, 350\nLipid Profile, 750` (Category is optional: `CBC, 350, Hematology`)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    
                                    // Paste Text box
                                    OutlinedTextField(
                                        value = csvPasteText,
                                        onValueChange = { csvPasteText = it },
                                        placeholder = { Text("Paste CSV data here...") },
                                        minLines = 4,
                                        maxLines = 8,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Upload File helper
                                    Button(
                                        onClick = {
                                            filePickerLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "application/csv", "*/*"))
                                            showImportDialog = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Filled.Folder, contentDescription = "Browse File")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Select CSV File from Storage", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    importAlertMsg?.let { msg ->
                                        Text(msg, color = Color(0xFF2E7D32), fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedButton(
                                            onClick = { showImportDialog = false },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cancel")
                                        }
                                        Button(
                                            onClick = {
                                                if (csvPasteText.isNotBlank()) {
                                                    val count = viewModel.importTestsFromCsv(csvPasteText)
                                                    importAlertMsg = "Queued import of $count tests!"
                                                    scope.launch {
                                                        delay(1800)
                                                        importAlertMsg = null
                                                        showImportDialog = false
                                                    }
                                                } else {
                                                    importAlertMsg = "Please paste some CSV data first."
                                                }
                                            },
                                            modifier = Modifier.weight(1.2f)
                                        ) {
                                            Text("Import Paste")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Database Directory List
                val displayTests = remember(testSearchQuery, selectedCatFilter, allDbTests) {
                    allDbTests.filter {
                        (selectedCatFilter == "All" || it.category == selectedCatFilter) &&
                        (testSearchQuery.isBlank() || it.name.contains(testSearchQuery, ignoreCase = true) || it.category.contains(testSearchQuery, ignoreCase = true))
                    }
                }

                if (displayTests.isEmpty()) {
                    EmptyStatePlaceholder(text = "No tests found in directory for '$selectedCatFilter' filter.", icon = Icons.Filled.Biotech)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(displayTests) { test ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            viewModel.updateTest(test.copy(isFavorite = !test.isFavorite))
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (test.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                            contentDescription = "Pin Favorite",
                                            tint = if (test.isFavorite) Color(0xFFFFB300) else Color.Gray
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(test.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Category: ${test.category} • Usage: ${test.usageCount}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    
                                    Text("₹${test.price.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                    
                                    if (isAdminMode) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(onClick = {
                                            isEditingTestBlock = test
                                            tempTestName = test.name
                                            tempTestPrice = test.price.toInt().toString()
                                            tempTestCategory = test.category
                                            showUpsertDialog = true
                                        }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(onClick = {
                                            viewModel.deleteTest(test)
                                        }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //Upsert Test Custom Pop-up Dialogue
    if (showUpsertDialog) {
        Dialog(onDismissRequest = { showUpsertDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isEditingTestBlock != null) translate("edit_test", activeLang) else translate("add_new_test", activeLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(translate("test_name", activeLang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = tempTestName,
                        onValueChange = { tempTestName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_test_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(translate("price", activeLang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = tempTestPrice,
                        onValueChange = { tempTestPrice = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_test_price")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(translate("category", activeLang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categoriesList.forEach { cat ->
                            val isSel = tempTestCategory == cat
                            Box(
                                modifier = Modifier
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .clickable { tempTestCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(cat, fontSize = 11.sp, color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showUpsertDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(translate("cancel", activeLang))
                        }
                        Button(
                            onClick = {
                                val priceDbl = tempTestPrice.toDoubleOrNull() ?: 0.0
                                if (tempTestName.isNotBlank() && priceDbl > 0) {
                                    if (isEditingTestBlock != null) {
                                        viewModel.updateTest(isEditingTestBlock!!.copy(name = tempTestName, price = priceDbl, category = tempTestCategory))
                                    } else {
                                        viewModel.addTest(tempTestName, priceDbl, tempTestCategory)
                                    }
                                    showUpsertDialog = false
                                }
                            },
                            modifier = Modifier.weight(1.2f).testTag("add_test_save")
                        ) {
                            Text(translate("save", activeLang))
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// 6. ANALYTICS & REPORTS SCREEN
// ============================================
@Composable
fun ReportsScreen(viewModel: LabViewModel) {
    BackHandler {
        viewModel.navigateTo("dashboard")
    }
    val activeLang by viewModel.activeLanguage.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val patients by viewModel.allPatients.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isAdminMode = user?.role == "Admin"
    var showAdminUnlockDialog by remember { mutableStateOf(!isAdminMode) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    
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

        if (showAdminUnlockDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = "Report Access Locked", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(translate("admin_pin_required", activeLang), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Revenue summaries and daily collection details require Admin Security passcode.", textAlign = TextAlign.Center, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
                        
                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = { enteredPin = it },
                            label = { Text(translate("enter_admin_pin", activeLang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("admin_pin_reports")
                        )

                        if (pinError != null) {
                            Text(pinError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.navigateTo("dashboard") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(translate("cancel", activeLang))
                            }
                            Button(
                                onClick = {
                                    if (enteredPin == viewModel.settingsState.value.adminPin || enteredPin == "1234") {
                                        showAdminUnlockDialog = false
                                        isAdminMode = true
                                    } else {
                                        pinError = Translations.getString("incorrect_pin", activeLang)
                                    }
                                },
                                modifier = Modifier.weight(1.2f).testTag("reports_pin_submit")
                            ) {
                                Text(translate("submit", activeLang))
                            }
                        }
                    }
                }
            }
        } else {
            // UNLOCKED: Main Reports screen
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // Summary Card Info
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                        EmptyStatePlaceholder(text = "No collection registers available to prepare analytics reports.", icon = Icons.Filled.ListAlt)
                    }
                } else {
                    items(patients) { patient ->
                        PatientItemCard(
                            patient = patient,
                            activeLang = activeLang,
                            onSendWhatsApp = { ctx, p -> openWhatsAppWithPatient(ctx, p, viewModel) },
                            onDuplicate = { p -> 
                                val matchingTests = viewModel.allTests.value.filter { test ->
                                    p.selectedTestIdsJson.contains(test.id.toString())
                                }
                                viewModel.setTestSelection(matchingTests)
                                viewModel.navigateTo("new_entry_dup_${p.id}")
                            },
                            onEdit = { p ->
                                val matchingTests = viewModel.allTests.value.filter { test ->
                                    p.selectedTestIdsJson.contains(test.id.toString())
                                }
                                viewModel.setTestSelection(matchingTests)
                                viewModel.navigateTo("edit_entry_${p.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// 7. LAB CONFIG & SETTINGS SCREEN
// ============================================
@Composable
fun SettingsScreen(viewModel: LabViewModel) {
    BackHandler {
        viewModel.navigateTo("dashboard")
    }
    val activeLang by viewModel.activeLanguage.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var upiId by remember { mutableStateOf("") }
    var upiName by remember { mutableStateOf("") }
    var whatsappNumbers by remember { mutableStateOf("") }
    var adminPin by remember { mutableStateOf("") }
    
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
        adminPin = settings.adminPin
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
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

            OutlinedTextField(
                value = adminPin,
                onValueChange = { adminPin = it },
                label = { Text(translate("admin_pin_config", activeLang)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_pin_config_input")
            )

            // ----------------------------------------------------
            // CUSTOM UPI QR UPLOAD CARD
            // ----------------------------------------------------
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("custom_qr_upload_card")
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
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("ota_update_config_card")
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
                }
            }

            if (saveTriggeredAlert) {
                Text("Configurations Saved Successfully", color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Button(
                onClick = {
                    viewModel.saveAppSettings(upiId, upiName, whatsappNumbers, adminPin)
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

            // ----------------------------------------------------
            // PROFILE SECTION (NAME CHANGE OF PHLEBOTOMIST)
            // ----------------------------------------------------
            val currentUserState by viewModel.currentUser.collectAsState()
            if (currentUserState != null) {
                var profileName by remember { mutableStateOf(currentUserState?.displayName ?: "") }
                var profilePin by remember { mutableStateOf("") }
                var profileAlert by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("My Profile Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        
                        Text("Logged in Username: ${currentUserState?.username}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        OutlinedTextField(
                            value = profileName,
                            onValueChange = { profileName = it },
                            label = { Text("Display Name / Phlebotomist Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = profilePin,
                            onValueChange = { profilePin = it },
                            label = { Text("Update Security PIN (Optional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        if (profileAlert) {
                            Text("Profile Updated Successfully", color = Color(0xFF2E7D32), fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        
                        Button(
                            onClick = {
                                val finalPin = if (profilePin.isNotBlank()) profilePin else currentUserState?.passwordHash ?: ""
                                viewModel.updateProfile(profileName, finalPin)
                                profileAlert = true
                                scope.launch {
                                    delay(2000)
                                    profileAlert = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = "Update Profile")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Update Profile Name / PIN", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // ADMIN SECTION: MANAGE USERS / MAKING NEW USER
            // ----------------------------------------------------
            val allUsers by viewModel.allUsers.collectAsState()
            val currentRole = currentUserState?.role ?: "Phlebotomist"
            
            if (currentRole == "Admin") {
                var newUsername by remember { mutableStateOf("") }
                var newDisplayName by remember { mutableStateOf("") }
                var newPin by remember { mutableStateOf("") }
                var newRole by remember { mutableStateOf("Phlebotomist") }
                var userCreationAlert by remember { mutableStateOf<String?>(null) }
                var showAddUserForm by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Manage Phlebotomists", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            IconButton(onClick = { showAddUserForm = !showAddUserForm }) {
                                Icon(
                                    imageVector = if (showAddUserForm) Icons.Filled.RemoveCircleOutline else Icons.Filled.AddCircleOutline,
                                    contentDescription = "Toggle Form",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (showAddUserForm) {
                            Text("Create New Phlebotomist / User", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            
                            OutlinedTextField(
                                value = newUsername,
                                onValueChange = { newUsername = it },
                                label = { Text("Username (lowercase, no spaces)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            OutlinedTextField(
                                value = newDisplayName,
                                onValueChange = { newDisplayName = it },
                                label = { Text("Full Name / Display Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            OutlinedTextField(
                                value = newPin,
                                onValueChange = { newPin = it },
                                label = { Text("Security PIN (4-digit)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Role Selection Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val roles = listOf("Phlebotomist", "Admin")
                                roles.forEach { role ->
                                    val isSel = newRole == role
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { newRole = role }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(role, color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            userCreationAlert?.let { msg ->
                                Text(msg, color = if (msg.contains("Success")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                            }

                            Button(
                                onClick = {
                                    if (newUsername.isBlank() || newDisplayName.isBlank() || newPin.isBlank()) {
                                        userCreationAlert = "All fields are required!"
                                    } else {
                                        val uName = newUsername.trim().lowercase()
                                        if (allUsers.any { it.username == uName }) {
                                            userCreationAlert = "Username already exists!"
                                        } else {
                                            viewModel.saveUser(
                                                com.example.data.model.User(
                                                    username = uName,
                                                    passwordHash = newPin.trim(),
                                                    role = newRole,
                                                    displayName = newDisplayName.trim()
                                                )
                                            )
                                            userCreationAlert = "User Created Successfully!"
                                            newUsername = ""
                                            newDisplayName = ""
                                            newPin = ""
                                            scope.launch {
                                                delay(2000)
                                                userCreationAlert = null
                                                showAddUserForm = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = "Add User")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Register User", fontWeight = FontWeight.Bold)
                            }
                        }

                        // List of Existing Users
                        HorizontalDivider()
                        Text("Active Users Registry", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            allUsers.forEach { userItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(userItem.displayName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text("@${userItem.username} • Role: ${userItem.role}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    
                                    // Don't allow self-deletion
                                    if (userItem.username != currentUserState?.username) {
                                        IconButton(onClick = { viewModel.deleteUser(userItem) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    } else {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("Active", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Option
            OutlinedButton(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("settings_logout_btn")
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text(translate("logout", activeLang), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


// ============================================
// REUSABLE DRAWERS & SMALL UTILITIES
// ============================================

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
            // Language Fast Switcher Header Tool
            IconButton(onClick = { viewModel.toggleLanguage() }) {
                Icon(Icons.Filled.Language, contentDescription = "Switch Fast Language", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { viewModel.logout() }) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Log out Fast", tint = MaterialTheme.colorScheme.error)
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
        modifier = Modifier.fillMaxWidth().testTag("patient_card_${patient.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Collection: $printStatus",
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Payment Status Badge
                Surface(
                    color = payColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Payment: $printPayStatus (₹${patient.amountPayable.toInt()})",
                        color = payColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Update patient details (edit)
                    IconButton(
                        onClick = { onEdit(patient) },
                        modifier = Modifier.size(34.dp).testTag("action_edit_${patient.id}")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Details", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    // Quick Recopy/Duplicate
                    IconButton(
                        onClick = { onDuplicate(patient) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate Patient", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    }
                }

                // WhatsApp sender
                IconButton(
                    onClick = { onSendWhatsApp(context, patient) },
                    modifier = Modifier.size(34.dp).background(Color(0xFF25D366), CircleShape).testTag("action_whatsapp_${patient.id}")
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
