package com.example.localskill.repo

import com.example.localskill.model.UserModel
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRepoImpl(
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : UserRepo {

    private val usersRef: DatabaseReference = database.getReference(Constants.USERS_NODE)

    override suspend fun addUser(user: UserModel): ResultState<Unit> = try {
        usersRef.child(user.id).setValue(user.toMap()).await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getUserById(userId: String): ResultState<UserModel> = try {
        val snapshot = usersRef.child(userId).get().await()
        val user = snapshot.getValue(UserModel::class.java)
        if (user != null) ResultState.Success(user) else ResultState.Error("User not found")
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getAllUsers(): ResultState<List<UserModel>> = try {
        val snapshot = usersRef.get().await()
        val users = snapshot.children.mapNotNull { it.getValue(UserModel::class.java) }
        ResultState.Success(users)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun updateUser(user: UserModel): ResultState<Unit> = try {
        usersRef.child(user.id).updateChildren(user.toMap()).await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun deleteUser(userId: String): ResultState<Unit> = try {
        usersRef.child(userId).removeValue().await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }
}
