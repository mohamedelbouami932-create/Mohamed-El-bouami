package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.CollectibleProgressEntity
import com.example.data.model.MissionProgressEntity
import com.example.data.model.TerritoryProgressEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("San Andreas Hub", appName)
    }

    @Test
    fun `territory progress dao persists and updates correctly`() = runBlocking {
        val dao = db.territoryDao()
        dao.setProgress(TerritoryProgressEntity(id = "terr_ganton", isControlled = true))
        dao.setProgress(TerritoryProgressEntity(id = "terr_idlewood", isControlled = false))

        val progress = dao.getAllProgress().first()
        assertEquals(2, progress.size)
        assertTrue(progress.first { it.id == "terr_ganton" }.isControlled)

        dao.updateStatus("terr_idlewood", true)
        val updated = dao.getAllProgress().first()
        assertTrue(updated.first { it.id == "terr_idlewood" }.isControlled)

        dao.resetAll()
        val empty = dao.getAllProgress().first()
        assertEquals(0, empty.size)
    }

    @Test
    fun `mission and collectible progress persist correctly in Room`() = runBlocking {
        val missionDao = db.missionProgressDao()
        val collectibleDao = db.collectibleProgressDao()

        missionDao.setProgress(MissionProgressEntity(id = "m_end_of_the_line", isCompleted = true))
        collectibleDao.setProgress(CollectibleProgressEntity(id = "tag_001", isCollected = true))

        val missions = missionDao.getAllProgress().first()
        val collectibles = collectibleDao.getAllProgress().first()

        assertEquals(1, missions.size)
        assertTrue(missions[0].isCompleted)

        assertEquals(1, collectibles.size)
        assertTrue(collectibles[0].isCollected)
    }
}
