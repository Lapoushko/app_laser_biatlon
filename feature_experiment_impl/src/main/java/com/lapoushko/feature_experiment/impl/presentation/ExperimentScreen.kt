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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lapoushko.core_ui.di.LocalViewModelFactory
import com.lapoushko.feature_experiment.api.domain.ExperimentEvent
import com.lapoushko.feature_experiment.api.domain.ExperimentSegment
import com.lapoushko.feature_experiment.api.domain.ExperimentSession
import com.lapoushko.feature_experiment.api.domain.ExperimentSummary
import com.lapoushko.feature_experiment.impl.data.buildExperimentCsv
import com.lapoushko.feature_experiment.impl.data.buildExperimentXlsx
import java.text.SimpleDateFormat
import java.util.Locale

private const val XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

private val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

@Composable
fun ExperimentRoute(
    viewModel: ExperimentViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> viewModel.onLocationPermissionResult(granted) }

    var pendingCsvContent by remember { mutableStateOf<String?>(null) }
    val saveCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val content = pendingCsvContent
        pendingCsvContent = null
        if (uri != null && content != null) {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                // Excel определяет кодировку CSV по BOM — без него UTF-8 с кириллицей
                // открывается как ANSI/CP1251 и превращается в кракозябры.
                stream.write(UTF8_BOM)
                stream.write(content.toByteArray(Charsets.UTF_8))
            }
        }
    }

    var pendingXlsxContent by remember { mutableStateOf<ByteArray?>(null) }
    val saveXlsxLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(XLSX_MIME_TYPE)
    ) { uri ->
        val content = pendingXlsxContent
        pendingXlsxContent = null
        if (uri != null && content != null) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(content) }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onLocationPermissionResult(hasLocationPermission(context))
    }

    ExperimentScreen(
        uiState = uiState,
        onTitleChange = viewModel::onTitleChange,
        onCheckpointInputChange = viewModel::onCheckpointInputChange,
        onDistanceInputChange = viewModel::onDistanceInputChange,
        onStartClick = viewModel::startSession,
        onStopClick = viewModel::stopSession,
        onMarkCheckpointClick = viewModel::markCheckpoint,
        onSessionClick = viewModel::selectSession,
        onDeleteSessionClick = viewModel::deleteSession,
        onRequestPermissionClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
        onShareClick = { session, summary -> shareSummary(context, session, summary) },
        onExportCsvClick = { session, events, summary ->
            pendingCsvContent = buildExperimentCsv(session, events, summary)
            saveCsvLauncher.launch("experiment_${session.id}.csv")
        },
        onExportXlsxClick = { session, events, summary ->
            pendingXlsxContent = buildExperimentXlsx(session, events, summary)
            saveXlsxLauncher.launch("experiment_${session.id}.xlsx")
        }
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
    onDistanceInputChange: (String) -> Unit,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onMarkCheckpointClick: () -> Unit,
    onSessionClick: (ExperimentSession) -> Unit,
    onDeleteSessionClick: (ExperimentSession) -> Unit,
    onRequestPermissionClick: () -> Unit,
    onShareClick: (ExperimentSession, ExperimentSummary) -> Unit,
    onExportCsvClick: (ExperimentSession, List<ExperimentEvent>, ExperimentSummary) -> Unit,
    onExportXlsxClick: (ExperimentSession, List<ExperimentEvent>, ExperimentSummary) -> Unit
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
                    onDistanceInputChange = onDistanceInputChange,
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
                    hasEvents = uiState.events.isNotEmpty(),
                    onShareClick = { onShareClick(session, uiState.summary) },
                    onExportCsvClick = { onExportCsvClick(session, uiState.events, uiState.summary) },
                    onExportXlsxClick = { onExportXlsxClick(session, uiState.events, uiState.summary) },
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
    onDistanceInputChange: (String) -> Unit,
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
                    label = { Text("Метка, например «за углом»") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.distanceInput,
                    onValueChange = onDistanceInputChange,
                    label = { Text("м") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(80.dp)
                )
            }
            Text(
                text = "Расстояние нужно для модели затухания сигнала — можно оставить пустым, если это не про дистанцию (например, метка про глушение помехой)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onMarkCheckpointClick,
                enabled = uiState.checkpointInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отметить")
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
    hasEvents: Boolean,
    onShareClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onExportXlsxClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Итоги: ${session.title}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                if (hasEvents) {
                    TextButton(onClick = onExportCsvClick) { Text("CSV") }
                    TextButton(onClick = onExportXlsxClick) { Text("XLSX с графиками") }
                }
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
                AnalyticsBlock(summary)
            }
        }
    }
}

@Composable
private fun AnalyticsBlock(summary: ExperimentSummary) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Расчёты",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Корреляция RSSI↔RTT: ${summary.correlation.rssiVsRtt.formatCorrelation()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Корреляция RSSI↔потери: ${summary.correlation.rssiVsLoss.formatCorrelation()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val model = summary.pathLossModel
        Text(
            text = if (model != null) {
                "Модель затухания: n=${model.pathLossExponent.format2()}, " +
                    "RSSI(1м)=${model.rssiAt1mDbm.format1()} дБм, " +
                    "R²=${model.rSquared.format2()} (${model.pointCount} точек)"
            } else {
                "Модель затухания: недостаточно меток с расстоянием (нужно ≥2)"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Double?.formatCorrelation(): String = this?.format2() ?: "недостаточно данных"
private fun Double.format2(): String = String.format(Locale.US, "%.2f", this)
private fun Double.format1(): String = String.format(Locale.US, "%.1f", this)

@Composable
private fun SegmentRow(segment: ExperimentSegment) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = segment.distanceMeters?.let { "${segment.label} (${it.format1()} м)" } ?: segment.label,
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
            segment.distanceMeters?.let { distanceMeters ->
                appendLine("  расстояние: ${distanceMeters.format1()} м")
            }
            appendLine()
        }
        appendLine("Корреляция RSSI↔RTT: ${summary.correlation.rssiVsRtt.formatCorrelation()}")
        appendLine("Корреляция RSSI↔потери: ${summary.correlation.rssiVsLoss.formatCorrelation()}")
        summary.pathLossModel?.let { model ->
            appendLine(
                "Модель затухания: n=${model.pathLossExponent.format2()}, " +
                    "RSSI(1м)=${model.rssiAt1mDbm.format1()} дБм, R²=${model.rSquared.format2()}"
            )
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Результаты эксперимента: ${session.title}")
        putExtra(Intent.EXTRA_TEXT, report)
    }
    context.startActivity(Intent.createChooser(intent, "Поделиться результатами"))
}
