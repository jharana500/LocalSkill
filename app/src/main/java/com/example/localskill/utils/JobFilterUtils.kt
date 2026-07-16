package com.example.localskill.utils

import com.example.localskill.model.JobFilterModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobSortOption

/**
 * Pure in-memory filtering/sorting over an already-loaded active-job list —
 * the accepted strategy for this project's current scale. Kept separate
 * from JobRepo so it stays trivially unit-testable and so a future
 * server-side/paginated search can replace it without touching the UI.
 */
object JobFilterUtils {

    fun applyFilters(jobs: List<JobModel>, filter: JobFilterModel): List<JobModel> {
        val query = filter.query.trim().lowercase()
        val filtered = jobs.filter { job ->
            (query.isBlank() || job.matchesQuery(query)) &&
                (filter.categoryId == null || job.categoryId == filter.categoryId) &&
                (filter.location == null || job.location.equals(filter.location, ignoreCase = true)) &&
                (filter.jobType == null || job.jobType == filter.jobType) &&
                (filter.workplaceType == null || job.workplaceType == filter.workplaceType) &&
                (filter.experienceLevel == null || job.experienceLevel == filter.experienceLevel) &&
                (filter.minimumSalary == null || jobMeetsMinimumSalary(job, filter.minimumSalary)) &&
                (filter.maximumSalary == null || jobMeetsMaximumSalary(job, filter.maximumSalary)) &&
                (filter.datePostedWithinDays == null || isWithinDays(job.createdAt, filter.datePostedWithinDays)) &&
                (!filter.verifiedCompanyOnly || job.companyVerified)
        }
        return sortJobs(filtered, filter.sortOption)
    }

    private fun JobModel.matchesQuery(query: String): Boolean =
        title.lowercase().contains(query) ||
            companyName.lowercase().contains(query) ||
            location.lowercase().contains(query) ||
            skills.any { it.lowercase().contains(query) }

    private fun jobMeetsMinimumSalary(job: JobModel, minimumSalary: Double): Boolean =
        (job.maximumSalary ?: job.minimumSalary ?: 0.0) >= minimumSalary

    private fun jobMeetsMaximumSalary(job: JobModel, maximumSalary: Double): Boolean =
        (job.minimumSalary ?: job.maximumSalary ?: Double.MAX_VALUE) <= maximumSalary

    private fun isWithinDays(timestampMillis: Long, days: Int): Boolean {
        val cutoff = System.currentTimeMillis() - days * 24L * 60 * 60 * 1000
        return timestampMillis >= cutoff
    }

    fun sortJobs(jobs: List<JobModel>, sortOption: String): List<JobModel> = when (sortOption) {
        JobSortOption.NEWEST.name -> jobs.sortedByDescending { it.createdAt }
        JobSortOption.SALARY_HIGH_TO_LOW.name ->
            jobs.sortedByDescending { it.maximumSalary ?: it.minimumSalary ?: -1.0 }

        JobSortOption.SALARY_LOW_TO_HIGH.name ->
            jobs.sortedBy { it.minimumSalary ?: it.maximumSalary ?: Double.MAX_VALUE }

        JobSortOption.DEADLINE_SOONEST.name ->
            jobs.sortedBy { if (it.applicationDeadline <= 0L) Long.MAX_VALUE else it.applicationDeadline }

        else -> jobs.sortedWith(compareByDescending<JobModel> { it.featured }.thenByDescending { it.createdAt })
    }
}
