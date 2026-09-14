package com.lapoushko.feature_targets.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lapoushko.core_ui.components.FullScreenLoading
import com.lapoushko.core_ui.di.LocalViewModelFactory
import com.lapoushko.feature_targets.api.domain.Target

@Composable
fun TargetsRoute(
    viewModel: TargetsViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val uiState by viewModel.uiState.collectAsState()
    TargetsScreen(
        uiState = uiState,
        onInfiniteModeChange = viewModel::onInfiniteModeChange,
        onDurationChange = viewModel::onDurationChange,
        onStartMatch = viewModel::onStartMatch,
        onStopMatch = viewModel::onStopMatch
    )
}

@Composable
private fun TargetsScreen(
    uiState: TargetsUiState,
    onInfiniteModeChange: (Boolean) -> Unit,
    onDurationChange: (String) -> Unit,
    onStartMatch: () -> Unit,
    onStopMatch: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        MatchControls(uiState, onInfiniteModeChange, onDurationChange, onStartMatch, onStopMatch)

        if (uiState.isLoading) {
            FullScreenLoading(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.targets, key = { it.id }) { target ->
                    TargetRow(target)
                }
            }
        }
    }
}

@Composable
private fun MatchControls(
    uiState: TargetsUiState,
    onInfiniteModeChange: (Boolean) -> Unit,
    onDurationChange: (String) -> Unit,
    onStartMatch: () -> Unit,
    onStopMatch: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Бесконечный режим", modifier = Modifier.padding(end = 8.dp))
            Switch(checked = uiState.isInfiniteMode, onCheckedChange = onInfiniteModeChange)
        }
        if (!uiState.isInfiniteMode) {
            OutlinedTextField(
                value = uiState.durationInput,
                onValueChange = onDurationChange,
                label = { Text("Длительность, сек") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onStartMatch, modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text("Начать матч")
            }
            OutlinedButton(onClick = onStopMatch, modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text("Остановить")
            }
        }
        uiState.actionError?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun TargetRow(target: Target) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Мишень №${target.id}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (target.players.isEmpty()) "Участники не назначены" else target.players.keys.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Попадания: ${target.hitsCount}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Заряд: ${target.batteryCharge}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
