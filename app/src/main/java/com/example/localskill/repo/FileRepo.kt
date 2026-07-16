package com.example.localskill.repo

import android.net.Uri
import com.example.localskill.model.ResumeModel
import com.example.localskill.utils.ResultState

interface FileRepo {

    suspend fun uploadResume(userId: String, uri: Uri): ResultState<ResumeModel>

    suspend fun deleteResume(downloadUrl: String): ResultState<Unit>

    suspend fun uploadProfileImage(userId: String, uri: Uri): ResultState<String>

    suspend fun deleteProfileImage(downloadUrl: String): ResultState<Unit>
}
