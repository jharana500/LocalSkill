package com.example.localskill.viewmodel

import com.example.localskill.model.EducationModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.model.SkillModel
import com.example.localskill.model.UserModel
import com.example.localskill.repo.ApplicationRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.FileRepo
import com.example.localskill.repo.JobSeekerProfileRepo
import com.example.localskill.repo.SavedJobRepo
import com.example.localskill.repo.UserRepo
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class JobSeekerProfileViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var userRepo: UserRepo
    private lateinit var profileRepo: JobSeekerProfileRepo
    private lateinit var applicationRepo: ApplicationRepo
    private lateinit var savedJobRepo: SavedJobRepo
    private lateinit var fileRepo: FileRepo
    private lateinit var viewModel: JobSeekerProfileViewModel

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("uid-123")
        userRepo = mock()
        whenever(userRepo.getUserById("uid-123")).thenReturn(
            ResultState.Success(UserModel(id = "uid-123", fullName = "Jane Doe", email = "jane@example.com"))
        )
        profileRepo = mock()
        applicationRepo = mock()
        whenever(applicationRepo.getUserApplications("uid-123")).thenReturn(ResultState.Success(emptyList()))
        savedJobRepo = mock()
        whenever(savedJobRepo.getSavedJobIds("uid-123")).thenReturn(ResultState.Success(emptySet()))
        fileRepo = mock()
        viewModel = JobSeekerProfileViewModel(authRepo, userRepo, profileRepo, applicationRepo, savedJobRepo, fileRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProfile exposes account and profile data together`() = runTest {
        whenever(profileRepo.getProfile("uid-123")).thenReturn(ResultState.Success(JobSeekerProfileModel(userId = "uid-123")))

        viewModel.loadProfile()

        val state = viewModel.uiState.value
        assertEquals("Jane Doe", state.fullName)
        assertEquals("jane@example.com", state.email)
    }

    @Test
    fun `updatePersonalInfo saves and reloads`() = runTest {
        val updated = JobSeekerProfileModel(userId = "uid-123", headline = "Android Developer", city = "Kathmandu")
        whenever(profileRepo.getProfile("uid-123")).thenReturn(
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123")),
            ResultState.Success(updated)
        )
        whenever(profileRepo.updatePersonalInfo("uid-123", "Android Developer", "Bio text", "Kathmandu", "Kathmandu"))
            .thenReturn(ResultState.Success(Unit))

        viewModel.loadProfile()
        viewModel.updatePersonalInfo("Android Developer", "Bio text", "Kathmandu", "Kathmandu")

        assertEquals("Android Developer", viewModel.uiState.value.profile.headline)
        assertEquals("Kathmandu", viewModel.uiState.value.profile.city)
    }

    @Test
    fun `addEducation appends a new entry with a stable id`() = runTest {
        val education = EducationModel(id = "edu-1", institution = "Tribhuvan University", qualification = "BSc")
        whenever(profileRepo.getProfile("uid-123")).thenReturn(
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123")),
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123", education = listOf(education)))
        )
        whenever(profileRepo.addEducation(any(), any())).thenReturn(ResultState.Success(Unit))

        viewModel.loadProfile()
        viewModel.addEducation(EducationModel(institution = "Tribhuvan University", qualification = "BSc"))

        val result = viewModel.uiState.value.profile.education
        assertEquals(1, result.size)
        assertTrue(result.first().id.isNotBlank())
    }

    @Test
    fun `removeEducation removes only the targeted entry`() = runTest {
        val keep = EducationModel(id = "keep", institution = "A")
        val drop = EducationModel(id = "drop", institution = "B")
        whenever(profileRepo.getProfile("uid-123")).thenReturn(
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123")),
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123", education = listOf(keep))),
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123", education = listOf(keep, drop))),
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123", education = listOf(keep)))
        )
        whenever(profileRepo.addEducation(any(), any())).thenReturn(ResultState.Success(Unit))
        whenever(profileRepo.removeEducation("uid-123", "drop")).thenReturn(ResultState.Success(Unit))

        viewModel.loadProfile()
        viewModel.addEducation(keep)
        viewModel.addEducation(drop)
        viewModel.removeEducation("drop")

        assertEquals(listOf("keep"), viewModel.uiState.value.profile.education.map { it.id })
    }

    @Test
    fun `addSkill rejects a case-insensitive duplicate`() = runTest {
        whenever(profileRepo.getProfile("uid-123")).thenReturn(
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123")),
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123", skills = listOf(SkillModel(name = "Kotlin"))))
        )
        whenever(profileRepo.addSkill("uid-123", SkillModel(name = "Kotlin"))).thenReturn(ResultState.Success(Unit))
        whenever(profileRepo.addSkill("uid-123", SkillModel(name = "kotlin")))
            .thenReturn(ResultState.Error("This skill is already on your profile."))

        viewModel.loadProfile()
        viewModel.addSkill(SkillModel(name = "Kotlin"))
        viewModel.addSkill(SkillModel(name = "kotlin"))

        assertEquals(1, viewModel.uiState.value.profile.skills.size)
    }

    @Test
    fun `removeSkill drops the matching skill`() = runTest {
        whenever(profileRepo.getProfile("uid-123")).thenReturn(
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123")),
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123", skills = listOf(SkillModel(name = "Kotlin")))),
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123", skills = emptyList()))
        )
        whenever(profileRepo.addSkill("uid-123", SkillModel(name = "Kotlin"))).thenReturn(ResultState.Success(Unit))
        whenever(profileRepo.removeSkill("uid-123", "kotlin")).thenReturn(ResultState.Success(Unit))

        viewModel.loadProfile()
        viewModel.addSkill(SkillModel(name = "Kotlin"))
        viewModel.removeSkill("kotlin")

        assertTrue(viewModel.uiState.value.profile.skills.isEmpty())
    }
}
