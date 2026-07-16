package com.example.localskill.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanyModelTest {

    private val completeCompany = CompanyModel(
        companyName = "Acme",
        description = "We build things.",
        industry = "Technology",
        address = "123 Main St",
        city = "Kathmandu",
        registrationNumber = "REG-1",
        logoUrl = "https://example.com/logo.png"
    )

    @Test
    fun `a fully completed profile has no missing sections`() {
        assertTrue(completeCompany.missingProfileSections.isEmpty())
        assertEquals(100, completeCompany.profileCompletionPercentage)
    }

    @Test
    fun `a blank profile is missing every section`() {
        val blank = CompanyModel()
        assertEquals(7, blank.missingProfileSections.size)
        assertEquals(0, blank.profileCompletionPercentage)
    }

    @Test
    fun `missing the logo alone is reported`() {
        val missingLogo = completeCompany.copy(logoUrl = "")
        assertEquals(listOf("Company logo"), missingLogo.missingProfileSections)
    }

    @Test
    fun `verification status flags match verificationStatus`() {
        assertTrue(completeCompany.copy(verificationStatus = CompanyVerificationStatus.VERIFIED.name).isVerified)
        assertTrue(completeCompany.copy(verificationStatus = CompanyVerificationStatus.PENDING.name).isPending)
        assertTrue(completeCompany.copy(verificationStatus = CompanyVerificationStatus.REJECTED.name).isRejected)
        assertTrue(completeCompany.copy(verificationStatus = CompanyVerificationStatus.DRAFT.name).isDraft)
    }
}
