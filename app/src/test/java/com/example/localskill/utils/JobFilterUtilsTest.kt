package com.example.localskill.utils

import com.example.localskill.model.JobFilterModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobSortOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JobFilterUtilsTest {

    private fun job(
        id: String,
        title: String = "Job $id",
        companyName: String = "Company",
        location: String = "Kathmandu",
        jobType: String = "FULL_TIME",
        featured: Boolean = false,
        verified: Boolean = false,
        minSalary: Double? = null,
        maxSalary: Double? = null,
        createdAt: Long = 1_000L,
        deadline: Long = 0L
    ) = JobModel(
        id = id,
        title = title,
        companyName = companyName,
        location = location,
        jobType = jobType,
        featured = featured,
        companyVerified = verified,
        minimumSalary = minSalary,
        maximumSalary = maxSalary,
        createdAt = createdAt,
        applicationDeadline = deadline
    )

    @Test
    fun `query matches title company location and skills`() {
        val jobs = listOf(
            job("1", title = "Android Developer"),
            job("2", companyName = "Acme Kotlin Labs"),
            job("3", location = "Pokhara"),
            job("4").copy(skills = listOf("Kotlin"))
        )

        val result = JobFilterUtils.applyFilters(jobs, JobFilterModel(query = "kotlin"))

        assertEquals(setOf("2", "4"), result.map { it.id }.toSet())
    }

    @Test
    fun `blank query returns all jobs`() {
        val jobs = listOf(job("1"), job("2"))
        val result = JobFilterUtils.applyFilters(jobs, JobFilterModel(query = "   "))
        assertEquals(2, result.size)
    }

    @Test
    fun `job type filter narrows results`() {
        val jobs = listOf(job("1", jobType = "FULL_TIME"), job("2", jobType = "INTERNSHIP"))
        val result = JobFilterUtils.applyFilters(jobs, JobFilterModel(jobType = "INTERNSHIP"))
        assertEquals(listOf("2"), result.map { it.id })
    }

    @Test
    fun `verified company only filter excludes unverified`() {
        val jobs = listOf(job("1", verified = true), job("2", verified = false))
        val result = JobFilterUtils.applyFilters(jobs, JobFilterModel(verifiedCompanyOnly = true))
        assertEquals(listOf("1"), result.map { it.id })
    }

    @Test
    fun `salary range filter matches overlapping jobs`() {
        val jobs = listOf(
            job("1", minSalary = 20000.0, maxSalary = 30000.0),
            job("2", minSalary = 50000.0, maxSalary = 70000.0)
        )
        val result = JobFilterUtils.applyFilters(jobs, JobFilterModel(minimumSalary = 40000.0))
        assertEquals(listOf("2"), result.map { it.id })
    }

    @Test
    fun `date posted within days excludes older jobs`() {
        val now = System.currentTimeMillis()
        val jobs = listOf(
            job("recent", createdAt = now),
            job("old", createdAt = now - 40L * 24 * 60 * 60 * 1000)
        )
        val result = JobFilterUtils.applyFilters(jobs, JobFilterModel(datePostedWithinDays = 7))
        assertEquals(listOf("recent"), result.map { it.id })
    }

    @Test
    fun `sort newest orders by createdAt descending`() {
        val jobs = listOf(job("old", createdAt = 100L), job("new", createdAt = 200L))
        val result = JobFilterUtils.sortJobs(jobs, JobSortOption.NEWEST.name)
        assertEquals(listOf("new", "old"), result.map { it.id })
    }

    @Test
    fun `sort salary high to low orders correctly`() {
        val jobs = listOf(
            job("low", minSalary = 10000.0, maxSalary = 15000.0),
            job("high", minSalary = 40000.0, maxSalary = 60000.0)
        )
        val result = JobFilterUtils.sortJobs(jobs, JobSortOption.SALARY_HIGH_TO_LOW.name)
        assertEquals(listOf("high", "low"), result.map { it.id })
    }

    @Test
    fun `sort deadline soonest puts no-deadline jobs last`() {
        val jobs = listOf(
            job("no-deadline", deadline = 0L),
            job("soon", deadline = 1_000L),
            job("later", deadline = 5_000L)
        )
        val result = JobFilterUtils.sortJobs(jobs, JobSortOption.DEADLINE_SOONEST.name)
        assertEquals(listOf("soon", "later", "no-deadline"), result.map { it.id })
    }

    @Test
    fun `most relevant sort surfaces featured jobs first`() {
        val jobs = listOf(job("regular", featured = false, createdAt = 200L), job("featured", featured = true, createdAt = 100L))
        val result = JobFilterUtils.sortJobs(jobs, JobSortOption.MOST_RELEVANT.name)
        assertTrue(result.first().id == "featured")
    }
}
