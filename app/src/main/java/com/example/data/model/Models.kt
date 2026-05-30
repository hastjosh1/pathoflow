package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val passwordHash: String, // PIN or password
    val role: String, // "Admin" or "Phlebotomist"
    val displayName: String,
    val lastLoginTime: Long = 0L
)

@Entity(tableName = "test_items")
data class TestItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val category: String, // "Hematology", "Biochemistry", "Serology", "Hormones", "Packages", "Others"
    val isFavorite: Boolean = false,
    val usageCount: Int = 0
)

@Entity(tableName = "patient_entries")
data class PatientEntry(
    @PrimaryKey val id: String, // Generates e.g., "ALC-20260526-0001" or unique time-based String
    val name: String,
    val age: Int,
    val sex: String, // "Male", "Female", "Other"
    val referredDoctor: String,
    val selectedTestIdsJson: String, // List of test IDs serialized as "[1,2,3]"
    val testsSnapshotJson: String, // JSON snapshot of list of tests at registration e.g. [{"name":"CBC","price":300}]
    val mobileNumber: String = "",
    val email: String = "",
    val address: String = "",
    val collectionType: String, // "Home Collection" or "Lab Visit"
    val collectionStatus: String = "Sample Collected", // "Sample Collected", "Sent to Lab", "Report Ready", "Delivered"
    val paymentStatus: String, // "Paid", "Partial", "Pending"
    val paymentMode: String, // "UPI", "Cash", "Card", "Credit"
    val amountPayable: Double,
    val amountPaid: Double,
    val balanceAmount: Double,
    val dateCreated: Long = System.currentTimeMillis(),
    val dateString: String, // Format: "dd-MM-yyyy hh:mm a"
    val collectedBy: String, // Username of the active phlebotomist
    val isSynced: Boolean = false,
    val isVoiceInputUsed: Boolean = false
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val labUpiId: String = "accuratelab@okhdfcbank",
    val labUpiName: String = "Accurate Laboratory",
    val labWhatsAppNumbersString: String = "9876543210,919876543210", // Primary & backup WhatsApp numbers
    val adminPin: String = "1234",
    val theme: String = "system", // "system", "light", "dark"
    val language: String = "en" // "en" for English, "gu" for Gujarati
)
