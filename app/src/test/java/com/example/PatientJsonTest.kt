package com.example

import com.example.data.util.PatientJson
import com.example.data.util.TestSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [PatientJson] — the safe replacement for the old hand-built JSON
 * strings that corrupted data when a test name contained special characters.
 */
class PatientJsonTest {

    @Test
    fun `ids round-trip`() {
        val ids = listOf(1, 2, 30, 400)
        assertEquals(ids, PatientJson.decodeIds(PatientJson.encodeIds(ids)))
    }

    @Test
    fun `blank or empty id json decodes to empty list`() {
        assertTrue(PatientJson.decodeIds("").isEmpty())
        assertTrue(PatientJson.decodeIds(null).isEmpty())
        assertTrue(PatientJson.decodeIds("[]").isEmpty())
    }

    @Test
    fun `legacy id format still parses`() {
        assertEquals(listOf(1, 2, 3), PatientJson.decodeIds("[1,2,3]"))
    }

    @Test
    fun `snapshots round-trip`() {
        val snaps = listOf(TestSnapshot("CBC", 300.0), TestSnapshot("Lipid Profile", 750.0))
        assertEquals(snaps, PatientJson.decodeSnapshots(PatientJson.encodeSnapshots(snaps)))
    }

    @Test
    fun `test name with quotes and commas survives round-trip`() {
        // This is exactly the input that broke the old hand-built JSON.
        val nasty = listOf(
            TestSnapshot("Vitamin D (\"25-OH\"), serum", 950.0),
            TestSnapshot("T3, T4, TSH \"Profile\"", 650.0)
        )
        val decoded = PatientJson.decodeSnapshots(PatientJson.encodeSnapshots(nasty))
        assertEquals(nasty, decoded)
        assertEquals("Vitamin D (\"25-OH\"), serum", decoded[0].name)
    }

    @Test
    fun `legacy snapshot format still parses`() {
        val decoded = PatientJson.decodeSnapshots("[{\"name\":\"CBC\",\"price\":300.0}]")
        assertEquals(1, decoded.size)
        assertEquals("CBC", decoded[0].name)
        assertEquals(300.0, decoded[0].price, 0.001)
    }
}
