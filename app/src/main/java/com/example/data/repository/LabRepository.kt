package com.example.data.repository

import com.example.data.local.AppSettingsDao
import com.example.data.local.PatientEntryDao
import com.example.data.local.TestItemDao
import com.example.data.local.UserDao
import com.example.data.model.AppSettings
import com.example.data.model.PatientEntry
import com.example.data.model.TestItem
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

class LabRepository(
    private val userDao: UserDao,
    private val testItemDao: TestItemDao,
    private val patientEntryDao: PatientEntryDao,
    private val appSettingsDao: AppSettingsDao
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
    
    suspend fun insertTest(testItem: TestItem) {
        testItemDao.insertTest(testItem)
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

    suspend fun clearAllTests() {
        testItemDao.clearAllTests()
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
