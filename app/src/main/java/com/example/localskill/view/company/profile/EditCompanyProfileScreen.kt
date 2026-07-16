package com.example.localskill.view.company.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localskill.view.common.components.LocalSkillPrimaryButton
import com.example.localskill.view.common.components.LocalSkillTextField
import com.example.localskill.view.common.components.LocalSkillTopAppBar
import com.example.localskill.view.theme.Spacing
import com.example.localskill.viewmodel.CompanyProfileEvent
import com.example.localskill.viewmodel.CompanyProfileViewModel

@Composable
fun EditCompanyProfileScreen(
    viewModel: CompanyProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var companyName by remember(uiState.company) { mutableStateOf(uiState.company.companyName) }
    var contactPersonName by remember(uiState.company) { mutableStateOf(uiState.company.contactPersonName) }
    var phone by remember(uiState.company) { mutableStateOf(uiState.company.phone) }
    var website by remember(uiState.company) { mutableStateOf(uiState.company.website) }
    var description by remember(uiState.company) { mutableStateOf(uiState.company.description) }
    var industry by remember(uiState.company) { mutableStateOf(uiState.company.industry) }
    var employeeCountRange by remember(uiState.company) { mutableStateOf(uiState.company.employeeCountRange) }
    var registrationNumber by remember(uiState.company) { mutableStateOf(uiState.company.registrationNumber) }
    var panNumber by remember(uiState.company) { mutableStateOf(uiState.company.panNumber) }
    var address by remember(uiState.company) { mutableStateOf(uiState.company.address) }
    var city by remember(uiState.company) { mutableStateOf(uiState.company.city) }
    var district by remember(uiState.company) { mutableStateOf(uiState.company.district) }

    LaunchedEffect(Unit) { viewModel.loadProfile() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CompanyProfileEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { LocalSkillTopAppBar(title = "Edit company profile", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
        ) {
            LocalSkillTextField(value = companyName, onValueChange = { companyName = it }, label = "Company name")
            LocalSkillTextField(value = contactPersonName, onValueChange = { contactPersonName = it }, label = "Contact person", modifier = Modifier.padding(top = Spacing.sm))
            LocalSkillTextField(value = phone, onValueChange = { phone = it }, label = "Phone", modifier = Modifier.padding(top = Spacing.sm))
            LocalSkillTextField(value = website, onValueChange = { website = it }, label = "Website", modifier = Modifier.padding(top = Spacing.sm))
            LocalSkillTextField(value = description, onValueChange = { description = it }, label = "Description", singleLine = false, modifier = Modifier.padding(top = Spacing.sm))
            LocalSkillTextField(value = industry, onValueChange = { industry = it }, label = "Industry", modifier = Modifier.padding(top = Spacing.sm))
            LocalSkillTextField(value = employeeCountRange, onValueChange = { employeeCountRange = it }, label = "Employee count (e.g. 11-50)", modifier = Modifier.padding(top = Spacing.sm))
            LocalSkillTextField(value = registrationNumber, onValueChange = { registrationNumber = it }, label = "Registration number", modifier = Modifier.padding(top = Spacing.sm))
            LocalSkillTextField(value = panNumber, onValueChange = { panNumber = it }, label = "PAN number", modifier = Modifier.padding(top = Spacing.sm))
            LocalSkillTextField(value = address, onValueChange = { address = it }, label = "Address", modifier = Modifier.padding(top = Spacing.sm))
            LocalSkillTextField(value = city, onValueChange = { city = it }, label = "City", modifier = Modifier.padding(top = Spacing.sm))
            LocalSkillTextField(value = district, onValueChange = { district = it }, label = "District", modifier = Modifier.padding(top = Spacing.sm))

            LocalSkillPrimaryButton(
                text = "Save",
                onClick = {
                    viewModel.updateProfile(
                        companyName = companyName,
                        contactPersonName = contactPersonName,
                        phone = phone,
                        website = website,
                        description = description,
                        industry = industry,
                        employeeCountRange = employeeCountRange,
                        registrationNumber = registrationNumber,
                        panNumber = panNumber,
                        address = address,
                        city = city,
                        district = district
                    )
                },
                isLoading = uiState.isSaving,
                enabled = companyName.isNotBlank(),
                modifier = Modifier.padding(top = Spacing.lg)
            )
        }
    }
}
