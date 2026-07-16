package com.example.localskill.fakes

import com.example.localskill.model.UserModel
import com.example.localskill.repo.UserRepo
import com.example.localskill.utils.ResultState

class FakeUserRepo : UserRepo {

    var userResult: ResultState<UserModel> = ResultState.Success(UserModel())
    var allUsersResult: ResultState<List<UserModel>> = ResultState.Success(emptyList())
    var addUserResult: ResultState<Unit> = ResultState.Success(Unit)
    var updateUserResult: ResultState<Unit> = ResultState.Success(Unit)
    var deleteUserResult: ResultState<Unit> = ResultState.Success(Unit)

    override suspend fun addUser(user: UserModel): ResultState<Unit> = addUserResult

    override suspend fun getUserById(userId: String): ResultState<UserModel> = userResult

    override suspend fun getAllUsers(): ResultState<List<UserModel>> = allUsersResult

    override suspend fun updateUser(user: UserModel): ResultState<Unit> = updateUserResult

    override suspend fun deleteUser(userId: String): ResultState<Unit> = deleteUserResult
}
