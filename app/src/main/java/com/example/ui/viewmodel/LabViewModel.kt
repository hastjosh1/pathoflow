package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.LabApplication
import com.example.data.model.AppSettings
import com.example.data.model.PatientEntry
import com.example.data.model.TestItem
import com.example.data.model.User
import com.example.data.repository.LabRepository
import com.example.ui.translation.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class LabViewModel(
    application: Application,
    private val repository: LabRepository
) : AndroidViewModel(application) {

    // Login/Session State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _customUpiQrPath = MutableStateFlow<String?>(null)
    val customUpiQrPath: StateFlow<String?> = _customUpiQrPath.asStateFlow()

    private val _updateServerUrl = MutableStateFlow("https://hastjoshi.github.io/pathoflow-updates/version.json")
    val updateServerUrl: StateFlow<String> = _updateServerUrl.asStateFlow()

    // Config & Preferences (Defaults fallback inside setting flow)
    val settingsState: StateFlow<AppSettings> = repository.settings
        .map { it ?: AppSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    // Current app-wide language State
    val activeLanguage: StateFlow<Language> = settingsState
        .map { if (it.language == "gu") Language.GUJARATI else Language.ENGLISH }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Language.ENGLISH
        )

    // Database Flows
    val allPatients: StateFlow<List<PatientEntry>> = repository.allPatients
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTests: StateFlow<List<TestItem>> = repository.allTests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active screen navigation state (simple light navigation)
    private val _currentNavDestination = MutableStateFlow("splash")
    val currentNavDestination: StateFlow<String> = _currentNavDestination.asStateFlow()

    // Transient UI selection states for New Entry Screen
    private val _selectedTests = MutableStateFlow<List<TestItem>>(emptyList())
    val selectedTests: StateFlow<List<TestItem>> = _selectedTests.asStateFlow()

    val runningTotal: StateFlow<Double> = _selectedTests
        .map { tests -> tests.sumOf { it.price } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // Remembered login from SharedPreferences
    init {
        val sp = application.getSharedPreferences("accurate_lab_prefs", Context.MODE_PRIVATE)
        _customUpiQrPath.value = sp.getString("custom_upi_qr_path", null)
        _updateServerUrl.value = sp.getString("update_server_url", "https://hastjoshi.github.io/pathoflow-updates/version.json") ?: "https://hastjoshi.github.io/pathoflow-updates/version.json"
        val savedUsername = sp.getString("remembered_username", null)
        val savedRole = sp.getString("remembered_role", null)
        val savedDisplayName = sp.getString("remembered_display_name", null)
        if (savedUsername != null && savedRole != null && savedDisplayName != null) {
            // Auto-login on startup
            _currentUser.value = User(savedUsername, "", savedRole, savedDisplayName)
            _isLoggedIn.value = true
            _currentNavDestination.value = "dashboard"
        }

        val customTestsSeeded = sp.getBoolean("custom_tests_seeded_v3", false)
        if (!customTestsSeeded) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    repository.clearAllTests()
                    val lines = DEFAULT_CSV_TESTS.lines()
                    for (line in lines) {
                        if (line.isBlank()) continue
                        val parts = mutableListOf<String>()
                        var inQuotes = false
                        val current = java.lang.StringBuilder()
                        var i = 0
                        while (i < line.length) {
                            val c = line[i]
                            if (c == '\"') {
                                inQuotes = !inQuotes
                            } else if (c == ',' && !inQuotes) {
                                parts.add(current.toString().trim())
                                current.setLength(0)
                            } else {
                                current.append(c)
                            }
                            i++
                        }
                        parts.add(current.toString().trim())
                        val cleanParts = parts.map { it.removeSurrounding("\"").trim() }
                        
                        if (cleanParts.size >= 2) {
                            val name = cleanParts[0]
                            val priceStr = cleanParts[1]
                            val price = priceStr.toDoubleOrNull()
                            if (name.isNotEmpty() && name.lowercase() != "name" && name.lowercase() != "test name" && price != null) {
                                // Automatically categorize based on test name keywords
                                val category = when {
                                    name.contains("URINE", ignoreCase = true) || name.contains("STOOL", ignoreCase = true) -> "Others"
                                    name.contains("HEMATOLOGY", ignoreCase = true) || name.contains("HAEMOGRAM", ignoreCase = true) || name.contains("BLOOD", ignoreCase = true) || name.contains("CBC", ignoreCase = true) -> "Hematology"
                                    name.contains("CULTURE", ignoreCase = true) || name.contains("C&S", ignoreCase = true) || name.contains("PUS", ignoreCase = true) || name.contains("AFB", ignoreCase = true) -> "Serology"
                                    name.contains("THYROID", ignoreCase = true) || name.contains("TFT", ignoreCase = true) || name.contains("TSH", ignoreCase = true) || name.contains("AMH", ignoreCase = true) || name.contains("FSH", ignoreCase = true) || name.contains("LH", ignoreCase = true) || name.contains("PROLACTIN", ignoreCase = true) -> "Hormones"
                                    name.contains("PROFILE", ignoreCase = true) || name.contains("PANEL", ignoreCase = true) -> "Packages"
                                    else -> "Biochemistry"
                                }
                                // Seed first 10 or popular tests as favorites
                                val isFavorite = name.contains("CBC", ignoreCase = true) || name.contains("HBA1C", ignoreCase = true) || name.contains("LIPID", ignoreCase = true) || name.contains("THYROID", ignoreCase = true) || name.contains("LFT", ignoreCase = true) || name.contains("KFT", ignoreCase = true)
                                
                                repository.insertTest(TestItem(name = name, price = price, category = category, isFavorite = isFavorite))
                            }
                        }
                    }
                    sp.edit().putBoolean("custom_tests_seeded_v3", true).apply()
                } catch (e: Exception) {
                    android.util.Log.e("LabViewModel", "Failed to seed custom tests", e)
                }
            }
        }
    }

    fun navigateTo(destination: String) {
        _currentNavDestination.value = destination
    }

    fun saveCustomUpiQrPath(path: String?) {
        val sp = getApplication<Application>().getSharedPreferences("accurate_lab_prefs", Context.MODE_PRIVATE)
        sp.edit().putString("custom_upi_qr_path", path).apply()
        _customUpiQrPath.value = path
    }



    fun saveUpdateServerUrl(url: String) {
        val sp = getApplication<Application>().getSharedPreferences("accurate_lab_prefs", Context.MODE_PRIVATE)
        sp.edit().putString("update_server_url", url).apply()
        _updateServerUrl.value = url
    }

    fun copyImageToInternalStorage(context: Context, uri: android.net.Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val dir = context.filesDir
            val file = File(dir, "custom_upi_qr.png")
            val outputStream = java.io.FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("LabViewModel", "Failed to copy image to internal storage", e)
            null
        }
    }

    // Login workflow
    fun attemptLogin(username: String, pin: String, rememberMe: Boolean, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByUsername(username.trim().lowercase())
            if (user != null) {
                if (user.passwordHash == pin.trim()) {
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    
                    if (rememberMe) {
                        val sp = getApplication<Application>().getSharedPreferences("accurate_lab_prefs", Context.MODE_PRIVATE)
                        sp.edit()
                            .putString("remembered_username", user.username)
                            .putString("remembered_role", user.role)
                            .putString("remembered_display_name", user.displayName)
                            .apply()
                    }
                    
                    onResult(true, "Login Successful")
                    _currentNavDestination.value = "dashboard"
                } else {
                    onResult(false, "Incorrect security PIN!")
                }
            } else {
                onResult(false, "User not found!")
            }
        }
    }

    fun logout() {
        val sp = getApplication<Application>().getSharedPreferences("accurate_lab_prefs", Context.MODE_PRIVATE)
        sp.edit().clear().apply()
        _currentUser.value = null
        _isLoggedIn.value = false
        _currentNavDestination.value = "login"
    }

    // Toggle language (English <-> Gujarati)
    fun toggleLanguage() {
        viewModelScope.launch {
            val currentSettings = settingsState.value
            val nextLang = if (currentSettings.language == "en") "gu" else "en"
            repository.saveSettings(currentSettings.copy(language = nextLang))
        }
    }

    // Generate Dynamic Patient ID
    fun generateNextPatientId(): String {
        val datePattern = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val randomSuffix = (1..4).map { ('a'..'z').random() }.joinToString("")
        return "ALC-$datePattern-$randomSuffix"
    }

    // Register test selection
    fun toggleTestSelection(test: TestItem) {
        val currentList = _selectedTests.value.toMutableList()
        if (currentList.any { it.id == test.id }) {
            currentList.removeAll { it.id == test.id }
        } else {
            currentList.add(test)
        }
        _selectedTests.value = currentList
    }

    fun clearTestSelection() {
        _selectedTests.value = emptyList()
    }

    fun setTestSelection(tests: List<TestItem>) {
        _selectedTests.value = tests
    }

    // Save Patient Entry (Create or Update)
    fun savePatientEntry(
        id: String,
        name: String,
        age: Int,
        sex: String,
        referredDoctor: String,
        mobileNumber: String,
        email: String,
        address: String,
        collectionType: String,
        collectionStatus: String,
        paymentStatus: String,
        paymentMode: String,
        amountPayable: Double,
        amountPaid: Double,
        balanceAmount: Double,
        voiceInputUsed: Boolean,
        onComplete: (Boolean, PatientEntry?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Prepare simple JSON serialized test elements
                val testIds = _selectedTests.value.map { it.id }
                val testIdsJson = "[" + testIds.joinToString(",") + "]"
                
                // Snapshots elements
                val snapList = _selectedTests.value.map { "{\"name\":\"${it.name}\",\"price\":${it.price}}" }
                val testSnapshotJson = "[" + snapList.joinToString(",") + "]"
                
                val dateStr = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date())
                
                val entry = PatientEntry(
                    id = id,
                    name = name.trim(),
                    age = age,
                    sex = sex,
                    referredDoctor = referredDoctor.trim().ifEmpty { "Self" },
                    selectedTestIdsJson = testIdsJson,
                    testsSnapshotJson = testSnapshotJson,
                    mobileNumber = mobileNumber.trim(),
                    email = email.trim(),
                    address = address.trim(),
                    collectionType = collectionType,
                    collectionStatus = collectionStatus,
                    paymentStatus = paymentStatus,
                    paymentMode = paymentMode,
                    amountPayable = amountPayable,
                    amountPaid = amountPaid,
                    balanceAmount = balanceAmount,
                    dateString = dateStr,
                    collectedBy = _currentUser.value?.displayName ?: "Phlebotomist",
                    isVoiceInputUsed = voiceInputUsed
                )
                
                // Save
                repository.insertPatient(entry)
                
                // Update local usage database count for the tests to feed shortcut rankings
                for (test in _selectedTests.value) {
                    repository.incrementTestUsage(test.id)
                }
                
                clearTestSelection()
                onComplete(true, entry)
            } catch (e: Exception) {
                Log.e("LabViewModel", "Error saving patient entry", e)
                onComplete(false, null)
            }
        }
    }

    suspend fun getPatientById(id: String): PatientEntry? {
        return repository.getPatientById(id)
    }

    // Delete Patient Entry
    fun deletePatientEntry(entry: PatientEntry) {
        viewModelScope.launch {
            repository.deletePatient(entry)
        }
    }

    // Save App Settings
    fun saveAppSettings(
        upiId: String,
        upiName: String,
        whatsappNumbers: String,
        adminPin: String
    ) {
        viewModelScope.launch {
            val current = settingsState.value
            val updated = current.copy(
                labUpiId = upiId.trim(),
                labUpiName = upiName.trim(),
                labWhatsAppNumbersString = whatsappNumbers.trim(),
                adminPin = adminPin.trim()
            )
            repository.saveSettings(updated)
        }
    }

    // Tests Administration
    fun addTest(name: String, price: Double, category: String) {
        viewModelScope.launch {
            repository.insertTest(TestItem(name = name.trim(), price = price, category = category))
        }
    }

    fun updateTest(test: TestItem) {
        viewModelScope.launch {
            repository.updateTest(test)
        }
    }

    fun deleteTest(test: TestItem) {
        viewModelScope.launch {
            repository.deleteTest(test)
        }
    }

    // User / Phlebotomist Administration
    fun saveUser(user: User) {
        viewModelScope.launch {
            repository.saveUser(user)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }

    fun updateProfile(displayName: String, pin: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(displayName = displayName.trim(), passwordHash = pin.trim())
            repository.saveUser(updated)
            _currentUser.value = updated
            
            // Also update SharedPreferences if rememberMe was used
            val sp = getApplication<Application>().getSharedPreferences("accurate_lab_prefs", Context.MODE_PRIVATE)
            if (sp.contains("remembered_username")) {
                sp.edit()
                    .putString("remembered_display_name", updated.displayName)
                    .apply()
            }
        }
    }

    // generate dynamic UPI link
    fun generateUpiPayUri(amount: Double, patientId: String, patientName: String): String {
        val s = settingsState.value
        val encodedName = java.net.URLEncoder.encode(s.labUpiName, "UTF-8")
        val note = java.net.URLEncoder.encode("Lab:$patientId - $patientName", "UTF-8")
        return "upi://pay?pa=${s.labUpiId}&pn=$encodedName&am=$amount&tn=$note&cu=INR"
    }

    // Format WhatsApp message
    fun formatWhatsAppMessage(entry: PatientEntry, testNames: List<String>): String {
        val sb = StringBuilder()
        sb.append("🔬 *Accurate Lab Patient Entry*\n\n")
        sb.append("📋 *ID:* `${entry.id}`\n")
        sb.append("👤 *Name:* ${entry.name}\n")
        sb.append("🎂 *Age/Sex:* ${entry.age} Y / ${entry.sex.take(1)}\n")
        sb.append("👨‍⚕️ *Doctor:* ${entry.referredDoctor}\n\n")
        sb.append("🧪 *Tests Selected:*\n")
        testNames.forEach { test ->
            sb.append(" • _${test}_\n")
        }
        sb.append("\n")
        if (entry.mobileNumber.isNotEmpty()) sb.append("📱 *Mobile:* ${entry.mobileNumber}\n")
        sb.append("📍 *Collection:* ${entry.collectionType} (${entry.collectionStatus})\n\n")
        sb.append("💰 *Estimated Amount:* ₹${entry.amountPayable}\n")
        sb.append("💵 *Paid Amount:* ₹${entry.amountPaid}\n")
        sb.append("⏰ *Payment Status:* _${entry.paymentStatus}_ (${entry.paymentMode})\n\n")
        sb.append("👷 *Collected By:* ${entry.collectedBy}\n")
        sb.append("📅 *Date:* ${entry.dateString}\n")
        sb.append("-------------------------")
        return sb.toString()
    }

    // Export Reports to CSV local text copy
    suspend fun exportToCsvString(): String = withContext(Dispatchers.Default) {
        val csv = StringBuilder()
        csv.append("Patient ID,Patient Name,Age,Sex,Doctor,Collection Type,Status,Total Amount,Paid Amount,Payment Status,Payment Mode,Date\n")
        for (p in allPatients.value) {
            csv.append("\"${p.id}\",\"${p.name}\",${p.age},\"${p.sex}\",\"${p.referredDoctor}\",\"${p.collectionType}\",\"${p.collectionStatus}\",${p.amountPayable},${p.amountPaid},\"${p.paymentStatus}\",\"${p.paymentMode}\",\"${p.dateString}\"\n")
        }
        csv.toString()
    }

    // Import and Export Tests CSV helpers
    fun importTestsFromCsv(csvText: String): Int {
        var count = 0
        val lines = csvText.lines()
        viewModelScope.launch {
            for (line in lines) {
                if (line.isBlank()) continue
                val parts = mutableListOf<String>()
                var inQuotes = false
                val current = java.lang.StringBuilder()
                var i = 0
                while (i < line.length) {
                    val c = line[i]
                    if (c == '\"') {
                        inQuotes = !inQuotes
                    } else if (c == ',' && !inQuotes) {
                        parts.add(current.toString().trim())
                        current.setLength(0)
                    } else {
                        current.append(c)
                    }
                    i++
                }
                parts.add(current.toString().trim())
                val cleanParts = parts.map { it.removeSurrounding("\"").trim() }
                
                if (cleanParts.size >= 2) {
                    val name = cleanParts[0]
                    val priceStr = cleanParts[1]
                    val price = priceStr.toDoubleOrNull()
                    if (name.isNotEmpty() && name.lowercase() != "name" && name.lowercase() != "test name" && name.lowercase() != "test name,rate" && name.lowercase() != "test name,price" && price != null) {
                        val category = if (cleanParts.size >= 3 && cleanParts[2].isNotEmpty()) {
                            cleanParts[2]
                        } else {
                            // Automatically categorize based on test name keywords
                            when {
                                name.contains("URINE", ignoreCase = true) || name.contains("STOOL", ignoreCase = true) -> "Others"
                                name.contains("HEMATOLOGY", ignoreCase = true) || name.contains("HAEMOGRAM", ignoreCase = true) || name.contains("BLOOD", ignoreCase = true) || name.contains("CBC", ignoreCase = true) -> "Hematology"
                                name.contains("CULTURE", ignoreCase = true) || name.contains("C&S", ignoreCase = true) || name.contains("PUS", ignoreCase = true) || name.contains("AFB", ignoreCase = true) -> "Serology"
                                name.contains("THYROID", ignoreCase = true) || name.contains("TFT", ignoreCase = true) || name.contains("TSH", ignoreCase = true) || name.contains("AMH", ignoreCase = true) || name.contains("FSH", ignoreCase = true) || name.contains("LH", ignoreCase = true) || name.contains("PROLACTIN", ignoreCase = true) -> "Hormones"
                                name.contains("PROFILE", ignoreCase = true) || name.contains("PANEL", ignoreCase = true) -> "Packages"
                                else -> "Biochemistry"
                            }
                        }
                        val standardCategories = listOf("Hematology", "Biochemistry", "Serology", "Hormones", "Packages", "Others")
                        val matchedCategory = standardCategories.firstOrNull { it.lowercase() == category.lowercase() } ?: "Others"
                        
                        repository.insertTest(TestItem(name = name, price = price, category = matchedCategory))
                        count++
                    }
                }
            }
        }
        return count
    }

    suspend fun exportTestsToCsvString(): String = withContext(Dispatchers.Default) {
        val csv = StringBuilder()
        csv.append("Test Name,Price,Category\n")
        for (t in allTests.value) {
            csv.append("\"${t.name}\",${t.price},\"${t.category}\"\n")
        }
        csv.toString()
    }
}

class LabViewModelFactory(
    private val application: Application,
    private val repository: LabRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LabViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LabViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private val DEFAULT_CSV_TESTS = """
24 HRS. URINE MICROALBUMIN,0
A.F.B. STUDY,0
A.S.O. TITRE,0
ACLA IGG/IGM,800
ADENOSINE DEAMINASE STUDY,0
AEC,400
ALKALINE PHOSPHATE,150
ALLERGY PROFILE,3100
ALPHA FETO,450
AMH,1200
AMMONIA LEVEL,400
AMYLASE,0
ANA BY IF,500
ANA PROFILE,2500
ANAEMIA PROFILE,0
ANC,0
ANC Telang,0
ANTENATAL PROFILE,670
ANTI CCP,700
ANTI HCV,300
ANTI THYROID ANTIBODY,800
ANTI TPO,500
ANTIDENGUE ANTIBODY,500
ANTILUPUS ANTICOAGULANT,1300
APLA IGG/IGM,800
ASCITIC FLUID,0
ASCITIC FLUID STUDY,0
AUTOIMMUNE PROFILE,0
Acetyle choline receptor test,2900
Anti Mullerian Hormone (AMH),1200
B-HCG,350
BIO-CHEMISTRY REPORT (Full),0
BIOPSY,1000
BLOOD CULTURE & SENSITIVITY REPORT,800
BLOOD GROUP,50
BLOOD H. PYLORI,400
BLOOD SUGAR ANALYSIS,50
"BLOOD SUGAR ANALYSIS F,PP",100
BLOOD UREA,100
BONE MARROW EXAMINATION,0
BRUCELLA (igG/igM),500
BRUCELLOSIS SEROLGY,0
BSL R,50
BTCT,100
BUL,0
Beta-2-glyacoprotien-1 /IGg/IGm,900
Bilirubin Total,100
Bilitab,0
C.S.F. STUDY,0
C3 & C4 COMPLIMENT LEVEL,0
CA-19.9,700
CALCIUM ESTIMATION,0
CARDIAC PROFILE,0
CEA,500
CHIKANGUNIA RT PCR,1500
CK-MB,350
COAGULATION PROFILE,0
CORTICOL,450
CORTISOL,500
COVID-19,0
COVID-19Ag,240
CPK,350
CPK-MM,300
CREATININE,0
CREATININE KINASE,0
CRP,200
CSF (CEREBRO-SPINAL FLUID REPORT),0
CULTURE & SENSITIVITY NEG,0
CULTURE & SENSITIVITY REPORT,550
CULTURE & SENSITIVITY STUDY,600
CULTURE & SENSITIVITY STUDY-2,500
CULTURE & SENSITIVITY STUDY-3,0
CYTO BREAST MASTITIS,1000
CYTO COLD ABSCESS,0
Coagulation Profile,1500
Covid Mini,250
D-DIMER,700
D3,650
DENGUE IGG & IGM,500
DENGUE NS 1,300
DENGUE VIRUS,500
DHEA-S,500
DILANTIN,0
DIRECT COOMBS TEST,100
DOUBLE MARKER,1500
EAR SCAPPING STDUY,0
ELECTROLYTES,400
ELISA HBSAG,200
ELISA HCV,500
ENZYME ESTIMATIONS,0
ESR,100
ESTRADIOL (E2),400
Estrogen blood,0
FASTING SUGAR,50
FEMALE  FERTILITY PROFILE,0
FEMALE FERTILITY,0
FERRETIN,400
FLUID REPORT,0
FOLIC ACID,500
FPP,0
FREE T3,300
FREE T4,300
FREE TESTOSTERONE,700
FSH,250
FSH LH PROLACTIN,700
G6 PD ENZYME,0
G6PD ENZYME ESTIMATION,0
G6PD SCREENING TEST,0
GFR,350
GGT,300
GLUCOSE TOLERANCE TEST,0
GRAM/AFB STAIN,0
GTT75gms,0
H.S.CRP,450
HAEMOGRAM (CBC),200
HAV (Rapid),350
HBA1C,500
HBA1C (Full),400
HBA2,500
HBSAG,100
HBV DNA (QUANTITAVE),3000
HBVsingle,0
HEMOGLOBIN,100
HEPATITIS MARKERS,0
HIGH SENSITIV C.R.P.,400
HIV I-II,200
HIV WESTERN BLOT.,0
HLA-B27,2500
HOMOCYSTINE,1000
Hb Electrophoresis,700
Histopathological Examination (HPE),2500
Homocysteine test,850
IGG. IGM BY ELISA,0
IL-6 (Interleukin -6),0
IMMUNOGLOBULIN ESTIMATION,0
INDIRECT COOMBS TEST,300
INDIRECT COOMS TEST,300
IONIZED CALCIUM,400
IUI,0
IgG,500
K,350
KFTfull,1000
Karyotyping From Blood,2800
LDH,350
LE CELL,0
LH,250
LIPID PROFILE,300
LIPID PROFILE (EXTENDED),2400
LIPOPROTEIN (a),700
LITHIUM,0
LIVER FUNCTION TESTS (LFT),1000
MALARIA CARD TEST,0
MALE FERTILITY PROFILE,0
MANTOUX TEST,0
MP (Card),100
MYOSITIS PROFILE,7500
Na,350
OGTT,150
OSMOTIC FRAGILITY,0
OTprofile,0
PANCREASE FUNCTION TEST,0
PAP SMEAR FOR CYTOLOGY,400
PHOSPHOLIPID (A/G/M),1200
PHOSPHORUS,250
PLATELATE,100
PLEURAL FLUID,0
PRE-OPERATIVE PROFILE,0
PREGNANCY TEST,0
PROGESTERONE,450
PROLACTINE,300
PROSTATE PROFILE,0
PROSTATE Test,400
PROTEIN C,3000
PROTEIN S,3000
PROTHROMBIN TIME,250
PROTHROMBIN TIME [PT],250
PS (PERIPHERAL SMEAR STUDY),200
PSA test,500
PTH(PARATHYROID HORMONE),550
PTTK / (aPTT),300
PUS CS,600
R.A.TEST,0
RA q,300
RA-ASO-CRP,350
RETIC.COUNT,250
RFT,700
S. Creatinine,100
S. LACTAC,500
S.ALBUMIN,150
S.Alkaline phosphatase,150
S.Calcium,200
S.Cholesterol,100
S.G.O.T.,0
S.IRON LEVEL,500
S.IRON PROFILE,1200
S.KETON,200
S.LIPASE LEVEL,0
S.PHOSPHORUS,500
S.Sodium/Potassium/Chloride,350
SCRUB TYPHUS (Anti Body),0
SCRUB TYPHUS AB(igG/igM),1100
SEMEN,200
SEMINAL FLUID REPORT [AFTER WASH ],0
SERUM AMYLASE,0
SERUM ELECTROLYTES,500
SERUM INSULIN LEVEL,400
SERUM IgE LEVEL,500
SERUM LIPASE,400
SERUM MAGNESIUM,350
SERUM TESTOSTERONE,400
SGOT,100
SGPT,100
SICKLING TEST,0
SODIUM,0
SPECIAL INVESTIGATION,0
SPECIAL INVESTIGATION,0
SPUTUM AFB,250
SPUTUM CBNAAT,2300
SPUTUM CULTURE EXAMINATION,400
SPUTUM EXAMINATION,100
SPUTUM RM,400
STOOL CULTURE,500
STOOL EXAMINATION,300
SYNOVIAL FLUID,500
Serology,400
Serum Creatinine,100
TB IGM  & TB IGG - ELISA,0
TC DC MP,270
TEST FOR CHIKUNGUNYA,400
TESTOSTERONE,500
TFT,300
THROAT SWAB STUDY,0
THYROID FUNCTION TESTS,300
TIBC,650
TORCH - 10,1500
TRANSFERRINE SATURATION%,0
TROPONIN-I,600
TSH,200
TUBERCULIN TEST,0
TUMOR MARKARS,0
TYPHI DOT (Rapid),150
TYPHIDOT IGG IGM,200
Total Protien,150
TuberTT,0
UPT,100
URINARY MICROALBUMIN,0
URINE ALBUMIN AND SUGAR,50
URINE ANALYSIS,50
URINE ANALYSIS (New),50
URINE CULTURE,500
URINE FOR B.J.PROTEINS,100
URINE MICRO ALBUMIN,400
URINE PORPHOBILINOGEN,0
URINE PREGNANCY TEST,100
URINE SODIUM,300
Uric Acid,200
VDRL,200
VDRL TITRE,350
VISIT CHARGES,30
VITAMINS,0
VM PROFILE,0
Vitamin B12,550
Vitamin D 3,650
WIDAL,100
WIDAL REACTION,300
WIDAL(Table),0
Y-Chromosomal Microdeletion by PCR,4500
ca-125,550
po4 blood test,0
s.Acetone,300
s.protein,150
""".trimIndent()

