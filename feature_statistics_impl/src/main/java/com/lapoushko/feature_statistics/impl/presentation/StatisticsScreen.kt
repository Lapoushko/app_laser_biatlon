package com.lapoushko.feature_statistics.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lapoushko.core_ui.components.FullScreenLoading
import com.lapoushko.core_ui.components.StaleDataBanner
import com.lapoushko.core_ui.di.LocalViewModelFactory
import com.lapoushko.feature_statistics.api.domain.GameMode
import com.lapoushko.feature_statistics.api.domain.Participant

@Composable
fun StatisticsRoute(
    viewModel: StatisticsViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val uiState by viewModel.uiState.collectAsState()
    StatisticsScreen(uiState = uiState)
}

@Composable
private fun StatisticsScreen(uiState: StatisticsUiState) {
    when (uiState) {
        is StatisticsUiState.Loading -> FullScreenLoading(modifier = Modifier.fillMaxSize())
        is StatisticsUiState.NotStarted -> Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "Матч ещё не начат — статистика появится после старта",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        is StatisticsUiState.Content -> Column(modifier = Modifier.fillMaxSize()) {
            if (uiState.statistics.isStale) {
                StaleDataBanner()
            }
            val modeLabel = if (uiState.statistics.gameMode == GameMode.TEAM) "командный" else "личный"
            val timeLabel = if (uiState.statistics.isInfiniteMode) {
                "без ограничения по времени"
            } else {
                "осталось ${uiState.statistics.liveRemainingSeconds} с из ${uiState.statistics.durationSeconds} с"
            }
            Text(
                text = "Режим: $modeLabel · $timeLabel",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.statistics.participants, key = { it.riflemanId }) { participant ->
                    ParticipantRow(participant)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(participant: Participant) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = participant.fullName, style = MaterialTheme.typography.bodyLarge)
                val targetsLabel = participant.targetIds.joinToString(", ")
                Text(
                    text = "Мишени: $targetsLabel" + (participant.teamName?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Попадания: ${participant.hits}/${participant.shots}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Заряд: ${participant.batteryCharge} · Патроны: ${participant.roundsLeft}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
