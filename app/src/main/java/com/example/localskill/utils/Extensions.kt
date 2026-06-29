package com.example.localskill.utils

fun Throwable.readableMessage(): String = message ?: "Something went wrong. Please try again."
