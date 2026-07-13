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
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
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
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
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
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = translate("test_selection", activeLang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Billing follows the price list chosen in Manage Tests —
                    // make it visible here so custom rates are never a surprise.
                    val allPriceLists by viewModel.allPriceLists.collectAsState()
                    val activePriceListId by viewModel.activePriceListId.collectAsState()
                    val activePriceListName = allPriceLists.firstOrNull { it.id == activePriceListId }?.name
                    if (activePriceListName != null) {
                        Row(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Sell,
                                contentDescription = "Active price list",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Billing at \"$activePriceListName\" prices",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
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
                        val commonTestNames = listOf(
                            "HAEMOGRAM (CBC)", "HBA1C", "BLOOD GROUP", 
                            "FASTING SUGAR", "LIVER FUNCTION TESTS (LFT)", "URINE ANALYSIS"
                        )
                        val frequentTests = allDbTests.sortedWith(
                            compareByDescending<TestItem> { it.usageCount }
                                .thenByDescending { commonTestNames.contains(it.name) }
                        ).take(6)

                        Text("Frequently Used Shortcuts:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
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
