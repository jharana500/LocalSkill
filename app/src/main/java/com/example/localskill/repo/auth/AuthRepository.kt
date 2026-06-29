package com.example.localskill.repo.auth

import com.example.localskill.model.UserModel
import com.example.localskill.utils.Resource

interface AuthRepository {
    fun currentUserId(): String?
    fun login(email: String, password: String, callback: (Resource<String>) -> Unit)
    fun register(user: UserModel, password: String, callback: (Resource<String>) -> Unit)
    fun forgotPassword(email: String, callback: (Resource<Unit>) -> Unit)
    fun logout()
}
