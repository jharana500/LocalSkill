package com.example.localskill.repo.auth

import com.example.localskill.model.UserModel
import com.example.localskill.repo.user.UserRepository
import com.example.localskill.utils.Resource
import com.example.localskill.utils.readableMessage
import com.google.firebase.auth.FirebaseAuth

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository = com.example.localskill.repo.user.UserRepositoryImpl()
) : AuthRepository {
    override fun currentUserId(): String? = auth.currentUser?.uid

    override fun login(email: String, password: String, callback: (Resource<String>) -> Unit) {
        callback(Resource.Loading)
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid
                if (userId == null) callback(Resource.Error("Unable to find user account"))
                else callback(Resource.Success(userId))
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun register(user: UserModel, password: String, callback: (Resource<String>) -> Unit) {
        callback(Resource.Loading)
        auth.createUserWithEmailAndPassword(user.email.trim(), password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid
                if (userId == null) {
                    callback(Resource.Error("Unable to create user account"))
                    return@addOnSuccessListener
                }
                val now = System.currentTimeMillis()
                val profile = user.copy(id = userId, createdAt = now, updatedAt = now)
                userRepository.saveUser(profile) { saveResult ->
                    when (saveResult) {
                        is Resource.Success -> callback(Resource.Success(userId))
                        is Resource.Error -> callback(Resource.Error(saveResult.message))
                        Resource.Loading -> Unit
                    }
                }
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun forgotPassword(email: String, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun logout() {
        auth.signOut()
    }
}
