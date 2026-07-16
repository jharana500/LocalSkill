package com.example.localskill.repo

import com.example.localskill.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.toMap

class UserRepoImp : UserRepo {
    val auth = FirebaseAuth.getInstance()

    val database = FirebaseDatabase.getInstance()

    val ref = database.getReference("user")
    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback

                } else {
                    callback(false, "${it.exception?.message}")
                }

            }
    }

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback

                } else {
                    callback(false, "${it.exception?.message}", "")
                }

            }
    }

    //C RUD
    override fun addUser(
        id: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        //to auto generate ID
        //  val id = ref.push().key.toString()
        ref.child(id).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "User Registered")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback

                } else {
                    callback(false, "${it.exception?.message}")
                }

            }

    }

    //CURD -- U
    override fun editProfile(
        id: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(id).updateChildren(model.toMap()).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Profile updated")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteUser(
        id: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(id).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Profile updated")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }


    override fun getUserById(
        id: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        ref.child(id).addValueEventListener((object : ValueEventListener {
            override fun onDataChange(snapshort: DataSnapshot) {
                if (snapshort.exists()) {
                    val user = snapshort.getValue(UserModel::class.java)

                    user.let {
                        callback(true, "user featched", it)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, "${error.message}", null)
            }
        }))
    }

    override fun getAllUser(callback: (Boolean, String, List<UserModel>) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshort: DataSnapshot) {
                if (snapshort.exists()) {
                    val allUsers = mutableListOf<UserModel>()
                    for (user in snapshort.children) {
                        val data = user.getValue(UserModel::class.java)
                        if (data != null) {
                            allUsers.add(data)
                        }
                    }
                    callback(true, "fetched", allUsers)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            callback(true, "Logout successful")
        } catch (e: Exception) {
            callback(false, e.toString())
        }
    }
}