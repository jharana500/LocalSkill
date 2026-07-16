package com.example.localskill.repo

import com.example.localskill.model.UserModel
import com.example.localskill.utils.Constants
import com.example.localskill.utils.ResultState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class UserRepoImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : UserRepo {

    private val usersRef: DatabaseReference = database.getReference(Constants.USERS_NODE)

    override suspend fun register(email: String, password: String): ResultState<String> =
        suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(ResultState.Success(task.result?.user?.uid.orEmpty()))
                    } else {
                        continuation.resume(
                            ResultState.Error(task.exception?.message ?: "Registration failed", task.exception)
                        )
                    }
                }
        }

    override suspend fun login(email: String, password: String): ResultState<String> =
        suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(ResultState.Success(task.result?.user?.uid.orEmpty()))
                    } else {
                        continuation.resume(
                            ResultState.Error(task.exception?.message ?: "Login failed", task.exception)
                        )
                    }
                }
        }

    override suspend fun addUser(user: UserModel): ResultState<Unit> =
        suspendCancellableCoroutine { continuation ->
            usersRef.child(user.id).setValue(user.toMap())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(ResultState.Success(Unit))
                    } else {
                        continuation.resume(
                            ResultState.Error(task.exception?.message ?: "Unable to save user", task.exception)
                        )
                    }
                }
        }

    override suspend fun getUserById(userId: String): ResultState<UserModel> =
        suspendCancellableCoroutine { continuation ->
            usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserModel::class.java)
                    if (user != null) {
                        continuation.resume(ResultState.Success(user))
                    } else {
                        continuation.resume(ResultState.Error("User not found"))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(ResultState.Error(error.message, error.toException()))
                }
            })
        }

    override suspend fun getAllUsers(): ResultState<List<UserModel>> =
        suspendCancellableCoroutine { continuation ->
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val users = snapshot.children.mapNotNull { it.getValue(UserModel::class.java) }
                    continuation.resume(ResultState.Success(users))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(ResultState.Error(error.message, error.toException()))
                }
            })
        }

    override suspend fun updateUser(user: UserModel): ResultState<Unit> =
        suspendCancellableCoroutine { continuation ->
            usersRef.child(user.id).updateChildren(user.toMap())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(ResultState.Success(Unit))
                    } else {
                        continuation.resume(
                            ResultState.Error(task.exception?.message ?: "Unable to update user", task.exception)
                        )
                    }
                }
        }

    override suspend fun deleteUser(userId: String): ResultState<Unit> =
        suspendCancellableCoroutine { continuation ->
            usersRef.child(userId).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(ResultState.Success(Unit))
                    } else {
                        continuation.resume(
                            ResultState.Error(task.exception?.message ?: "Unable to delete user", task.exception)
                        )
                    }
                }
        }

    override suspend fun sendPasswordResetEmail(email: String): ResultState<Unit> =
        suspendCancellableCoroutine { continuation ->
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(ResultState.Success(Unit))
                    } else {
                        continuation.resume(
                            ResultState.Error(task.exception?.message ?: "Unable to send reset email", task.exception)
                        )
                    }
                }
        }

    override fun logout() {
        auth.signOut()
    }
}
