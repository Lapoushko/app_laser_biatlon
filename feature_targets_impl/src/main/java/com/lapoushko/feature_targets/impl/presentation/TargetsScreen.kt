package com.lapoushko.feature_targets.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lapoushko.core_ui.components.FullScreenLoading
import com.lapoushko.core_ui.di.LocalViewModelFactory
import com.lapoushko.feature_targets.api.domain.Target
import com.lapoushko.feature_targets.api.domain.TargetHitSeries

@Composable
fun TargetsRoute(
    viewModel: TargetsViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val uiState by viewModel.uiState.collectAsState()
    TargetsScreen(uiState = uiState)
}

@Composable
private fun TargetsScreen(uiState: TargetsUiState) {
    when {
        uiState.isLoading -> FullScreenLoading(modifier = Modifier.fillMaxSize())
        uiState.targets.isEmpty() -> EmptyTargetsMessage(modifier = Modifier.fillMaxSize())
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.targets, key = { it.id }) { target ->
                TargetCard(target)
            }
        }
    }
}

@Composable
private fun EmptyTargetsMessage(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "Мишени появятся здесь после начала матча",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TargetCard(target: Target) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Мишень №${target.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (target.players.isEmpty()) "Участники не назначены" else target.players.keys.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "${target.hitsCount} попаданий", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Заряд: ${target.batteryCharge}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (target.hitSeries.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "Серии попаданий",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    target.hitSeries.forEach { series -> HitSeriesRow(series) }
                }
            }
        }
    }
}

@Composable
private fun HitSeriesRow(series: TargetHitSeries) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = series.shooterName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        HitZonesIndicator(series.pattern)
        Text(
            text = "${series.hitsCount}/5",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Пять зон мишени: закрашенный кружок — зона поражена ('0' в паттерне), кольцо — промах ('1').
 * Рисуем сами вместо растровых картинок из shootingimages — те не масштабируются под ширину
 * карточки и теряются на тёмной теме (чёрные кружки на тёмном фоне).
 */
@Composable
private fun HitZonesIndicator(pattern: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        pattern.forEach { zone ->
            val isHit = zone == '0'
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .then(
                        if (isHit) {
                            Modifier.background(MaterialTheme.colorScheme.primary)
                        } else {
                            Modifier.border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        }
                    )
            )
        }
    }
}
