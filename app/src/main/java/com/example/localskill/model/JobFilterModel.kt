package com.example.localskill.model

enum class JobSortOption {
    MOST_RELEVANT,
    NEWEST,
    SALARY_HIGH_TO_LOW,
    SALARY_LOW_TO_HIGH,
    DEADLINE_SOONEST
}

data class JobFilterModel(
    val query: String = "",
    val categoryId: String? = null,
    val location: String? = null,
    val jobType: String? = null,
    val workplaceType: String? = null,
    val experienceLevel: String? = null,
    val minimumSalary: Double? = null,
    val maximumSalary: Double? = null,
    val datePostedWithinDays: Int? = null,
    val verifiedCompanyOnly: Boolean = false,
    val sortOption: String = JobSortOption.MOST_RELEVANT.name
) {
    val activeFilterCount: Int
        get() = listOfNotNull(
            categoryId,
            location,
            jobType,
            workplaceType,
            experienceLevel,
            minimumSalary,
            maximumSalary,
            datePostedWithinDays
        ).size + (if (verifiedCompanyOnly) 1 else 0)

    val hasActiveFilters: Boolean
        get() = activeFilterCount > 0
}
