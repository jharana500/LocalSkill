package com.example.localskill.repo

import com.example.localskill.model.UserModel
import com.example.localskill.utils.ResultState

interface UserRepo {

    suspend fun register(email: String, password: String): ResultState<String>

    suspend fun login(email: String, password: String): ResultState<String>

    suspend fun addUser(user: UserModel): ResultState<Unit>

    suspend fun getUserById(userId: String): ResultState<UserModel>

    suspend fun getAllUsers(): ResultState<List<UserModel>>

    suspend fun updateUser(user: UserModel): ResultState<Unit>

    suspend fun deleteUser(userId: String): ResultState<Unit>

    suspend fun sendPasswordResetEmail(email: String): ResultState<Unit>

    fun logout()
}
