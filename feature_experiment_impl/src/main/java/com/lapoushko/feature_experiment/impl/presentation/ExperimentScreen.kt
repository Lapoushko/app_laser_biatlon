package com.lapoushko.feature_experiment.impl.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lapoushko.core_ui.di.LocalViewModelFactory
import com.lapoushko.feature_experiment.api.domain.ExperimentSegment
import com.lapoushko.feature_experiment.api.domain.ExperimentSession
import com.lapoushko.feature_experiment.api.domain.ExperimentSummary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExperimentRoute(
    viewModel: ExperimentViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.onLocationPermissionResult(granted) }

    LaunchedEffect(Unit) {
        viewModel.onLocationPermissionResult(hasLocationPermission(context))
    }

    ExperimentScreen(
        uiState = uiState,
        onTitleChange = viewModel::onTitleChange,
        onCheckpointInputChange = viewModel::onCheckpointInputChange,
        onStartClick = viewModel::startSession,
        onStopClick = viewModel::stopSession,
        onMarkCheckpointClick = viewModel::markCheckpoint,
        onSessionClick = viewModel::selectSession,
        onDeleteSessionClick = viewModel::deleteSession,
        onRequestPermissionClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
        onShareClick = { session, summary -> shareSummary(context, session, summary) }
    )
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

@Composable
private fun ExperimentScreen(
    uiState: ExperimentUiState,
    onTitleChange: (String) -> Unit,
    onCheckpointInputChange: (String) -> Unit,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onMarkCheckpointClick: () -> Unit,
    onSessionClick: (ExperimentSession) -> Unit,
    onDeleteSessionClick: (ExperimentSession) -> Unit,
    onRequestPermissionClick: () -> Unit,
    onShareClick: (ExperimentSession, ExperimentSummary) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Эксперимент: устойчивость связи",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
        if (!uiState.hasLocationPermission) {
            item {
                PermissionBanner(
                    onRequestPermissionClick = onRequestPermissionClick,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
        item {
            if (uiState.isRunning) {
                LiveDashboardCard(
                    uiState = uiState,
                    onCheckpointInputChange = onCheckpointInputChange,
                    onMarkCheckpointClick = onMarkCheckpointClick,
                    onStopClick = onStopClick,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                )
            } else {
                StartSessionCard(
                    titleInput = uiState.titleInput,
                    onTitleChange = onTitleChange,
                    onStartClick = onStartClick,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
        uiState.selectedSession?.let { session ->
            item {
                SummaryCard(
                    session = session,
                    summary = uiState.summary,
                    onShareClick = { onShareClick(session, uiState.summary) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
        if (uiState.sessions.isNotEmpty()) {
            item {
                Text(
                    text = "История замеров",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(uiState.sessions, key = { it.id }) { session ->
                SessionRow(
                    session = session,
                    selected = session.id == uiState.selectedSessionId,
                    onClick = { onSessionClick(session) },
                    onDeleteClick = { onDeleteSessionClick(session) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PermissionBanner(onRequestPermissionClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Нет доступа к уровню Wi-Fi сигнала",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "Android требует разрешение на геолокацию, чтобы читать RSSI антенны. " +
                    "Без него эксперимент по-прежнему считает доставку пакетов, но без силы сигнала.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = onRequestPermissionClick) {
                Text("Предоставить доступ")
            }
        }
    }
}

@Composable
private fun StartSessionCard(
    titleInput: String,
    onTitleChange: (String) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Новый замер", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = "Начните замер, идите с винтовкой от точки доступа и ставьте метки " +
                    "(расстояние, место, уровень помехи) — по ним разобьётся статистика.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = titleInput,
                onValueChange = onTitleChange,
                label = { Text("Название замера") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = onStartClick, modifier = Modifier.fillMaxWidth()) {
                Text("Начать замер")
            }
        }
    }
}

@Composable
private fun LiveDashboardCard(
    uiState: ExperimentUiState,
    onCheckpointInputChange: (String) -> Unit,
    onMarkCheckpointClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session = uiState.selectedSession
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = session?.title ?: "Эксперимент",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    session?.let {
                        val elapsedSeconds = (System.currentTimeMillis() - it.startedAtEpochMillis) / 1000
                        Text(
                            text = "Идёт: $elapsedSeconds с",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = uiState.currentRssiDbm?.let { "$it дБм" } ?: "RSSI: —",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DashboardMetric(label = "Пингов", value = "${uiState.summary.totalPings}")
                DashboardMetric(label = "Успешно", value = "${uiState.summary.totalSuccess}")
                DashboardMetric(label = "Потери", value = "${uiState.summary.overallLossPercent}%")
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.checkpointInput,
                    onValueChange = onCheckpointInputChange,
                    label = { Text("Метка, например «5 м»") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = onMarkCheckpointClick, enabled = uiState.checkpointInput.isNotBlank()) {
                    Text("Отметить")
                }
            }
            OutlinedButton(onClick = onStopClick, modifier = Modifier.fillMaxWidth()) {
                Text("Завершить замер")
            }
        }
    }
}

@Composable
private fun DashboardMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SummaryCard(
    session: ExperimentSession,
    summary: ExperimentSummary,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Итоги: ${session.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (summary.totalPings > 0) {
                    TextButton(onClick = onShareClick) { Text("Поделиться") }
                }
            }
            if (summary.segments.isEmpty()) {
                Text(
                    text = "Пока нет данных — подождите первые пинги или поставьте метку",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                summary.segments.forEach { segment -> SegmentRow(segment) }
            }
        }
    }
}

@Composable
private fun SegmentRow(segment: ExperimentSegment) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = segment.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = "${segment.successCount}/${segment.pingCount}", style = MaterialTheme.typography.bodyMedium)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "Потери: ${segment.lossPercent}%",
                style = MaterialTheme.typography.bodySmall,
                color = if (segment.lossPercent > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "RTT: ${segment.averageRttMillis ?: "—"} мс",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = segment.averageRssiDbm?.let { "RSSI: ${segment.minRssiDbm}…${segment.maxRssiDbm}" } ?: "RSSI: —",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

private val sessionDateFormat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

@Composable
private fun SessionRow(
    session: ExperimentSession,
    selected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = session.title, style = MaterialTheme.typography.bodyMedium)
                val statusText = if (session.isRunning) "идёт" else "завершён"
                Text(
                    text = "${sessionDateFormat.format(session.startedAtEpochMillis)} · $statusText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onDeleteClick) {
                Text("Удалить")
            }
        }
    }
}

private fun shareSummary(context: Context, session: ExperimentSession, summary: ExperimentSummary) {
    val report = buildString {
        appendLine("Эксперимент: ${session.title}")
        appendLine("Всего пингов: ${summary.totalPings}, потери: ${summary.overallLossPercent}%")
        appendLine()
        summary.segments.forEach { segment ->
            appendLine(segment.label)
            appendLine("  доставлено: ${segment.successCount}/${segment.pingCount} (потери ${segment.lossPercent}%)")
            appendLine("  RTT ср.: ${segment.averageRttMillis ?: "—"} мс")
            if (segment.averageRssiDbm != null) {
                appendLine("  RSSI: от ${segment.minRssiDbm} до ${segment.maxRssiDbm} дБм (ср. ${segment.averageRssiDbm})")
            }
            appendLine()
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Результаты эксперимента: ${session.title}")
        putExtra(Intent.EXTRA_TEXT, report)
    }
    context.startActivity(Intent.createChooser(intent, "Поделиться результатами"))
}
