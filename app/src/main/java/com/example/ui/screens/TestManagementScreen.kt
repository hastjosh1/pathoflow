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
import com.example.data.model.PriceList
import com.example.data.model.TestItem
import com.example.ui.components.OfflineUpiQrCode
import com.example.ui.translation.*
import com.example.ui.viewmodel.LabViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TestManagementScreen(viewModel: LabViewModel) {
    val activeLang by viewModel.activeLanguage.collectAsState()
    val allDbTests by viewModel.allTests.collectAsState()
    val standardTests by viewModel.standardTests.collectAsState()
    val allPriceLists by viewModel.allPriceLists.collectAsState()
    val activePriceListId by viewModel.activePriceListId.collectAsState()
    val activeOverrides by viewModel.activePriceOverrides.collectAsState()

    val standardPriceById = remember(standardTests) { standardTests.associate { it.id to it.price } }
    val activeListName = allPriceLists.firstOrNull { it.id == activePriceListId }?.name

    // Single-user app: full management access, no admin lock.
    val isAdminMode = true

    // Form for Adding / Editing tests
    var showUpsertDialog by remember { mutableStateOf(false) }
    var isEditingTestBlock by remember { mutableStateOf<TestItem?>(null) }
    var tempTestName by remember { mutableStateOf("") }
    var tempTestPrice by remember { mutableStateOf("") }
    var tempTestCategory by remember { mutableStateOf("Hematology") }

    // Price list management
    var showNewListDialog by remember { mutableStateOf(false) }
    var listPendingDelete by remember { mutableStateOf<PriceList?>(null) }

    // Multi-select deletion
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }

    fun exitSelectionMode() {
        selectionMode = false
        selectedIds = emptySet()
    }

    BackHandler {
        if (selectionMode) exitSelectionMode() else viewModel.navigateTo("dashboard")
    }

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

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                
                var testSearchQuery by remember { mutableStateOf("") }

                // Price list selector: Standard + custom rate cards + New
                Text(
                    "PRICE LIST",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Built-in Standard list (base TestItem prices)
                    val stdSelected = activePriceListId == 0
                    Box(
                        modifier = Modifier
                            .background(if (stdSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .clickable { viewModel.setActivePriceList(0) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Standard", color = if (stdSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    allPriceLists.forEach { priceList ->
                        val isSelected = activePriceListId == priceList.id
                        Row(
                            modifier = Modifier
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setActivePriceList(priceList.id) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(priceList.name, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Delete price list",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { listPendingDelete = priceList }
                                )
                            }
                        }
                    }

                    // Create new price list
                    Row(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { showNewListDialog = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "New price list", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New List", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

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
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
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
                                                putExtra(Intent.EXTRA_SUBJECT, "Accurate_Lab_Test_Directory")
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

                // Selection toolbar / directory header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectionMode) {
                        Text(
                            "${selectedIds.size} selected",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val allVisibleSelected = displayTests.isNotEmpty() && displayTests.all { it.id in selectedIds }
                            TextButton(onClick = {
                                selectedIds = if (allVisibleSelected) emptySet() else displayTests.map { it.id }.toSet()
                            }) {
                                Text(if (allVisibleSelected) "Clear All" else "Select All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { showBulkDeleteConfirm = true },
                                enabled = selectedIds.isNotEmpty()
                            ) {
                                Text("Delete (${selectedIds.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selectedIds.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
                            }
                            IconButton(onClick = { exitSelectionMode() }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Exit selection", modifier = Modifier.size(18.dp))
                            }
                        }
                    } else {
                        Text(
                            "Directory: ${displayTests.size} tests" + (activeListName?.let { " • $it prices" } ?: ""),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isAdminMode && displayTests.isNotEmpty()) {
                            TextButton(onClick = { selectionMode = true }) {
                                Icon(Icons.Filled.Checklist, contentDescription = "Select tests", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Select", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (displayTests.isEmpty()) {
                    EmptyStatePlaceholder(text = "No tests found in directory for '$selectedCatFilter' filter.", icon = Icons.Filled.Biotech)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(displayTests, key = { it.id }) { test ->
                            val isSelected = test.id in selectedIds
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .combinedClickable(
                                        onClick = {
                                            if (selectionMode) {
                                                selectedIds = if (isSelected) selectedIds - test.id else selectedIds + test.id
                                            }
                                        },
                                        onLongClick = {
                                            if (!selectionMode) {
                                                selectionMode = true
                                                selectedIds = setOf(test.id)
                                            }
                                        }
                                    ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (selectionMode) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                selectedIds = if (checked) selectedIds + test.id else selectedIds - test.id
                                            },
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(test.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Category: ${test.category} • Usage: ${test.usageCount}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₹${test.price.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                        if (activeOverrides.containsKey(test.id)) {
                                            Text(
                                                "Std ₹${(standardPriceById[test.id] ?: 0.0).toInt()}",
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (isAdminMode && !selectionMode) {
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

                    Text(
                        if (activeListName != null) "${translate("price", activeLang)} — $activeListName" else translate("price", activeLang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = tempTestPrice,
                        onValueChange = { tempTestPrice = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_test_price")
                    )
                    if (activeListName != null) {
                        Text(
                            "This price applies only to the \"$activeListName\" list. Name & category changes apply everywhere.",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        val editingTest = isEditingTestBlock
                        if (editingTest != null && activeOverrides.containsKey(editingTest.id)) {
                            TextButton(
                                onClick = {
                                    viewModel.resetPriceToStandard(editingTest.id)
                                    showUpsertDialog = false
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "Reset to Standard price (₹${(standardPriceById[editingTest.id] ?: 0.0).toInt()})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

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
                                    viewModel.upsertTestForActiveList(isEditingTestBlock, tempTestName, priceDbl, tempTestCategory)
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

    // New Price List dialog
    if (showNewListDialog) {
        var newListName by remember { mutableStateOf("") }
        var newListPercent by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showNewListDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("New Price List", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Create a separate rate card (e.g. B2B, Hospital, Camp Rates). It starts with Standard prices — edit any test to set its price in this list.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text("List Name", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        placeholder = { Text("e.g. B2B Rates") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Adjust All Prices By % (optional)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newListPercent,
                        onValueChange = { newListPercent = it },
                        placeholder = { Text("e.g. -20 for 20% discount, 10 for 10% markup") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showNewListDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(translate("cancel", activeLang))
                        }
                        Button(
                            onClick = {
                                if (newListName.isNotBlank()) {
                                    viewModel.addPriceList(newListName, newListPercent.toDoubleOrNull())
                                    showNewListDialog = false
                                }
                            },
                            enabled = newListName.isNotBlank(),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }

    // Delete Price List confirmation
    listPendingDelete?.let { pendingList ->
        AlertDialog(
            onDismissRequest = { listPendingDelete = null },
            title = { Text("Delete \"${pendingList.name}\"?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This removes the price list and its custom prices. Tests themselves are NOT deleted, and past patient entries keep their billed amounts.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePriceList(pendingList)
                        listPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete List")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { listPendingDelete = null }) {
                    Text(translate("cancel", activeLang))
                }
            }
        )
    }

    // Bulk delete confirmation
    if (showBulkDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            title = { Text("Delete ${selectedIds.size} tests?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Selected tests will be removed from the directory and every price list. This cannot be undone. Past patient entries keep their billed snapshots.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTests(selectedIds)
                        showBulkDeleteConfirm = false
                        exitSelectionMode()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All Selected")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBulkDeleteConfirm = false }) {
                    Text(translate("cancel", activeLang))
                }
            }
        )
    }
}

// ============================================
// 6. ANALYTICS & REPORTS SCREEN
// ============================================
