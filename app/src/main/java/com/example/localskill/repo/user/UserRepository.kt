package com.example.localskill.repo.user

import com.example.localskill.model.UserModel
import com.example.localskill.model.UserRole
import com.example.localskill.utils.Resource

interface UserRepository {
    fun saveUser(user: UserModel, callback: (Resource<Unit>) -> Unit)
    fun getUser(userId: String, callback: (Resource<UserModel>) -> Unit)
    fun getWorkers(callback: (Resource<List<UserModel>>) -> Unit)
    fun updateRole(userId: String, role: UserRole, callback: (Resource<Unit>) -> Unit)
    fun updateProfile(user: UserModel, callback: (Resource<Unit>) -> Unit)
}
