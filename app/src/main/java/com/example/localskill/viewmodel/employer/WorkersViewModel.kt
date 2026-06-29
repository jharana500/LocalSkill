package com.example.localskill.viewmodel.employer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.localskill.model.UserModel
import com.example.localskill.repo.auth.AuthRepository
import com.example.localskill.repo.auth.AuthRepositoryImpl
import com.example.localskill.repo.user.UserRepository
import com.example.localskill.repo.user.UserRepositoryImpl
import com.example.localskill.utils.LocationUtils
import com.example.localskill.utils.Resource

data class WorkersUiState(
    val workers: List<UserModel> = emptyList(),
    val filteredWorkers: List<UserModel> = emptyList(),
    val searchQuery: String = "",
    val selectedRadiusKm: Double? = null,
    val employerLatitude: Double? = null,
    val employerLongitude: Double? = null,
    val employerCity: String = "",
    val employerArea: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class WorkersViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl()
) : ViewModel() {
    var uiState by mutableStateOf(WorkersUiState())
        private set

    fun load() {
        authRepository.currentUserId()?.let { employerId ->
            userRepository.getUser(employerId) { result ->
                if (result is Resource.Success) {
                    uiState = uiState.copy(
                        employerLatitude = result.data.latitude,
                        employerLongitude = result.data.longitude,
                        employerCity = result.data.city,
                        employerArea = result.data.area
                    )
                    uiState = uiState.copy(filteredWorkers = filterWorkers(uiState.workers))
                }
            }
        }
        userRepository.getWorkers { result ->
            uiState = when (result) {
                Resource.Loading -> uiState.copy(isLoading = true, errorMessage = null)
                is Resource.Error -> uiState.copy(isLoading = false, errorMessage = result.message)
                is Resource.Success -> uiState.copy(
                    isLoading = false,
                    workers = result.data,
                    filteredWorkers = filterWorkers(result.data)
                )
            }
        }
    }

    fun onSearchChange(value: String) {
        uiState = uiState.copy(
            searchQuery = value,
            filteredWorkers = filterWorkers(uiState.workers, searchQuery = value),
            errorMessage = null
        )
    }

    fun onRadiusSelected(value: Double?) {
        uiState = uiState.copy(
            selectedRadiusKm = value,
            filteredWorkers = filterWorkers(uiState.workers, radiusKm = value),
            errorMessage = null
        )
    }

    fun distanceText(worker: UserModel): String? =
        LocationUtils.distanceKmOrNull(uiState.employerLatitude, uiState.employerLongitude, worker.latitude, worker.longitude)
            ?.let(LocationUtils::formatDistance)

    private fun filterWorkers(
        workers: List<UserModel>,
        searchQuery: String = uiState.searchQuery,
        radiusKm: Double? = uiState.selectedRadiusKm
    ): List<UserModel> {
        val query = searchQuery.trim()
        val filtered = workers.filter { worker ->
            val matchesSearch = query.isBlank() ||
                worker.fullName.contains(query, ignoreCase = true) ||
                worker.bio.contains(query, ignoreCase = true) ||
                worker.experience.contains(query, ignoreCase = true) ||
                worker.city.contains(query, ignoreCase = true) ||
                worker.area.contains(query, ignoreCase = true) ||
                worker.location.contains(query, ignoreCase = true)
            val distance = LocationUtils.distanceKmOrNull(uiState.employerLatitude, uiState.employerLongitude, worker.latitude, worker.longitude)
            val matchesRadius = radiusKm == null || distance?.let { it <= radiusKm } ?: textLocationMatches(worker)
            matchesSearch && matchesRadius
        }
        return filtered.sortedWith(compareBy<UserModel> {
            LocationUtils.distanceKmOrNull(uiState.employerLatitude, uiState.employerLongitude, it.latitude, it.longitude)
                ?: Double.MAX_VALUE
        }.thenBy { it.fullName })
    }

    private fun textLocationMatches(worker: UserModel): Boolean {
        if (uiState.employerCity.isNotBlank() && worker.city.equals(uiState.employerCity, ignoreCase = true)) return true
        if (uiState.employerArea.isNotBlank() && worker.area.equals(uiState.employerArea, ignoreCase = true)) return true
        return false
    }
}
