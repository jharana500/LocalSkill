package com.example.localskill.utils

import com.google.firebase.database.DatabaseException

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class FirebaseErrorMapperTest {

    @Test
    fun mapsDatabasePermissionSafely() {
        val message = FirebaseErrorMapper.map(DatabaseException("Permission denied at /secret/path"))

        assertEquals("You do not have permission to perform this action.", message)
    }

    @Test
    fun mapsConnectivitySafely() {
        val message = FirebaseErrorMapper.map(IOException("socket closed"))

        assertEquals("We could not reach the service. Check your connection and try again.", message)
    }

}
