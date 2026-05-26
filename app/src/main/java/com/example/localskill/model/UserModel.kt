package com.example.localskill.model

import android.R.attr.name

data class UserModel (
    val id : String = "",
    val fName: String = "",
    val email : String = "",
    val password: String= "",
    val address : String="",
    val contact : String="",
){
    fun toMap() : Map<String, Any?>{
        return mapOf(
            "name" to name,
            "email" to email,
            "address" to address,
            "contact" to contact
        )
    }
}