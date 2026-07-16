package com.example.localskill

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.localskill.repo.AppPreferencesRepo
import com.example.localskill.repo.AppPreferencesRepoImpl
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.AuthRepoImpl
import com.example.localskill.repo.UserRepo
import com.example.localskill.repo.UserRepoImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Hand-rolled dependency container. Repositories are created once here and
 * shared across the app instead of being constructed inside composables or
 * ViewModels.
 */
class AppContainer(context: Context) {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    val userRepo: UserRepo by lazy { UserRepoImpl(firebaseDatabase) }

    val authRepo: AuthRepo by lazy { AuthRepoImpl(firebaseAuth, firebaseDatabase, userRepo) }

    val appPreferencesRepo: AppPreferencesRepo by lazy {
        AppPreferencesRepoImpl(context.applicationContext)
    }
}

@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current.applicationContext
    return (context as LocalSkillApplication).appContainer
}
