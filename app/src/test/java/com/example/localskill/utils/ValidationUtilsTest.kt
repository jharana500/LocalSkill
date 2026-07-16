package com.example.localskill.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `valid email passes`() {
        assertNull(ValidationUtils.validateEmail("jane@example.com"))
    }

    @Test
    fun `email missing at sign fails`() {
        assertNotNull(ValidationUtils.validateEmail("janeexample.com"))
    }

    @Test
    fun `empty email fails`() {
        assertNotNull(ValidationUtils.validateEmail(""))
    }

    @Test
    fun `email with leading whitespace fails`() {
        assertNotNull(ValidationUtils.validateEmail(" jane@example.com"))
    }

    @Test
    fun `local Nepal phone number passes`() {
        assertNull(ValidationUtils.validatePhone("9812345678"))
    }

    @Test
    fun `plus977 Nepal phone number passes`() {
        assertNull(ValidationUtils.validatePhone("+9779812345678"))
    }

    @Test
    fun `phone number too short fails`() {
        assertNotNull(ValidationUtils.validatePhone("98123"))
    }

    @Test
    fun `phone number with wrong prefix fails`() {
        assertNotNull(ValidationUtils.validatePhone("1234567890"))
    }

    @Test
    fun `password meeting all requirements passes`() {
        assertNull(ValidationUtils.validatePassword("Str0ngPass"))
    }

    @Test
    fun `password shorter than minimum length fails`() {
        assertNotNull(ValidationUtils.validatePassword("Ab1"))
    }

    @Test
    fun `password without a digit fails`() {
        assertNotNull(ValidationUtils.validatePassword("NoDigitsHere"))
    }

    @Test
    fun `password without an uppercase letter fails`() {
        assertNotNull(ValidationUtils.validatePassword("nouppercase1"))
    }

    @Test
    fun `matching confirm password passes`() {
        assertNull(ValidationUtils.validateConfirmPassword("Str0ngPass", "Str0ngPass"))
    }

    @Test
    fun `mismatched confirm password fails`() {
        assertNotNull(ValidationUtils.validateConfirmPassword("Str0ngPass", "Different1"))
    }

    @Test
    fun `empty confirm password fails`() {
        assertNotNull(ValidationUtils.validateConfirmPassword("Str0ngPass", ""))
    }

    @Test
    fun `terms not accepted fails`() {
        assertNotNull(ValidationUtils.validateTermsAccepted(false))
    }

    @Test
    fun `terms accepted passes`() {
        assertNull(ValidationUtils.validateTermsAccepted(true))
    }

    @Test
    fun `full name requires at least a few characters`() {
        assertNotNull(ValidationUtils.validateFullName("Jo"))
        assertNull(ValidationUtils.validateFullName("Jane Doe"))
    }

    @Test
    fun `password strength scales with complexity`() {
        assertEquals(PasswordStrength.NONE, ValidationUtils.passwordStrength(""))
        assertEquals(PasswordStrength.WEAK, ValidationUtils.passwordStrength("abcdefgh"))
        assertEquals(PasswordStrength.STRONG, ValidationUtils.passwordStrength("Str0ng!Pass123"))
    }
}
