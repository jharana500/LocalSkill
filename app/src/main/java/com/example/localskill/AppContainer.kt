package com.example.localskill

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.localskill.repo.AdminRepo
import com.example.localskill.repo.AdminRepoImpl
import com.example.localskill.repo.AppPreferencesRepo
import com.example.localskill.repo.AppPreferencesRepoImpl
import com.example.localskill.repo.ApplicantRepo
import com.example.localskill.repo.ApplicantRepoImpl
import com.example.localskill.repo.ApplicationRepo
import com.example.localskill.repo.ApplicationRepoImpl
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.AuthRepoImpl
import com.example.localskill.repo.CompanyJobRepo
import com.example.localskill.repo.CompanyJobRepoImpl
import com.example.localskill.repo.CompanyRepo
import com.example.localskill.repo.CompanyRepoImpl
import com.example.localskill.repo.FileRepo
import com.example.localskill.repo.FileRepoImpl
import com.example.localskill.repo.JobRepo
import com.example.localskill.repo.JobRepoImpl
import com.example.localskill.repo.JobSeekerProfileRepo
import com.example.localskill.repo.JobSeekerProfileRepoImpl
import com.example.localskill.repo.ReportRepo
import com.example.localskill.repo.ReportRepoImpl
import com.example.localskill.repo.SavedJobRepo
import com.example.localskill.repo.SavedJobRepoImpl
import com.example.localskill.repo.UserRepo
import com.example.localskill.repo.UserRepoImpl
import com.example.localskill.services.FileValidationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

/**
 * Hand-rolled dependency container. Repositories are created once here and
 * shared across the app instead of being constructed inside composables or
 * ViewModels.
 */
class AppContainer(context: Context) {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    val userRepo: UserRepo by lazy { UserRepoImpl(firebaseDatabase) }

    val authRepo: AuthRepo by lazy { AuthRepoImpl(firebaseAuth, firebaseDatabase, userRepo) }

    val appPreferencesRepo: AppPreferencesRepo by lazy {
        AppPreferencesRepoImpl(context.applicationContext)
    }

    val jobRepo: JobRepo by lazy { JobRepoImpl(firebaseDatabase) }

    val jobSeekerProfileRepo: JobSeekerProfileRepo by lazy { JobSeekerProfileRepoImpl(firebaseDatabase) }

    val applicationRepo: ApplicationRepo by lazy { ApplicationRepoImpl(firebaseDatabase) }

    val savedJobRepo: SavedJobRepo by lazy { SavedJobRepoImpl(firebaseDatabase) }

    private val fileValidationService: FileValidationService by lazy {
        FileValidationService(context.applicationContext)
    }

    val fileRepo: FileRepo by lazy { FileRepoImpl(fileValidationService, firebaseStorage) }

    val companyRepo: CompanyRepo by lazy { CompanyRepoImpl(firebaseDatabase) }

    val companyJobRepo: CompanyJobRepo by lazy { CompanyJobRepoImpl(firebaseDatabase, companyRepo) }

    val applicantRepo: ApplicantRepo by lazy { ApplicantRepoImpl(firebaseDatabase, jobSeekerProfileRepo) }

    val adminRepo: AdminRepo by lazy { AdminRepoImpl(firebaseDatabase, userRepo) }

    val reportRepo: ReportRepo by lazy { ReportRepoImpl(firebaseDatabase) }
}

@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current.applicationContext
    return (context as LocalSkillApplication).appContainer
}
