package com.example.localskill.repo

import android.content.Context
import android.net.Uri
import com.example.localskill.model.CompanyDocumentModel
import com.example.localskill.model.ResumeModel
import com.example.localskill.services.FileValidationService
import com.example.localskill.utils.Constants
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

/**
 * Uploads to Cloudinary via an unsigned upload preset instead of Firebase Storage.
 * Unsigned uploads cannot be securely deleted from a client with no backend (that
 * requires a signed request, which would mean shipping the Cloudinary API secret
 * inside the APK) — so every delete*() here is a deliberate no-op. Replacing a
 * file just stops referencing the old URL; the old asset is left orphaned in
 * Cloudinary rather than exposing the account's secret to reverse engineering.
 */
class CloudinaryFileRepoImpl(
    private val context: Context,
    private val fileValidationService: FileValidationService,
    private val httpClient: OkHttpClient = OkHttpClient()
) : FileRepo {

    override suspend fun uploadResume(userId: String, uri: Uri): ResultState<ResumeModel> {
        val metadata = fileValidationService.readMetadata(uri)
            ?: return ResultState.Error("Unable to read this file. Please choose another.")
        fileValidationService.validateResume(metadata)?.let { return ResultState.Error(it) }

        return try {
            val downloadUrl = uploadFile(uri, folder("resumes", userId), metadata.displayName, metadata.mimeType)
            ResultState.Success(
                ResumeModel(
                    fileName = metadata.displayName,
                    downloadUrl = downloadUrl,
                    fileSizeBytes = metadata.sizeBytes,
                    uploadedAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            ResultState.Error(mapError(e), e)
        }
    }

    override suspend fun deleteResume(downloadUrl: String): ResultState<Unit> = ResultState.Success(Unit)

    override suspend fun uploadProfileImage(userId: String, uri: Uri): ResultState<String> {
        val metadata = fileValidationService.readMetadata(uri)
            ?: return ResultState.Error("Unable to read this image. Please choose another.")
        fileValidationService.validateImage(metadata)?.let { return ResultState.Error(it) }

        return try {
            ResultState.Success(uploadFile(uri, folder("profileImages", userId), metadata.displayName, metadata.mimeType))
        } catch (e: Exception) {
            ResultState.Error(mapError(e), e)
        }
    }

    override suspend fun deleteProfileImage(downloadUrl: String): ResultState<Unit> = ResultState.Success(Unit)

    override suspend fun uploadCompanyLogo(companyId: String, uri: Uri): ResultState<String> {
        val metadata = fileValidationService.readMetadata(uri)
            ?: return ResultState.Error("Unable to read this image. Please choose another.")
        fileValidationService.validateCompanyLogo(metadata)?.let { return ResultState.Error(it) }

        return try {
            ResultState.Success(uploadFile(uri, folder("companyLogos", companyId), metadata.displayName, metadata.mimeType))
        } catch (e: Exception) {
            ResultState.Error(mapError(e), e)
        }
    }

    override suspend fun deleteCompanyLogo(downloadUrl: String): ResultState<Unit> = ResultState.Success(Unit)

    override suspend fun uploadCompanyDocument(
        companyId: String,
        documentType: String,
        uri: Uri
    ): ResultState<CompanyDocumentModel> {
        val metadata = fileValidationService.readMetadata(uri)
            ?: return ResultState.Error("Unable to read this file. Please choose another.")
        fileValidationService.validateCompanyDocument(metadata)?.let { return ResultState.Error(it) }

        return try {
            val downloadUrl = uploadFile(
                uri,
                folder("companyDocuments", companyId, documentType),
                metadata.displayName,
                metadata.mimeType
            )
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
            ResultState.Error(mapError(e), e)
        }
    }

    override suspend fun deleteCompanyDocument(downloadUrl: String): ResultState<Unit> = ResultState.Success(Unit)

    override suspend fun uploadReportEvidence(reporterId: String, uri: Uri): ResultState<String> {
        val metadata = fileValidationService.readMetadata(uri)
            ?: return ResultState.Error("Unable to read this file. Please choose another.")
        fileValidationService.validateReportEvidence(metadata)?.let { return ResultState.Error(it) }

        return try {
            ResultState.Success(uploadFile(uri, folder("reportEvidence", reporterId), metadata.displayName, metadata.mimeType))
        } catch (e: Exception) {
            ResultState.Error(mapError(e), e)
        }
    }

    private fun folder(vararg segments: String): String =
        (listOf(Constants.CLOUDINARY_FOLDER_ROOT) + segments).joinToString("/")

    private suspend fun uploadFile(uri: Uri, folder: String, displayName: String, mimeType: String?): String =
        withContext(Dispatchers.IO) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IOException("Unable to read this file. Please choose another.")
            val mediaType = (mimeType ?: "application/octet-stream").toMediaTypeOrNull()

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", displayName, bytes.toRequestBody(mediaType))
                .addFormDataPart("upload_preset", Constants.CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart("folder", folder)
                .build()

            val request = Request.Builder()
                .url(Constants.CLOUDINARY_UPLOAD_URL)
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val serverMessage = runCatching {
                        JSONObject(responseText).getJSONObject("error").getString("message")
                    }.getOrNull()
                    throw IOException(serverMessage ?: "Upload failed. Please try again.")
                }
                JSONObject(responseText).getString("secure_url")
            }
        }

    private fun mapError(e: Exception): String = when (e) {
        is IOException -> e.message?.takeIf { it.isNotBlank() } ?: "Upload failed. Check your internet connection and try again."
        else -> "Something went wrong. Please try again."
    }
}
