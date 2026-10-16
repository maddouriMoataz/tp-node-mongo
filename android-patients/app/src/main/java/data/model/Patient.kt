package com.example.patients.data.model

import kotlinx.serialization.Serializable

// Patient as returned by the API (GET)
@Serializable
data class Patient(
    val id: String,
    val nom: String,
    val prenom: String,
    val age: Int? = null,
    val telephone: String? = null,
    val medecin: String? = null,
    val statut: String = "Actif"
)

// Data sent to create or update a patient (POST / PUT): no id
@Serializable
data class PatientRequest(
    val nom: String,
    val prenom: String,
    val age: Int? = null,
    val telephone: String? = null,
    val medecin: String? = null
)

// Error body returned by the API: { "message": "...", "erreurs": [...] }
@Serializable
data class ApiError(
    val message: String,
    val erreurs: List<String> = emptyList()
)