package com.example.localskill.viewmodel

import com.example.localskill.model.JobCategoryModel
import com.example.localskill.repo.AdminRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AdminCategoryViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var adminRepo: AdminRepo
    private lateinit var viewModel: AdminCategoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("admin-1")
        adminRepo = mock()
        viewModel = AdminCategoryViewModel(authRepo, adminRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addCategory creates a new active category`() = runTest {
        val category = JobCategoryModel(id = "category-0", name = "Engineering", jobCount = 0, isActive = true)
        whenever(adminRepo.addCategory("Engineering")).thenReturn(ResultState.Success(Unit))
        whenever(adminRepo.getCategories(includeInactive = true)).thenReturn(ResultState.Success(listOf(category)))

        viewModel.addCategory("Engineering")

        assertEquals(1, viewModel.uiState.value.categories.size)
        assertEquals("Engineering", viewModel.uiState.value.categories.single().name)
    }

    @Test
    fun `addCategory surfaces a duplicate-name error from the repo without adding a category`() = runTest {
        whenever(adminRepo.addCategory("engineering"))
            .thenReturn(ResultState.Error("A category with this name already exists."))

        val events = mutableListOf<AdminCategoryEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.addCategory("engineering")
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.categories.size)
        assertEquals(
            listOf(AdminCategoryEvent.ShowMessage("A category with this name already exists.")),
            events
        )
        job.cancel()
    }

    @Test
    fun `setCategoryActive deactivates a category and reloads the list`() = runTest {
        val deactivated = JobCategoryModel(id = "category-0", name = "Engineering", jobCount = 0, isActive = false)
        whenever(adminRepo.setCategoryActive("category-0", false, "admin-1"))
            .thenReturn(ResultState.Success(Unit))
        whenever(adminRepo.getCategories(includeInactive = true)).thenReturn(ResultState.Success(listOf(deactivated)))

        viewModel.setCategoryActive("category-0", false)

        assertEquals(false, viewModel.uiState.value.categories.single().isActive)
    }
}
