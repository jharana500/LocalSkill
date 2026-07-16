package com.example.localskill.repo

import com.example.localskill.model.JobReportModel
import com.example.localskill.utils.ResultState

interface ReportRepo {

    /** Rejects a blank reason and a duplicate open report from the same reporter against the same target. */
    suspend fun submitReport(report: JobReportModel): ResultState<JobReportModel>

    suspend fun getReports(status: String? = null): ResultState<List<JobReportModel>>

    suspend fun getReportById(reportId: String): ResultState<JobReportModel>

    suspend fun markUnderReview(adminId: String, reportId: String): ResultState<Unit>

    suspend fun resolveReport(adminId: String, reportId: String, resolutionNote: String): ResultState<Unit>

    suspend fun rejectReport(adminId: String, reportId: String, resolutionNote: String): ResultState<Unit>
}
