package com.example.data.local

import androidx.room.*
import com.example.data.model.AppSettings
import com.example.data.model.PatientEntry
import com.example.data.model.TestItem
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface TestItemDao {
    @Query("SELECT * FROM test_items ORDER BY category ASC, name ASC")
    fun getAllTestsFlow(): Flow<List<TestItem>>

    @Query("SELECT * FROM test_items WHERE id = :id LIMIT 1")
    suspend fun getTestById(id: Int): TestItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestItem)

    @Update
    suspend fun updateTest(test: TestItem)

    @Delete
    suspend fun deleteTest(test: TestItem)

    @Query("UPDATE test_items SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: Int)

    @Query("DELETE FROM test_items")
    suspend fun clearAllTests()
}

@Dao
interface PatientEntryDao {
    @Query("SELECT * FROM patient_entries ORDER BY dateCreated DESC")
    fun getAllPatientsFlow(): Flow<List<PatientEntry>>

    @Query("SELECT * FROM patient_entries WHERE id = :id LIMIT 1")
    suspend fun getPatientById(id: String): PatientEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntry)

    @Update
    suspend fun updatePatient(patient: PatientEntry)

    @Delete
    suspend fun deletePatient(patient: PatientEntry)
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)
}
