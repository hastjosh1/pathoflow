package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppSettings
import com.example.data.model.PatientEntry
import com.example.data.model.PriceList
import com.example.data.model.PriceOverride
import com.example.data.model.TestItem
import com.example.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, TestItem::class, PatientEntry::class, AppSettings::class, PriceList::class, PriceOverride::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun testItemDao(): TestItemDao
    abstract fun patientEntryDao(): PatientEntryDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun priceListDao(): PriceListDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // v1 -> v2: custom price lists. Additive only — existing patient and
        // test data is untouched, so OTA updates keep field data intact.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `price_lists` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `price_overrides` (`priceListId` INTEGER NOT NULL, `testId` INTEGER NOT NULL, `price` REAL NOT NULL, PRIMARY KEY(`priceListId`, `testId`))"
                )
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "accurate_lab_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            // Seed Default Users
            val userDao = db.userDao()
            userDao.insertUser(User("admin", "1234", "Admin", "Admin (Lab)"))
            userDao.insertUser(User("amit", "0000", "Phlebotomist", "Amit Patel (Phlebotomist)"))

            // Seed Default Settings
            val settingsDao = db.appSettingsDao()
            settingsDao.insertSettings(AppSettings())

            // Seed Some Fast-Access Popular Tests
            val testDao = db.testItemDao()
            val presetTests = listOf(
                TestItem(name = "CBC (Complete Blood Count)", price = 300.0, category = "Hematology", isFavorite = true),
                TestItem(name = "HbA1c (Glycated Haemoglobin)", price = 400.0, category = "Biochemistry", isFavorite = true),
                TestItem(name = "Lipid Profile", price = 750.0, category = "Biochemistry", isFavorite = true),
                TestItem(name = "LFT (Liver Function Test)", price = 550.0, category = "Biochemistry"),
                TestItem(name = "KFT (Kidney Function Test)", price = 550.0, category = "Biochemistry"),
                TestItem(name = "Thyroid Profile (T3, T4, TSH)", price = 650.0, category = "Hormones", isFavorite = true),
                TestItem(name = "Widal Slide Test (Typhoid)", price = 300.0, category = "Serology"),
                TestItem(name = "Dengue NS1 Antigen (Rapid)", price = 750.0, category = "Serology"),
                TestItem(name = "Vitamin D3 (25-Hydroxy)", price = 950.0, category = "Hormones"),
                TestItem(name = "Vitamin B12", price = 850.0, category = "Hormones"),
                TestItem(name = "Diabetes Screening Package", price = 1100.0, category = "Packages", isFavorite = true),
                TestItem(name = "Fever Panel Mini", price = 1400.0, category = "Packages"),
                TestItem(name = "Essential Health Checkup", price = 2400.0, category = "Packages", isFavorite = true),
                TestItem(name = "Urine Routine & Microscopy", price = 200.0, category = "Others")
            )
            for (test in presetTests) {
                testDao.insertTest(test)
            }
        }
    }
}
