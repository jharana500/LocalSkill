package com.example.localskill.repo

import com.example.localskill.model.UserModel
import com.example.localskill.utils.ResultState

/**
 * Owns user profile records. Authentication concerns (login, registration,
 * logout, password reset, email verification, session state) live in
 * [AuthRepo] instead.
 */
interface UserRepo {

    suspend fun addUser(user: UserModel): ResultState<Unit>

    suspend fun getUserById(userId: String): ResultState<UserModel>

    suspend fun getAllUsers(): ResultState<List<UserModel>>

    suspend fun updateUser(user: UserModel): ResultState<Unit>

    suspend fun deleteUser(userId: String): ResultState<Unit>
}
