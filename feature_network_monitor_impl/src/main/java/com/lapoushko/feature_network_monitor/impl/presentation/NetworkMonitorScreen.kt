package com.lapoushko.feature_network_monitor.impl.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lapoushko.core_ui.di.LocalViewModelFactory
import com.lapoushko.feature_network_monitor.api.domain.ConnectionQuality
import com.lapoushko.feature_network_monitor.api.domain.ConnectionStatus
import com.lapoushko.feature_network_monitor.api.domain.LatencyPoint
import com.lapoushko.feature_network_monitor.api.domain.NetworkLogEntry
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NetworkMonitorRoute(
    viewModel: NetworkMonitorViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val uiState by viewModel.uiState.collectAsState()
    NetworkMonitorScreen(
        uiState = uiState,
        onLogClick = viewModel::toggleExpanded,
        onClearClick = viewModel::clear
    )
}

@Composable
private fun NetworkMonitorScreen(
    uiState: NetworkMonitorUiState,
    onLogClick: (Long) -> Unit,
    onClearClick: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Сетевой трафик", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onClearClick) {
                    Text("Очистить")
                }
            }
            ConnectionQualityCard(
                quality = uiState.connectionQuality,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            )
            if (uiState.logs.isEmpty()) {
                Text(
                    text = "Запросов пока не было",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        items(uiState.logs, key = { it.id }) { log ->
            NetworkLogRow(
                log = log,
                expanded = log.id == uiState.expandedLogId,
                onClick = { onLogClick(log.id) }
            )
            HorizontalDivider()
        }
    }
}

private data class StatusPresentation(val label: String, val color: Color)

@Composable
private fun statusPresentation(status: ConnectionStatus): StatusPresentation = when (status) {
    ConnectionStatus.GOOD -> StatusPresentation("Соединение стабильно", Color(0xFF2E7D32))
    ConnectionStatus.DEGRADED -> StatusPresentation("Есть проблемы со связью", Color(0xFFF9A825))
    ConnectionStatus.LOST -> StatusPresentation("Связь потеряна", MaterialTheme.colorScheme.error)
    ConnectionStatus.UNKNOWN -> StatusPresentation("Нет данных о связи", MaterialTheme.colorScheme.outline)
}

@Composable
private fun ConnectionQualityCard(quality: ConnectionQuality, modifier: Modifier = Modifier) {
    val presentation = statusPresentation(quality.status)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(presentation.color)
                )
                Text(
                    text = presentation.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QualityMetric(label = "Задержка", value = quality.averageLatencyMillis?.let { "$it мс" } ?: "—")
                QualityMetric(label = "Потери", value = "${quality.lossPercent}%")
                QualityMetric(label = "Выборка", value = "${quality.sampleSize} зап.")
            }
            if (quality.latencySeries.size >= 2) {
                LatencyChart(
                    points = quality.latencySeries,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                )
            }
        }
    }
}

@Composable
private fun QualityMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Мини-график задержки последних запросов: линия по [LatencyPoint.durationMillis], сбои — красными точками. */
@Composable
private fun LatencyChart(points: List<LatencyPoint>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val baselineColor = MaterialTheme.colorScheme.outlineVariant
    val maxDuration = (points.maxOfOrNull { it.durationMillis } ?: 0L).coerceAtLeast(1L).toFloat()

    Canvas(modifier = modifier) {
        val stepX = if (points.size > 1) size.width / (points.size - 1) else 0f
        val strokeWidth = 2.dp.toPx()

        drawLine(
            color = baselineColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )

        val path = Path()
        val offsets = points.mapIndexed { index, point ->
            val x = stepX * index
            val ratio = (point.durationMillis / maxDuration).coerceIn(0f, 1f)
            val y = size.height - ratio * size.height
            Offset(x, y)
        }
        offsets.forEachIndexed { index, offset ->
            if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = strokeWidth))

        points.forEachIndexed { index, point ->
            if (point.isError) {
                drawCircle(color = errorColor, radius = 3.dp.toPx(), center = offsets[index])
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

@Composable
private fun NetworkLogRow(log: NetworkLogEntry, expanded: Boolean, onClick: () -> Unit) {
    val statusColor = if (log.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "${log.method} ${log.url}", style = MaterialTheme.typography.bodyMedium)
                Text(text = timeFormat.format(log.timestampEpochMillis), style = MaterialTheme.typography.bodySmall)
            }
            val statusText = log.errorMessage ?: log.responseCode?.toString() ?: "…"
            Text(
                text = "Статус: $statusText · ${log.durationMillis} мс",
                style = MaterialTheme.typography.bodySmall,
                color = statusColor
            )
            if (expanded) {
                Text(text = "Заголовки запроса:\n${log.requestHeaders}", style = detailStyle())
                if (log.requestBody != null) {
                    Text(text = "Тело запроса:\n${log.requestBody}", style = detailStyle())
                }
                if (log.responseHeaders != null) {
                    Text(text = "Заголовки ответа:\n${log.responseHeaders}", style = detailStyle())
                }
                if (log.responseBody != null) {
                    Text(text = "Тело ответа:\n${log.responseBody}", style = detailStyle())
                }
            }
        }
    }
}

@Composable
private fun detailStyle() = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
