package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.TestItem
import com.example.data.repository.LabRepository
import com.example.ui.viewmodel.LabViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Core logic tests for [LabViewModel]: patient-ID format and test-selection
 * (which drives the running total / amount payable on the entry screen).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LabViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var viewModel: LabViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = LabRepository(
            db.userDao(), db.testItemDao(), db.patientEntryDao(), db.appSettingsDao(), db.priceListDao()
        )
        viewModel = LabViewModel(app, repository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `generated patient id has expected format`() {
        val id = viewModel.generateNextPatientId()
        // e.g. ALC-20260601-abcd
        assertTrue("id was '$id'", Regex("^ALC-\\d{8}-[a-z]{4}$").matches(id))
    }

    @Test
    fun `generated patient ids are unique across calls`() {
        val ids = (1..50).map { viewModel.generateNextPatientId() }.toSet()
        // Random 4-letter suffix should make collisions extremely unlikely.
        assertTrue("expected mostly-unique ids, got ${ids.size}", ids.size >= 48)
    }

    @Test
    fun `toggling a test adds then removes it from the selection`() {
        val test = TestItem(id = 1, name = "CBC", price = 300.0, category = "Hematology")

        viewModel.toggleTestSelection(test)
        assertEquals(listOf(test), viewModel.selectedTests.value)

        viewModel.toggleTestSelection(test)
        assertTrue(viewModel.selectedTests.value.isEmpty())
    }

    @Test
    fun `selection total equals sum of selected test prices`() {
        val cbc = TestItem(id = 1, name = "CBC", price = 300.0, category = "Hematology")
        val lipid = TestItem(id = 2, name = "Lipid", price = 750.0, category = "Biochemistry")

        viewModel.setTestSelection(listOf(cbc, lipid))

        val total = viewModel.selectedTests.value.sumOf { it.price }
        assertEquals(1050.0, total, 0.001)
    }

    @Test
    fun `clearing the selection empties it`() {
        viewModel.setTestSelection(
            listOf(TestItem(id = 1, name = "CBC", price = 300.0, category = "Hematology"))
        )
        assertFalse(viewModel.selectedTests.value.isEmpty())

        viewModel.clearTestSelection()
        assertTrue(viewModel.selectedTests.value.isEmpty())
    }
}
