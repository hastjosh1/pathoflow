package com.example.data.util

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Snapshot of a test as it existed at the moment of patient registration
 * (name + price), so historical entries keep their original pricing even if
 * the test catalog later changes.
 */
@JsonClass(generateAdapter = true)
data class TestSnapshot(
    val name: String,
    val price: Double
)

/**
 * Centralized, safe (de)serialization for the JSON columns on PatientEntry.
 *
 * Replaces hand-built JSON strings, which corrupted data whenever a test name
 * contained a quote, comma, or other special character. Reads also go through
 * here so membership checks are exact instead of fragile substring matches.
 */
object PatientJson {

    private val moshi: Moshi = Moshi.Builder().build()

    private val idListType = Types.newParameterizedType(List::class.java, Integer::class.java)
    private val idListAdapter = moshi.adapter<List<Int>>(idListType)

    private val snapshotListType =
        Types.newParameterizedType(List::class.java, TestSnapshot::class.java)
    private val snapshotListAdapter = moshi.adapter<List<TestSnapshot>>(snapshotListType)

    fun encodeIds(ids: List<Int>): String = idListAdapter.toJson(ids)

    fun decodeIds(json: String?): List<Int> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            idListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun encodeSnapshots(snapshots: List<TestSnapshot>): String =
        snapshotListAdapter.toJson(snapshots)

    fun decodeSnapshots(json: String?): List<TestSnapshot> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            snapshotListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
