package com.example.localskill.viewmodel

import com.example.localskill.model.JobModel
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.SavedJobRepo
import com.example.localskill.utils.ResultState
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SavedJobViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var savedJobRepo: SavedJobRepo
    private lateinit var viewModel: SavedJobViewModel

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("uid-123")
        savedJobRepo = mock()
        whenever(savedJobRepo.getSavedJobs("uid-123")).thenReturn(
            ResultState.Success(listOf(JobModel(id = "1", title = "Android Developer")))
        )
        viewModel = SavedJobViewModel(authRepo, savedJobRepo)
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
        whenever(savedJobRepo.unsaveJob("uid-123", "1")).thenReturn(ResultState.Success(Unit))

        viewModel.loadSavedJobs()
        viewModel.unsaveJob("1")

        assertTrue(viewModel.uiState.value.savedJobs.isEmpty())
    }
}
