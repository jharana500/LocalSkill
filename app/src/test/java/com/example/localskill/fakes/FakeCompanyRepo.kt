package com.example.localskill.fakes

import com.example.localskill.model.CompanyDocumentModel
import com.example.localskill.model.CompanyDocumentType
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.repo.CompanyRepo
import com.example.localskill.utils.ResultState

/** Mirrors CompanyRepoImpl's submitVerification gating so ViewModel tests exercise real business rules. */
class FakeCompanyRepo : CompanyRepo {

    val companies = mutableMapOf<String, CompanyModel>()
    val documents = mutableMapOf<String, MutableList<CompanyDocumentModel>>()

    override suspend fun getCompany(companyId: String): ResultState<CompanyModel> =
        companies[companyId]?.let { ResultState.Success(it) } ?: ResultState.Error("Company profile not found.")

    override suspend fun updateCompanyProfile(company: CompanyModel): ResultState<Unit> {
        val existing = companies[company.id] ?: return ResultState.Error("Company profile not found.")
        companies[company.id] = existing.copy(
            companyName = company.companyName,
            contactPersonName = company.contactPersonName,
            phone = company.phone,
            website = company.website,
            description = company.description,
            industry = company.industry,
            employeeCountRange = company.employeeCountRange,
            registrationNumber = company.registrationNumber,
            panNumber = company.panNumber,
            address = company.address,
            city = company.city,
            district = company.district
        )
        return ResultState.Success(Unit)
    }

    override suspend fun updateLogoUrl(companyId: String, logoUrl: String): ResultState<Unit> {
        val existing = companies[companyId] ?: return ResultState.Error("Company profile not found.")
        companies[companyId] = existing.copy(logoUrl = logoUrl)
        return ResultState.Success(Unit)
    }

    override suspend fun getDocuments(companyId: String): ResultState<List<CompanyDocumentModel>> =
        ResultState.Success(documents[companyId].orEmpty())

    override suspend fun saveDocumentMetadata(document: CompanyDocumentModel): ResultState<Unit> {
        val list = documents.getOrPut(document.companyId) { mutableListOf() }
        list.removeAll { it.id == document.id }
        list.add(document)
        return ResultState.Success(Unit)
    }

    override suspend fun deleteDocumentMetadata(companyId: String, documentId: String): ResultState<Unit> {
        documents[companyId]?.removeAll { it.id == documentId }
        return ResultState.Success(Unit)
    }

    override suspend fun submitVerification(companyId: String): ResultState<Unit> {
        val company = companies[companyId] ?: return ResultState.Error("Company profile not found.")
        if (company.verificationStatus == CompanyVerificationStatus.VERIFIED.name ||
            company.verificationStatus == CompanyVerificationStatus.PENDING.name
        ) {
            return ResultState.Error("This company is already verified or under review.")
        }
        if (company.missingProfileSections.isNotEmpty()) {
            return ResultState.Error("Complete your company profile before submitting for verification.")
        }
        val docs = documents[companyId].orEmpty()
        if (docs.none { it.documentType == CompanyDocumentType.REGISTRATION_CERTIFICATE.name }) {
            return ResultState.Error("Upload your company registration certificate before submitting.")
        }
        companies[companyId] = company.copy(
            verificationStatus = CompanyVerificationStatus.PENDING.name,
            verificationSubmittedAt = System.currentTimeMillis(),
            rejectionReason = ""
        )
        return ResultState.Success(Unit)
    }
}
