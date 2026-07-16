package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeAdminRepo
import com.example.localskill.fakes.FakeAuthRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminCategoryViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeAdminRepo: FakeAdminRepo
    private lateinit var viewModel: AdminCategoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeAdminRepo = FakeAdminRepo()
        viewModel = AdminCategoryViewModel(fakeAuthRepo, fakeAdminRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addCategory creates a new active category`() = runTest {
        viewModel.addCategory("Engineering")

        assertEquals(1, fakeAdminRepo.categories.size)
        assertEquals("Engineering", fakeAdminRepo.categories.values.single().name)
    }

    @Test
    fun `addCategory rejects a case-insensitive duplicate name`() = runTest {
        viewModel.addCategory("Engineering")
        viewModel.addCategory("engineering")

        assertEquals(1, fakeAdminRepo.categories.size)
    }

    @Test
    fun `setCategoryActive deactivates a category`() = runTest {
        viewModel.addCategory("Engineering")
        val categoryId = fakeAdminRepo.categories.values.single().id

        viewModel.setCategoryActive(categoryId, false)

        assertEquals(false, fakeAdminRepo.categories.getValue(categoryId).isActive)
    }
}
