package com.example.localskill.utils

object FileValidationUtils {

    val ACCEPTED_RESUME_MIME_TYPES = setOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    val ACCEPTED_IMAGE_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp")

    val ACCEPTED_DOCUMENT_MIME_TYPES = setOf("application/pdf", "image/jpeg", "image/png")

    fun validateResumeFile(mimeType: String?, sizeBytes: Long): String? = when {
        mimeType == null -> "Unable to read this file. Please choose another."
        mimeType !in ACCEPTED_RESUME_MIME_TYPES -> "Choose a PDF or Word document."
        sizeBytes <= 0L -> "This file appears to be empty."
        sizeBytes > Constants.MAX_RESUME_SIZE_BYTES -> "File is larger than 5 MB. Choose a smaller file."
        else -> null
    }

    fun validateImageFile(mimeType: String?, sizeBytes: Long): String? = when {
        mimeType == null -> "Unable to read this image. Please choose another."
        mimeType !in ACCEPTED_IMAGE_MIME_TYPES -> "Choose a JPEG, PNG, or WEBP image."
        sizeBytes <= 0L -> "This image appears to be empty."
        sizeBytes > Constants.MAX_PROFILE_IMAGE_SIZE_BYTES -> "Image is larger than 3 MB. Choose a smaller image."
        else -> null
    }

    fun validateCompanyLogo(mimeType: String?, sizeBytes: Long): String? = when {
        mimeType == null -> "Unable to read this image. Please choose another."
        mimeType !in ACCEPTED_IMAGE_MIME_TYPES -> "Choose a JPEG, PNG, or WEBP image."
        sizeBytes <= 0L -> "This image appears to be empty."
        sizeBytes > Constants.MAX_COMPANY_LOGO_SIZE_BYTES -> "Logo is larger than 3 MB. Choose a smaller image."
        else -> null
    }

    fun validateCompanyDocument(mimeType: String?, sizeBytes: Long): String? = when {
        mimeType == null -> "Unable to read this file. Please choose another."
        mimeType !in ACCEPTED_DOCUMENT_MIME_TYPES -> "Choose a PDF, JPEG, or PNG file."
        sizeBytes <= 0L -> "This file appears to be empty."
        sizeBytes > Constants.MAX_COMPANY_DOCUMENT_SIZE_BYTES -> "File is larger than 8 MB. Choose a smaller file."
        else -> null
    }

    fun validateReportEvidence(mimeType: String?, sizeBytes: Long): String? = when {
        mimeType == null -> "Unable to read this file. Please choose another."
        mimeType !in ACCEPTED_DOCUMENT_MIME_TYPES -> "Choose a PDF, JPEG, or PNG file."
        sizeBytes <= 0L -> "This file appears to be empty."
        sizeBytes > Constants.MAX_REPORT_EVIDENCE_SIZE_BYTES -> "File is larger than 5 MB. Choose a smaller file."
        else -> null
    }
}
