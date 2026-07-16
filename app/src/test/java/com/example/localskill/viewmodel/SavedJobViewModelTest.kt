package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.fakes.FakeSavedJobRepo
import com.example.localskill.model.JobModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedJobViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeSavedJobRepo: FakeSavedJobRepo
    private lateinit var viewModel: SavedJobViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeSavedJobRepo = FakeSavedJobRepo().apply {
            jobsById = mapOf("1" to JobModel(id = "1", title = "Android Developer"))
            savedIdsByUser["uid-123"] = mutableSetOf("1")
        }
        viewModel = SavedJobViewModel(fakeAuthRepo, fakeSavedJobRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadSavedJobs populates state from repo`() = runTest {
        viewModel.loadSavedJobs()
        assertEquals(listOf("1"), viewModel.uiState.value.savedJobs.map { it.id })
    }

    @Test
    fun `unsaveJob removes job from local state immediately`() = runTest {
        viewModel.loadSavedJobs()
        viewModel.unsaveJob("1")
        assertTrue(viewModel.uiState.value.savedJobs.isEmpty())
        assertTrue(fakeSavedJobRepo.savedIdsByUser["uid-123"]?.contains("1") != true)
    }
}
