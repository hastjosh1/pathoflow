package com.example.data.repository

import com.example.data.local.AppSettingsDao
import com.example.data.local.PatientEntryDao
import com.example.data.local.PriceListDao
import com.example.data.local.TestItemDao
import com.example.data.local.UserDao
import com.example.data.model.AppSettings
import com.example.data.model.PatientEntry
import com.example.data.model.PriceList
import com.example.data.model.PriceOverride
import com.example.data.model.TestItem
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

class LabRepository(
    private val userDao: UserDao,
    private val testItemDao: TestItemDao,
    private val patientEntryDao: PatientEntryDao,
    private val appSettingsDao: AppSettingsDao,
    private val priceListDao: PriceListDao
) {
    // Users
    val allUsers: Flow<List<User>> = userDao.getAllUsersFlow()
    
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }
    
    suspend fun saveUser(user: User) {
        userDao.insertUser(user)
    }
    
    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    // Tests
    val allTests: Flow<List<TestItem>> = testItemDao.getAllTestsFlow()
    
    suspend fun insertTest(testItem: TestItem): Long {
        return testItemDao.insertTest(testItem)
    }
    
    suspend fun updateTest(testItem: TestItem) {
        testItemDao.updateTest(testItem)
    }
    
    suspend fun deleteTest(testItem: TestItem) {
        testItemDao.deleteTest(testItem)
    }
    
    suspend fun incrementTestUsage(id: Int) {
        testItemDao.incrementUsageCount(id)
    }

    suspend fun deleteTestsByIds(ids: List<Int>) {
        testItemDao.deleteTestsByIds(ids)
        priceListDao.deleteOverridesForTests(ids)
    }

    suspend fun clearAllTests() {
        testItemDao.clearAllTests()
    }

    // Price lists
    val allPriceLists: Flow<List<PriceList>> = priceListDao.getAllPriceListsFlow()

    fun overridesForList(priceListId: Int): Flow<List<PriceOverride>> {
        return priceListDao.getOverridesForListFlow(priceListId)
    }

    suspend fun insertPriceList(priceList: PriceList): Long {
        return priceListDao.insertPriceList(priceList)
    }

    suspend fun deletePriceList(priceList: PriceList) {
        priceListDao.clearOverridesForList(priceList.id)
        priceListDao.deletePriceList(priceList)
    }

    suspend fun setPriceOverride(priceListId: Int, testId: Int, price: Double) {
        priceListDao.upsertOverride(PriceOverride(priceListId, testId, price))
    }

    suspend fun setPriceOverrides(overrides: List<PriceOverride>) {
        priceListDao.upsertOverrides(overrides)
    }

    suspend fun removePriceOverride(priceListId: Int, testId: Int) {
        priceListDao.deleteOverride(priceListId, testId)
    }

    suspend fun getTestById(id: Int): TestItem? {
        return testItemDao.getTestById(id)
    }

    // Patients
    val allPatients: Flow<List<PatientEntry>> = patientEntryDao.getAllPatientsFlow()
    
    suspend fun getPatientById(id: String): PatientEntry? {
        return patientEntryDao.getPatientById(id)
    }
    
    suspend fun insertPatient(patient: PatientEntry) {
        patientEntryDao.insertPatient(patient)
    }
    
    suspend fun updatePatient(patient: PatientEntry) {
        patientEntryDao.updatePatient(patient)
    }
    
    suspend fun deletePatient(patient: PatientEntry) {
        patientEntryDao.deletePatient(patient)
    }

    // Settings
    val settings: Flow<AppSettings?> = appSettingsDao.getSettingsFlow()
    
    suspend fun getSettingsDirect(): AppSettings? {
        return appSettingsDao.getSettingsDirect()
    }
    
    suspend fun saveSettings(settings: AppSettings) {
        appSettingsDao.insertSettings(settings)
    }
}
