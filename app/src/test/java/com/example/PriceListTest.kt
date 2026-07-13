package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.PriceList
import com.example.data.model.TestItem
import com.example.data.repository.LabRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Price-list and bulk-delete behavior at the repository level: overrides
 * apply per list, deleting a list clears its overrides, and bulk test
 * deletion removes the tests plus every override pointing at them.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PriceListTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: LabRepository

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LabRepository(
            db.userDao(), db.testItemDao(), db.patientEntryDao(), db.appSettingsDao(), db.priceListDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `override applies only to its own price list`() = runBlocking {
        val testId = repository.insertTest(TestItem(name = "CBC", price = 300.0, category = "Hematology")).toInt()
        val b2bId = repository.insertPriceList(PriceList(name = "B2B")).toInt()
        val campId = repository.insertPriceList(PriceList(name = "Camp")).toInt()

        repository.setPriceOverride(b2bId, testId, 250.0)

        val b2bOverrides = repository.overridesForList(b2bId).first()
        val campOverrides = repository.overridesForList(campId).first()

        assertEquals(1, b2bOverrides.size)
        assertEquals(250.0, b2bOverrides.first().price, 0.001)
        assertTrue(campOverrides.isEmpty())

        // Standard price on the test itself is untouched.
        assertEquals(300.0, repository.getTestById(testId)!!.price, 0.001)
    }

    @Test
    fun `re-saving an override replaces the old price`() = runBlocking {
        val testId = repository.insertTest(TestItem(name = "LFT", price = 550.0, category = "Biochemistry")).toInt()
        val listId = repository.insertPriceList(PriceList(name = "Hospital")).toInt()

        repository.setPriceOverride(listId, testId, 500.0)
        repository.setPriceOverride(listId, testId, 450.0)

        val overrides = repository.overridesForList(listId).first()
        assertEquals(1, overrides.size)
        assertEquals(450.0, overrides.first().price, 0.001)
    }

    @Test
    fun `removing an override restores standard pricing fallback`() = runBlocking {
        val testId = repository.insertTest(TestItem(name = "TSH", price = 200.0, category = "Hormones")).toInt()
        val listId = repository.insertPriceList(PriceList(name = "B2B")).toInt()

        repository.setPriceOverride(listId, testId, 150.0)
        repository.removePriceOverride(listId, testId)

        assertTrue(repository.overridesForList(listId).first().isEmpty())
    }

    @Test
    fun `deleting a price list clears its overrides but keeps tests`() = runBlocking {
        val testId = repository.insertTest(TestItem(name = "HBA1C", price = 400.0, category = "Biochemistry")).toInt()
        val list = PriceList(id = repository.insertPriceList(PriceList(name = "Camp")).toInt(), name = "Camp")

        repository.setPriceOverride(list.id, testId, 350.0)
        repository.deletePriceList(list)

        assertTrue(repository.allPriceLists.first().isEmpty())
        assertTrue(repository.overridesForList(list.id).first().isEmpty())
        assertEquals("HBA1C", repository.getTestById(testId)!!.name)
    }

    @Test
    fun `bulk delete removes selected tests and their overrides`() = runBlocking {
        val id1 = repository.insertTest(TestItem(name = "CBC", price = 300.0, category = "Hematology")).toInt()
        val id2 = repository.insertTest(TestItem(name = "Lipid", price = 750.0, category = "Biochemistry")).toInt()
        val id3 = repository.insertTest(TestItem(name = "TSH", price = 200.0, category = "Hormones")).toInt()
        val listId = repository.insertPriceList(PriceList(name = "B2B")).toInt()
        repository.setPriceOverride(listId, id1, 250.0)
        repository.setPriceOverride(listId, id3, 180.0)

        repository.deleteTestsByIds(listOf(id1, id3))

        val remaining = repository.allTests.first()
        assertEquals(listOf(id2), remaining.map { it.id })
        assertNull(repository.getTestById(id1))

        // Overrides for deleted tests are gone too.
        assertTrue(repository.overridesForList(listId).first().isEmpty())
    }
}
