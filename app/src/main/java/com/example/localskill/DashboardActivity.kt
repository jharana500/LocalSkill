package com.example.localskill

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DashboardScreen()
        }
    }
}

data class Skill(
    val title: String,
    val price: String,
    val location: String
)

@Composable
fun DashboardScreen() {

    val context = LocalContext.current

    val skillList = listOf(
        Skill("Math Tutor", "Rs.500/hr", "Baneshwor"),
        Skill("Graphic Designer", "Rs.2000", "Kathmandu"),
        Skill("Video Editor", "Rs.1500", "Lalitpur")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "SkillSetu",
                fontSize = 28.sp
            )

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = "",
            onValueChange = {

            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Search Skills")
            },
            leadingIcon = {
                Icon(Icons.Default.Search, null)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                context.startActivity(
                    Intent(context, AddSkillActivity::class.java)
                )

            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4F46E5)
            )
        ) {

            Icon(Icons.Default.Add, null)

            Spacer(modifier = Modifier.height(10.dp))

            Text("Add Skill")
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {

            items(skillList) { skill ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = skill.title,
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(skill.price)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(skill.location)

                        Spacer(modifier = Modifier.height(15.dp))

                        Button(
                            onClick = {

                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F46E5)
                            )
                        ) {

                            Text("View")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    DashboardScreen()
}