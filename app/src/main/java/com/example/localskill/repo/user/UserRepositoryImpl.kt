package com.example.localskill.repo.user

import com.example.localskill.model.UserModel
import com.example.localskill.model.UserRole
import com.example.localskill.utils.Constants
import com.example.localskill.utils.Resource
import com.example.localskill.utils.readableMessage
import com.google.firebase.database.FirebaseDatabase

class UserRepositoryImpl(
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : UserRepository {
    private val ref = database.getReference(Constants.USERS)

    override fun saveUser(user: UserModel, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        ref.child(user.id).setValue(user)
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getUser(userId: String, callback: (Resource<UserModel>) -> Unit) {
        callback(Resource.Loading)
        ref.child(userId).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(UserModel::class.java)
                if (user == null) callback(Resource.Error("User profile not found"))
                else callback(Resource.Success(user))
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun getWorkers(callback: (Resource<List<UserModel>>) -> Unit) {
        callback(Resource.Loading)
        ref.orderByChild("role").equalTo(UserRole.WORKER.name).get()
            .addOnSuccessListener { snapshot ->
                val workers = snapshot.children.mapNotNull { it.getValue(UserModel::class.java) }
                callback(Resource.Success(workers))
            }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun updateRole(userId: String, role: UserRole, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        ref.child(userId).updateChildren(
            mapOf(
                "role" to role.name,
                "updatedAt" to System.currentTimeMillis()
            )
        )
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }

    override fun updateProfile(user: UserModel, callback: (Resource<Unit>) -> Unit) {
        callback(Resource.Loading)
        ref.child(user.id).setValue(user.copy(updatedAt = System.currentTimeMillis()))
            .addOnSuccessListener { callback(Resource.Success(Unit)) }
            .addOnFailureListener { callback(Resource.Error(it.readableMessage())) }
    }
}
