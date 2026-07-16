package com.example.localskill.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JobSeekerProfileModelTest {

    @Test
    fun `empty profile has zero completion`() {
        assertEquals(0, JobSeekerProfileModel().completionPercentage)
    }

    @Test
    fun `fully filled profile is 100 percent complete`() {
        val profile = JobSeekerProfileModel(
            headline = "Android Developer",
            bio = "Building things",
            city = "Kathmandu",
            skills = listOf(SkillModel(name = "Kotlin")),
            education = listOf(EducationModel(institution = "TU", qualification = "BSc")),
            experience = listOf(ExperienceModel(jobTitle = "Dev", company = "Acme")),
            resume = ResumeModel(fileName = "resume.pdf", downloadUrl = "https://example.com/resume.pdf"),
            profileImageUrl = "https://example.com/photo.jpg"
        )
        assertEquals(100, profile.completionPercentage)
    }

    @Test
    fun `partially filled profile reports missing sections`() {
        val profile = JobSeekerProfileModel(headline = "Android Developer", bio = "Bio", city = "Kathmandu")
        assertTrue(profile.missingSections.contains("Skills"))
        assertTrue(profile.missingSections.contains("Education"))
        assertTrue(profile.missingSections.contains("Experience"))
        assertTrue(profile.missingSections.contains("Resume"))
        assertTrue(profile.missingSections.contains("Profile photo"))
        assertTrue(!profile.missingSections.contains("Personal information"))
    }

    @Test
    fun `completion percentage is deterministic for the same input`() {
        val profile = JobSeekerProfileModel(headline = "Headline", city = "Pokhara")
        assertEquals(profile.completionPercentage, profile.copy().completionPercentage)
    }
}
