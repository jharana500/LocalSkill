package com.example.localskill.repo.application

import com.example.localskill.model.ApplicationModel
import com.example.localskill.utils.Resource

interface ApplicationRepository {
    fun applyToJob(application: ApplicationModel, callback: (Resource<Unit>) -> Unit)
    fun getApplicationById(applicationId: String, callback: (Resource<ApplicationModel>) -> Unit)
    fun hasWorkerApplied(jobId: String, workerId: String, callback: (Resource<Boolean>) -> Unit)
    fun getApplicationsForWorker(workerId: String, callback: (Resource<List<ApplicationModel>>) -> Unit)
    fun getApplicationsForJob(jobId: String, callback: (Resource<List<ApplicationModel>>) -> Unit)
    fun getApplicationsForEmployer(employerId: String, callback: (Resource<List<ApplicationModel>>) -> Unit)
    fun updateApplicationStatus(
        application: ApplicationModel,
        newStatus: String,
        callback: (Resource<Unit>) -> Unit
    )
}
