package com.example.localskill.repo

import com.example.localskill.model.CompanyDocumentModel
import com.example.localskill.model.CompanyDocumentType
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class CompanyRepoImpl(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : CompanyRepo {

    private val companiesRef: DatabaseReference = database.getReference(Constants.COMPANIES_NODE)
    private val companyDocumentsRef: DatabaseReference = database.getReference(Constants.COMPANY_DOCUMENTS_NODE)

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
            companiesRef.child(companyId).updateChildren(
                mapOf(
                    "verificationStatus" to CompanyVerificationStatus.PENDING.name,
                    "verificationSubmittedAt" to System.currentTimeMillis(),
                    "rejectionReason" to "",
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }
}
