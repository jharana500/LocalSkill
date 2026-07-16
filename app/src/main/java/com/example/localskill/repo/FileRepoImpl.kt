package com.example.localskill.repo

import android.net.Uri
import com.example.localskill.model.CompanyDocumentModel
import com.example.localskill.model.ResumeModel
import com.example.localskill.services.FileValidationService
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FileRepoImpl(
    private val fileValidationService: FileValidationService,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : FileRepo {

    override suspend fun uploadResume(userId: String, uri: Uri): ResultState<ResumeModel> {
        val metadata = fileValidationService.readMetadata(uri)
            ?: return ResultState.Error("Unable to read this file. Please choose another.")
        fileValidationService.validateResume(metadata)?.let { return ResultState.Error(it) }

        return try {
            val fileName = "${UUID.randomUUID()}_${metadata.displayName}"
            val ref = storage.reference
                .child(Constants.RESUMES_STORAGE_PATH)
                .child(userId)
                .child(fileName)
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            ResultState.Success(
                ResumeModel(
                    fileName = metadata.displayName,
                    downloadUrl = downloadUrl,
                    fileSizeBytes = metadata.sizeBytes,
                    uploadedAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun deleteResume(downloadUrl: String): ResultState<Unit> = deleteByUrl(downloadUrl)

    override suspend fun uploadProfileImage(userId: String, uri: Uri): ResultState<String> {
        val metadata = fileValidationService.readMetadata(uri)
            ?: return ResultState.Error("Unable to read this image. Please choose another.")
        fileValidationService.validateImage(metadata)?.let { return ResultState.Error(it) }

        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val ref = storage.reference
                .child(Constants.PROFILE_IMAGES_STORAGE_PATH)
                .child(userId)
                .child(fileName)
            ref.putFile(uri).await()
            ResultState.Success(ref.downloadUrl.await().toString())
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun deleteProfileImage(downloadUrl: String): ResultState<Unit> = deleteByUrl(downloadUrl)

    override suspend fun uploadCompanyLogo(companyId: String, uri: Uri): ResultState<String> {
        val metadata = fileValidationService.readMetadata(uri)
            ?: return ResultState.Error("Unable to read this image. Please choose another.")
        fileValidationService.validateCompanyLogo(metadata)?.let { return ResultState.Error(it) }

        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val ref = storage.reference
                .child(Constants.COMPANY_LOGOS_STORAGE_PATH)
                .child(companyId)
                .child(fileName)
            ref.putFile(uri).await()
            ResultState.Success(ref.downloadUrl.await().toString())
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun deleteCompanyLogo(downloadUrl: String): ResultState<Unit> = deleteByUrl(downloadUrl)

    override suspend fun uploadCompanyDocument(
        companyId: String,
        documentType: String,
        uri: Uri
    ): ResultState<CompanyDocumentModel> {
        val metadata = fileValidationService.readMetadata(uri)
            ?: return ResultState.Error("Unable to read this file. Please choose another.")
        fileValidationService.validateCompanyDocument(metadata)?.let { return ResultState.Error(it) }

        return try {
            val fileName = "${UUID.randomUUID()}_${metadata.displayName}"
            val ref = storage.reference
                .child(Constants.COMPANY_DOCUMENTS_STORAGE_PATH)
                .child(companyId)
                .child(documentType)
                .child(fileName)
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            ResultState.Success(
                CompanyDocumentModel(
                    id = UUID.randomUUID().toString(),
                    companyId = companyId,
                    documentType = documentType,
                    fileName = metadata.displayName,
                    downloadUrl = downloadUrl,
                    fileSizeBytes = metadata.sizeBytes,
                    uploadedAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun deleteCompanyDocument(downloadUrl: String): ResultState<Unit> = deleteByUrl(downloadUrl)

    override suspend fun uploadReportEvidence(reporterId: String, uri: Uri): ResultState<String> {
        val metadata = fileValidationService.readMetadata(uri)
            ?: return ResultState.Error("Unable to read this file. Please choose another.")
        fileValidationService.validateReportEvidence(metadata)?.let { return ResultState.Error(it) }

        return try {
            val fileName = "${UUID.randomUUID()}_${metadata.displayName}"
            val ref = storage.reference
                .child(Constants.REPORT_EVIDENCE_STORAGE_PATH)
                .child(reporterId)
                .child(fileName)
            ref.putFile(uri).await()
            ResultState.Success(ref.downloadUrl.await().toString())
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    private suspend fun deleteByUrl(downloadUrl: String): ResultState<Unit> {
        if (downloadUrl.isBlank()) return ResultState.Success(Unit)
        return try {
            storage.getReferenceFromUrl(downloadUrl).delete().await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }
}
