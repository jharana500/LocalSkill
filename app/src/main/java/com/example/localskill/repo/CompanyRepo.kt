package com.example.localskill.repo

import com.example.localskill.model.CompanyDocumentModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.utils.ResultState

/**
 * Owns the company's own editable profile, its verification document
 * metadata, and submitting for verification. Admin-controlled decisions
 * (approve/reject/suspend) live in [AdminRepo] instead, which is the only
 * repository trusted to write verificationStatus/verifiedAt/verifiedBy.
 */
interface CompanyRepo {

    suspend fun getCompany(companyId: String): ResultState<CompanyModel>

    /** Writes only business-editable fields; verification fields are never touched here. */
    suspend fun updateCompanyProfile(company: CompanyModel): ResultState<Unit>

    suspend fun updateLogoUrl(companyId: String, logoUrl: String): ResultState<Unit>

    suspend fun getDocuments(companyId: String): ResultState<List<CompanyDocumentModel>>

    suspend fun saveDocumentMetadata(document: CompanyDocumentModel): ResultState<Unit>

    suspend fun deleteDocumentMetadata(companyId: String, documentId: String): ResultState<Unit>

    /** Moves a DRAFT or REJECTED company to PENDING once its profile and documents are complete. */
    suspend fun submitVerification(companyId: String): ResultState<Unit>
}
