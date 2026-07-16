package com.example.localskill.repo

import com.example.localskill.model.CompanyDocumentModel
import com.example.localskill.model.CompanyDocumentType
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.model.NotificationEntityType
import com.example.localskill.model.NotificationType
import com.example.localskill.model.UserModel
import com.example.localskill.model.UserRole
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class CompanyRepoImpl(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val notificationRepo: NotificationRepo? = null
) : CompanyRepo {

    private val companiesRef: DatabaseReference = database.getReference(Constants.COMPANIES_NODE)
    private val companyDocumentsRef: DatabaseReference = database.getReference(Constants.COMPANY_DOCUMENTS_NODE)
    private val usersRef: DatabaseReference = database.getReference(Constants.USERS_NODE)

    override suspend fun getCompany(companyId: String): ResultState<CompanyModel> = try {
        val snapshot = companiesRef.child(companyId).get().await()
        val company = snapshot.getValue(CompanyModel::class.java)
        if (company != null) ResultState.Success(company) else ResultState.Error("Company profile not found.")
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun updateCompanyProfile(company: CompanyModel): ResultState<Unit> = try {
        companiesRef.child(company.id).updateChildren(company.toEditableMap()).await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun updateLogoUrl(companyId: String, logoUrl: String): ResultState<Unit> = try {
        companiesRef.child(companyId).updateChildren(
            mapOf("logoUrl" to logoUrl, "updatedAt" to System.currentTimeMillis())
        ).await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getDocuments(companyId: String): ResultState<List<CompanyDocumentModel>> = try {
        val snapshot = companyDocumentsRef.child(companyId).get().await()
        val documents = snapshot.children.mapNotNull { it.getValue(CompanyDocumentModel::class.java) }
        ResultState.Success(documents.sortedByDescending { it.uploadedAt })
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun saveDocumentMetadata(document: CompanyDocumentModel): ResultState<Unit> = try {
        companyDocumentsRef.child(document.companyId).child(document.id).setValue(document.toMap()).await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun deleteDocumentMetadata(companyId: String, documentId: String): ResultState<Unit> = try {
        companyDocumentsRef.child(companyId).child(documentId).removeValue().await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun submitVerification(companyId: String): ResultState<Unit> {
        val companyResult = getCompany(companyId)
        if (companyResult !is ResultState.Success) {
            return ResultState.Error((companyResult as? ResultState.Error)?.message ?: "Company profile not found.")
        }
        val company = companyResult.data
        if (company.verificationStatus == CompanyVerificationStatus.VERIFIED.name ||
            company.verificationStatus == CompanyVerificationStatus.PENDING.name
        ) {
            return ResultState.Error("This company is already verified or under review.")
        }
        if (company.missingProfileSections.isNotEmpty()) {
            return ResultState.Error("Complete your company profile before submitting for verification.")
        }

        val documentsResult = getDocuments(companyId)
        val documents = (documentsResult as? ResultState.Success)?.data.orEmpty()
        if (documents.none { it.documentType == CompanyDocumentType.REGISTRATION_CERTIFICATE.name }) {
            return ResultState.Error("Upload your company registration certificate before submitting.")
        }

        return try {
            val now = System.currentTimeMillis()
            val resubmitted = company.verificationStatus == CompanyVerificationStatus.REJECTED.name
            companiesRef.child(companyId).updateChildren(
                mapOf(
                    "verificationStatus" to CompanyVerificationStatus.PENDING.name,
                    "verificationSubmittedAt" to now,
                    "rejectionReason" to "",
                    "updatedAt" to now
                )
            ).await()
            notificationRepo?.createNotificationIfAbsent(
                recipientId = company.ownerUserId,
                senderId = company.ownerUserId,
                type = NotificationType.VERIFICATION_SUBMITTED_CONFIRMATION,
                relatedEntityType = NotificationEntityType.COMPANY,
                relatedEntityId = companyId,
                eventKey = "company:$companyId:verification_submitted:$now:owner"
            )
            notifyAdmins(
                senderId = company.ownerUserId,
                type = if (resubmitted) NotificationType.COMPANY_VERIFICATION_RESUBMITTED else NotificationType.NEW_VERIFICATION_REQUEST,
                companyId = companyId,
                eventKeySuffix = if (resubmitted) "verification_resubmitted:$now" else "verification_submitted:$now"
            )
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    private suspend fun notifyAdmins(
        senderId: String,
        type: NotificationType,
        companyId: String,
        eventKeySuffix: String
    ) {
        val admins = usersRef.get().await().children.mapNotNull { it.getValue(UserModel::class.java) }
            .filter { it.role == UserRole.ADMIN.name }
        admins.forEach { admin ->
            notificationRepo?.createNotificationIfAbsent(
                recipientId = admin.id,
                senderId = senderId,
                type = type,
                relatedEntityType = NotificationEntityType.COMPANY,
                relatedEntityId = companyId,
                eventKey = "company:$companyId:$eventKeySuffix:admin:${admin.id}"
            )
        }
    }
}
