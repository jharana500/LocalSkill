package com.example.localskill

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.example.localskill.ui.theme.LocalSkillTheme
import com.example.localskill.utils.FcmTokenUtils
import com.example.localskill.utils.NotificationUtils
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationUtils.createNotificationChannel(this)
        requestNotificationPermissionIfNeeded()
        saveFcmTokenIfSignedIn()
        setContent {
            LocalSkillTheme {
                LocalSkillApp()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }

    private fun saveFcmTokenIfSignedIn() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FcmTokenUtils.saveTokenForUser(userId)
    }
}
