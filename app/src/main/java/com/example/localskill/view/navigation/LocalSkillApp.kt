package com.example.localskill.view.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.localskill.view.theme.LocalSkillTheme

@Composable
fun LocalSkillApp() {
    LocalSkillTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavGraph()
        }
    }
}
