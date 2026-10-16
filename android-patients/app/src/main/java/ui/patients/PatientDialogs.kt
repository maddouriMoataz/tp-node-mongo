package com.example.patients.ui.patients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.patients.data.model.Patient
import com.example.patients.data.model.PatientRequest

// One form for both cases: initial == null -> creation, initial != null -> edit
@Composable
fun PatientFormDialog(
    title: String,
    initial: Patient?,
    onDismiss: () -> Unit,
    onConfirm: (PatientRequest) -> Unit
) {
    var nom by rememberSaveable { mutableStateOf(initial?.nom ?: "") }
    var prenom by rememberSaveable { mutableStateOf(initial?.prenom ?: "") }
    var age by rememberSaveable { mutableStateOf(initial?.age?.toString() ?: "") }
    var telephone by rememberSaveable { mutableStateOf(initial?.telephone ?: "") }
    var medecin by rememberSaveable { mutableStateOf(initial?.medecin ?: "") }

    val formulaireValide = nom.isNotBlank() && prenom.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nom, onValueChange = { nom = it },
                    label = { Text("Nom *") }, singleLine = true
                )
                OutlinedTextField(
                    value = prenom, onValueChange = { prenom = it },
                    label = { Text("Prénom *") }, singleLine = true
                )
                OutlinedTextField(
                    value = age, onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text("Âge") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = telephone, onValueChange = { telephone = it },
                    label = { Text("Téléphone") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = medecin, onValueChange = { medecin = it },
                    label = { Text("Médecin") }, singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = formulaireValide,
                onClick = {
                    onConfirm(
                        PatientRequest(
                            nom = nom.trim(),
                            prenom = prenom.trim(),
                            age = age.toIntOrNull(),
                            telephone = telephone.trim().ifBlank { null },
                            medecin = medecin.trim().ifBlank { null }
                        )
                    )
                }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(patient: Patient, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Supprimer ce patient ?") },
        text = { Text("${patient.prenom} ${patient.nom} sera supprimé définitivement.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Supprimer", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}