package com.example.localskill.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyJobRepo
import com.example.localskill.repo.CompanyRepo
import com.example.localskill.repo.JobRepo
import com.example.localskill.utils.JobValidationUtils
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class CompanyJobsUiState(
    val isLoading: Boolean = true,
    val jobs: List<JobModel> = emptyList(),
    val errorMessage: String? = null
)

data class CompanyJobFormUiState(
    val isLoading: Boolean = false,
    val jobId: String = "",
    val title: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val description: String = "",
    val responsibilities: List<String> = emptyList(),
    val requirements: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val location: String = "",
    val jobType: String = "",
    val workplaceType: String = "",
    val minimumSalary: Double? = null,
    val maximumSalary: Double? = null,
    val experienceLevel: String = "",
    val educationRequirement: String = "",
    val vacancyCount: Int = 1,
    val applicationDeadline: Long = 0L,
    val categories: List<JobCategoryModel> = emptyList(),
    val isSaving: Boolean = false,
    val isPublishing: Boolean = false,
    val saveSuccess: Boolean = false,
    val publishSuccess: Boolean = false,
    val violations: List<String> = emptyList(),
    val errorMessage: String? = null
) {
    val isEditingExisting: Boolean get() = jobId.isNotBlank()
}

sealed class CompanyJobEvent {
    data class ShowMessage(val message: String) : CompanyJobEvent()
}

class CompanyJobViewModel(
    private val authRepo: AuthRepo,
    private val companyRepo: CompanyRepo,
    private val companyJobRepo: CompanyJobRepo,
    private val jobRepo: JobRepo
) : ViewModel() {

    private val _jobsUiState = MutableStateFlow(CompanyJobsUiState())
    val jobsUiState: StateFlow<CompanyJobsUiState> = _jobsUiState.asStateFlow()

    private val _formUiState = MutableStateFlow(CompanyJobFormUiState())
    val formUiState: StateFlow<CompanyJobFormUiState> = _formUiState.asStateFlow()

    private val _events = Channel<CompanyJobEvent>(Channel.BUFFERED)
    val events: Flow<CompanyJobEvent> = _events.receiveAsFlow()

    fun loadJobs() {
        val companyId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            _jobsUiState.value = _jobsUiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = companyJobRepo.getCompanyJobs(companyId)) {
                is ResultState.Success -> _jobsUiState.value = CompanyJobsUiState(isLoading = false, jobs = result.data)
                is ResultState.Error -> _jobsUiState.value = CompanyJobsUiState(isLoading = false, errorMessage = result.message)
                else -> Unit
            }
        }
    }

    fun loadForm(jobId: String?) {
        viewModelScope.launch {
            _formUiState.value = CompanyJobFormUiState(isLoading = true)
            val categoriesResult = jobRepo.getCategories()
            val categories = (categoriesResult as? ResultState.Success)?.data ?: emptyList()

            if (jobId.isNullOrBlank()) {
                _formUiState.value = CompanyJobFormUiState(isLoading = false, categories = categories)
                return@launch
            }

            val companyId = authRepo.currentUserId() ?: return@launch
            when (val result = companyJobRepo.getCompanyJobById(companyId, jobId)) {
                is ResultState.Success -> {
                    val job = result.data
                    _formUiState.value = CompanyJobFormUiState(
                        isLoading = false,
                        jobId = job.id,
                        title = job.title,
                        categoryId = job.categoryId,
                        categoryName = job.categoryName,
                        description = job.description,
                        responsibilities = job.responsibilities,
                        requirements = job.requirements,
                        skills = job.skills,
                        location = job.location,
                        jobType = job.jobType,
                        workplaceType = job.workplaceType,
                        minimumSalary = job.minimumSalary,
                        maximumSalary = job.maximumSalary,
                        experienceLevel = job.experienceLevel,
                        educationRequirement = job.educationRequirement,
                        vacancyCount = job.vacancyCount,
                        applicationDeadline = job.applicationDeadline,
                        categories = categories
                    )
                }

                is ResultState.Error -> _formUiState.value = CompanyJobFormUiState(
                    isLoading = false,
                    categories = categories,
                    errorMessage = result.message
                )

                else -> Unit
            }
        }
    }

    fun updateTitle(value: String) = updateForm { it.copy(title = value) }
    fun updateCategory(category: JobCategoryModel) =
        updateForm { it.copy(categoryId = category.id, categoryName = category.name) }
    fun updateDescription(value: String) = updateForm { it.copy(description = value) }
    fun updateLocation(value: String) = updateForm { it.copy(location = value) }
    fun updateJobType(value: String) = updateForm { it.copy(jobType = value) }
    fun updateWorkplaceType(value: String) = updateForm { it.copy(workplaceType = value) }
    fun updateMinimumSalary(value: Double?) = updateForm { it.copy(minimumSalary = value) }
    fun updateMaximumSalary(value: Double?) = updateForm { it.copy(maximumSalary = value) }
    fun updateExperienceLevel(value: String) = updateForm { it.copy(experienceLevel = value) }
    fun updateEducationRequirement(value: String) = updateForm { it.copy(educationRequirement = value) }
    fun updateVacancyCount(value: Int) = updateForm { it.copy(vacancyCount = value) }
    fun updateDeadline(value: Long) = updateForm { it.copy(applicationDeadline = value) }

    fun addResponsibility(value: String) {
        if (value.isBlank()) return
        updateForm { it.copy(responsibilities = it.responsibilities + value.trim()) }
    }

    fun removeResponsibility(index: Int) =
        updateForm { it.copy(responsibilities = it.responsibilities.filterIndexed { i, _ -> i != index }) }

    fun addRequirement(value: String) {
        if (value.isBlank()) return
        updateForm { it.copy(requirements = it.requirements + value.trim()) }
    }

    fun removeRequirement(index: Int) =
        updateForm { it.copy(requirements = it.requirements.filterIndexed { i, _ -> i != index }) }

    fun addSkill(value: String) {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return
        val current = _formUiState.value.skills
        if (current.any { it.equals(trimmed, ignoreCase = true) }) return
        updateForm { it.copy(skills = it.skills + trimmed) }
    }

    fun removeSkill(value: String) = updateForm { it.copy(skills = it.skills.filterNot { s -> s == value }) }

    private fun updateForm(transform: (CompanyJobFormUiState) -> CompanyJobFormUiState) {
        _formUiState.value = transform(_formUiState.value).copy(violations = emptyList(), errorMessage = null)
    }

    fun saveDraft() {
        if (_formUiState.value.isSaving) return
        viewModelScope.launch {
            _formUiState.value = _formUiState.value.copy(isSaving = true)
            when (val result = persistJob()) {
                is ResultState.Success -> {
                    _formUiState.value = _formUiState.value.copy(
                        isSaving = false,
                        jobId = result.data.id,
                        saveSuccess = true
                    )
                    _events.send(CompanyJobEvent.ShowMessage("Draft saved."))
                }

                is ResultState.Error -> {
                    _formUiState.value = _formUiState.value.copy(isSaving = false, errorMessage = result.message)
                    _events.send(CompanyJobEvent.ShowMessage(result.message))
                }

                else -> Unit
            }
        }
    }

    fun publish() {
        if (_formUiState.value.isPublishing) return
        val companyId = authRepo.currentUserId() ?: return
        val state = _formUiState.value

        val violations = JobValidationUtils.validatePublishReadiness(
            title = state.title,
            description = state.description,
            categoryId = state.categoryId,
            location = state.location,
            jobType = state.jobType,
            workplaceType = state.workplaceType,
            vacancyCount = state.vacancyCount,
            applicationDeadline = state.applicationDeadline,
            minimumSalary = state.minimumSalary,
            maximumSalary = state.maximumSalary,
            skills = state.skills
        )
        if (violations.isNotEmpty()) {
            _formUiState.value = state.copy(violations = violations)
            return
        }

        viewModelScope.launch {
            _formUiState.value = _formUiState.value.copy(isPublishing = true, violations = emptyList())

            val jobId = _formUiState.value.jobId.ifBlank {
                when (val saved = persistJob()) {
                    is ResultState.Success -> saved.data.id
                    is ResultState.Error -> {
                        _formUiState.value = _formUiState.value.copy(isPublishing = false, errorMessage = saved.message)
                        _events.send(CompanyJobEvent.ShowMessage(saved.message))
                        return@launch
                    }
                    else -> return@launch
                }
            }
            _formUiState.value = _formUiState.value.copy(jobId = jobId)

            when (val publishResult = companyJobRepo.publishJob(companyId, jobId)) {
                is ResultState.Success -> {
                    _formUiState.value = _formUiState.value.copy(isPublishing = false, publishSuccess = true)
                    _events.send(CompanyJobEvent.ShowMessage("Job published."))
                }

                is ResultState.Error -> {
                    _formUiState.value = _formUiState.value.copy(isPublishing = false, errorMessage = publishResult.message)
                    _events.send(CompanyJobEvent.ShowMessage(publishResult.message))
                }

                else -> Unit
            }
        }
    }

    fun closeJob(jobId: String) = mutateJobLifecycle { companyId -> companyJobRepo.closeJob(companyId, jobId) }
    fun reopenJob(jobId: String) = mutateJobLifecycle { companyId -> companyJobRepo.reopenJob(companyId, jobId) }
    fun deleteDraft(jobId: String) = mutateJobLifecycle { companyId -> companyJobRepo.deleteDraft(companyId, jobId) }

    private fun mutateJobLifecycle(action: suspend (companyId: String) -> ResultState<Unit>) {
        val companyId = authRepo.currentUserId() ?: return
        viewModelScope.launch {
            when (val result = action(companyId)) {
                is ResultState.Error -> _events.send(CompanyJobEvent.ShowMessage(result.message))
                is ResultState.Success -> loadJobs()
                else -> Unit
            }
        }
    }

    private suspend fun persistJob(): ResultState<JobModel> {
        val companyId = authRepo.currentUserId() ?: return ResultState.Error("You must be signed in.")
        val state = _formUiState.value
        if (state.title.isBlank()) return ResultState.Error("Job title is required to save a draft.")

        val companyResult = companyRepo.getCompany(companyId)
        val company = (companyResult as? ResultState.Success)?.data

        val job = JobModel(
            id = state.jobId,
            companyId = companyId,
            companyName = company?.companyName.orEmpty(),
            companyLogoUrl = company?.logoUrl.orEmpty(),
            companyVerified = company?.isVerified == true,
            title = state.title,
            description = state.description,
            responsibilities = state.responsibilities,
            requirements = state.requirements,
            skills = state.skills,
            categoryId = state.categoryId,
            categoryName = state.categoryName,
            location = state.location,
            jobType = state.jobType,
            workplaceType = state.workplaceType,
            minimumSalary = state.minimumSalary,
            maximumSalary = state.maximumSalary,
            experienceLevel = state.experienceLevel,
            educationRequirement = state.educationRequirement,
            vacancyCount = state.vacancyCount,
            applicationDeadline = state.applicationDeadline,
            status = JobStatus.DRAFT.name
        )

        return if (state.isEditingExisting) {
            when (val result = companyJobRepo.updateJob(companyId, job)) {
                is ResultState.Success -> ResultState.Success(job)
                is ResultState.Error -> result
                else -> ResultState.Error("Unable to save this job.")
            }
        } else {
            companyJobRepo.createDraft(job)
        }
    }
}
