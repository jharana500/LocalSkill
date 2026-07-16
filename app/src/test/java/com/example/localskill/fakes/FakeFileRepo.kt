package com.example.localskill.fakes

import android.net.Uri
import com.example.localskill.model.ResumeModel
import com.example.localskill.repo.FileRepo
import com.example.localskill.utils.ResultState

class FakeFileRepo : FileRepo {

    var uploadResumeResult: ResultState<ResumeModel> =
        ResultState.Success(ResumeModel(fileName = "resume.pdf", downloadUrl = "https://example.com/resume.pdf"))
    var uploadImageResult: ResultState<String> = ResultState.Success("https://example.com/photo.jpg")

    override suspend fun uploadResume(userId: String, uri: Uri): ResultState<ResumeModel> = uploadResumeResult

    override suspend fun deleteResume(downloadUrl: String): ResultState<Unit> = ResultState.Success(Unit)

    override suspend fun uploadProfileImage(userId: String, uri: Uri): ResultState<String> = uploadImageResult

    override suspend fun deleteProfileImage(downloadUrl: String): ResultState<Unit> = ResultState.Success(Unit)
}
