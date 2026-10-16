package com.example.patients.ui.patients

import com.example.patients.data.model.Patient

// The only possible states of the screen
sealed interface PatientsUiState {
    data object Loading : PatientsUiState
    data class Success(val patients: List<Patient>) : PatientsUiState
    data class Error(val message: String) : PatientsUiState
}