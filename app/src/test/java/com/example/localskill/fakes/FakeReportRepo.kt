package com.example.localskill.fakes

import com.example.localskill.model.JobReportModel
import com.example.localskill.model.ReportStatus
import com.example.localskill.repo.ReportRepo
import com.example.localskill.utils.ResultState

class FakeReportRepo : ReportRepo {

    val reports = mutableMapOf<String, JobReportModel>()
    var submitResult: ResultState<JobReportModel>? = null

    override suspend fun submitReport(report: JobReportModel): ResultState<JobReportModel> {
        submitResult?.let { return it }
        if (report.reason.isBlank()) return ResultState.Error("A reason is required.")
        val duplicate = reports.values.any {
            it.reporterId == report.reporterId &&
                it.targetId == report.targetId &&
                it.status == ReportStatus.PENDING.name
        }
        if (duplicate) return ResultState.Error("You have already reported this.")
        val saved = report.copy(id = "report-${reports.size + 1}", createdAt = System.currentTimeMillis())
        reports[saved.id] = saved
        return ResultState.Success(saved)
    }

    override suspend fun getReports(status: String?): ResultState<List<JobReportModel>> =
        ResultState.Success(reports.values.filter { status == null || it.status == status })

    override suspend fun getReportById(reportId: String): ResultState<JobReportModel> =
        reports[reportId]?.let { ResultState.Success(it) } ?: ResultState.Error("Report not found.")

    override suspend fun markUnderReview(adminId: String, reportId: String): ResultState<Unit> {
        val report = reports[reportId] ?: return ResultState.Error("Report not found.")
        reports[reportId] = report.copy(status = ReportStatus.UNDER_REVIEW.name)
        return ResultState.Success(Unit)
    }

    override suspend fun resolveReport(adminId: String, reportId: String, resolutionNote: String): ResultState<Unit> {
        val report = reports[reportId] ?: return ResultState.Error("Report not found.")
        reports[reportId] = report.copy(status = ReportStatus.RESOLVED.name, resolutionNote = resolutionNote, resolvedBy = adminId)
        return ResultState.Success(Unit)
    }

    override suspend fun rejectReport(adminId: String, reportId: String, resolutionNote: String): ResultState<Unit> {
        val report = reports[reportId] ?: return ResultState.Error("Report not found.")
        reports[reportId] = report.copy(status = ReportStatus.REJECTED.name, resolutionNote = resolutionNote, resolvedBy = adminId)
        return ResultState.Success(Unit)
    }
}
