@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.patients.ui.patients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patients.data.model.Patient

// ---------------------------------------------------------------
// Main screen: observes the state, shows one display per state
// and sends events to the ViewModel.
// ---------------------------------------------------------------
@Composable
fun PatientsScreen(
    viewModel: PatientViewModel = viewModel(factory = PatientViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Local UI state (kept on rotation). We store only ids and look the patient up in the list.
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }

    val patients = (uiState as? PatientsUiState.Success)?.patients.orEmpty()

    // Shows each message sent by the ViewModel
    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patients", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.loadPatients() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nouveau patient") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is PatientsUiState.Loading -> LoadingContent(Modifier.align(Alignment.Center))

                is PatientsUiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadPatients() },
                    modifier = Modifier.align(Alignment.Center)
                )

                is PatientsUiState.Success -> PatientsContent(
                    patients = state.patients,
                    query = query,
                    onQueryChange = { query = it },
                    onEdit = { editingId = it.id },
                    onDelete = { deletingId = it.id }
                )
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        PatientFormDialog(
            title = "Nouveau patient",
            initial = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { request ->
                viewModel.addPatient(request)
                showAddDialog = false
            }
        )
    }

    // Edit dialog (same form, pre-filled)
    patients.firstOrNull { it.id == editingId }?.let { patient ->
        PatientFormDialog(
            title = "Modifier le patient",
            initial = patient,
            onDismiss = { editingId = null },
            onConfirm = { request ->
                viewModel.updatePatient(patient.id, request)
                editingId = null
            }
        )
    }

    // Delete confirmation
    patients.firstOrNull { it.id == deletingId }?.let { patient ->
        ConfirmDeleteDialog(
            patient = patient,
            onDismiss = { deletingId = null },
            onConfirm = {
                viewModel.deletePatient(patient)
                deletingId = null
            }
        )
    }
}

// ---------------------------------------------------------------
// Success state: search, then the list
// ---------------------------------------------------------------
@Composable
fun PatientsContent(
    patients: List<Patient>,
    query: String,
    onQueryChange: (String) -> Unit,
    onEdit: (Patient) -> Unit,
    onDelete: (Patient) -> Unit
) {
    // Filtering is done on the screen: it only changes what is displayed, not the data
    val filtered = patients.filter { p ->
        val text = "${p.prenom} ${p.nom} ${p.medecin.orEmpty()}"
        text.contains(query.trim(), ignoreCase = true)
    }

    LazyColumn(
        // big bottom padding so the floating button never hides the last card
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SearchField(query, onQueryChange) }

        if (filtered.isEmpty()) {
            item { EmptyState(hasPatients = patients.isNotEmpty()) }
        } else {
            items(filtered, key = { it.id }) { patient ->
                PatientCard(
                    patient = patient,
                    onEdit = { onEdit(patient) },
                    onDelete = { onDelete(patient) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------
// The 3 other states
// ---------------------------------------------------------------
@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("Chargement…", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Oups, un problème", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}

@Composable
fun EmptyState(hasPatients: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            if (hasPatients) "Aucun résultat" else "Aucun patient",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (hasPatients) "Essayez un autre nom."
            else "Appuyez sur « Nouveau patient » pour en ajouter un.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}