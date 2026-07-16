package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeApplicationRepo
import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.fakes.FakeFileRepo
import com.example.localskill.fakes.FakeJobSeekerProfileRepo
import com.example.localskill.fakes.FakeSavedJobRepo
import com.example.localskill.fakes.FakeUserRepo
import com.example.localskill.model.EducationModel
import com.example.localskill.model.SkillModel
import com.example.localskill.model.UserModel
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

@OptIn(ExperimentalCoroutinesApi::class)
class JobSeekerProfileViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeUserRepo: FakeUserRepo
    private lateinit var fakeProfileRepo: FakeJobSeekerProfileRepo
    private lateinit var viewModel: JobSeekerProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeUserRepo = FakeUserRepo().apply {
            userResult = ResultState.Success(UserModel(id = "uid-123", fullName = "Jane Doe", email = "jane@example.com"))
        }
        fakeProfileRepo = FakeJobSeekerProfileRepo()
        viewModel = JobSeekerProfileViewModel(
            fakeAuthRepo,
            fakeUserRepo,
            fakeProfileRepo,
            FakeApplicationRepo(),
            FakeSavedJobRepo(),
            FakeFileRepo()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile exposes account and profile data together`() = runTest {
        viewModel.loadProfile()
        val state = viewModel.uiState.value
        assertEquals("Jane Doe", state.fullName)
        assertEquals("jane@example.com", state.email)
    }

    @Test
    fun `updatePersonalInfo saves and reloads`() = runTest {
        viewModel.loadProfile()
        viewModel.updatePersonalInfo("Android Developer", "Bio text", "Kathmandu", "Kathmandu")

        assertEquals("Android Developer", viewModel.uiState.value.profile.headline)
        assertEquals("Kathmandu", viewModel.uiState.value.profile.city)
    }

    @Test
    fun `addEducation appends a new entry with a stable id`() = runTest {
        viewModel.loadProfile()
        viewModel.addEducation(EducationModel(institution = "Tribhuvan University", qualification = "BSc"))

        val education = viewModel.uiState.value.profile.education
        assertEquals(1, education.size)
        assertTrue(education.first().id.isNotBlank())
    }

    @Test
    fun `removeEducation removes only the targeted entry`() = runTest {
        viewModel.loadProfile()
        viewModel.addEducation(EducationModel(id = "keep", institution = "A"))
        viewModel.addEducation(EducationModel(id = "drop", institution = "B"))

        viewModel.removeEducation("drop")

        assertEquals(listOf("keep"), viewModel.uiState.value.profile.education.map { it.id })
    }

    @Test
    fun `addSkill rejects a case-insensitive duplicate`() = runTest {
        viewModel.loadProfile()
        viewModel.addSkill(SkillModel(name = "Kotlin"))
        viewModel.addSkill(SkillModel(name = "kotlin"))

        assertEquals(1, viewModel.uiState.value.profile.skills.size)
    }

    @Test
    fun `removeSkill drops the matching skill`() = runTest {
        viewModel.loadProfile()
        viewModel.addSkill(SkillModel(name = "Kotlin"))
        viewModel.removeSkill("kotlin")

        assertTrue(viewModel.uiState.value.profile.skills.isEmpty())
    }
}
