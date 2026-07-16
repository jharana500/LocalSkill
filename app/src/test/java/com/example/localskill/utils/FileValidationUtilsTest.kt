package com.example.localskill.utils

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FileValidationUtilsTest {

    @Test
    fun `pdf resume within size limit passes`() {
        assertNull(FileValidationUtils.validateResumeFile("application/pdf", 1024L))
    }

    @Test
    fun `docx resume passes`() {
        assertNull(
            FileValidationUtils.validateResumeFile(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                1024L
            )
        )
    }

    @Test
    fun `unsupported resume mime type fails`() {
        assertNotNull(FileValidationUtils.validateResumeFile("image/png", 1024L))
    }

    @Test
    fun `null resume mime type fails`() {
        assertNotNull(FileValidationUtils.validateResumeFile(null, 1024L))
    }

    @Test
    fun `empty resume file fails`() {
        assertNotNull(FileValidationUtils.validateResumeFile("application/pdf", 0L))
    }

    @Test
    fun `oversized resume file fails`() {
        assertNotNull(FileValidationUtils.validateResumeFile("application/pdf", Constants.MAX_RESUME_SIZE_BYTES + 1))
    }

    @Test
    fun `jpeg image within size limit passes`() {
        assertNull(FileValidationUtils.validateImageFile("image/jpeg", 1024L))
    }

    @Test
    fun `unsupported image mime type fails`() {
        assertNotNull(FileValidationUtils.validateImageFile("application/pdf", 1024L))
    }

    @Test
    fun `oversized image file fails`() {
        assertNotNull(FileValidationUtils.validateImageFile("image/png", Constants.MAX_PROFILE_IMAGE_SIZE_BYTES + 1))
    }
}
