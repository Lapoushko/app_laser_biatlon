package com.lapoushko.feature_experiment.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lapoushko.feature_experiment.api.domain.ExperimentEvent
import com.lapoushko.feature_experiment.api.domain.ExperimentSession
import com.lapoushko.feature_experiment.api.domain.ExperimentSummary
import com.lapoushko.feature_experiment.api.domain.usecase.DeleteExperimentSessionUseCase
import com.lapoushko.feature_experiment.api.domain.usecase.EndExperimentSessionUseCase
import com.lapoushko.feature_experiment.api.domain.usecase.ObserveExperimentEventsUseCase
import com.lapoushko.feature_experiment.api.domain.usecase.ObserveExperimentSessionsUseCase
import com.lapoushko.feature_experiment.api.domain.usecase.ObserveExperimentSummaryUseCase
import com.lapoushko.feature_experiment.api.domain.usecase.RecordCheckpointUseCase
import com.lapoushko.feature_experiment.api.domain.usecase.RunExperimentPingLoopUseCase
import com.lapoushko.feature_experiment.api.domain.usecase.StartExperimentSessionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ExperimentViewModel @Inject constructor(
    private val startExperimentSession: StartExperimentSessionUseCase,
    private val endExperimentSession: EndExperimentSessionUseCase,
    private val deleteExperimentSession: DeleteExperimentSessionUseCase,
    private val recordExperimentCheckpoint: RecordCheckpointUseCase,
    observeExperimentSessions: ObserveExperimentSessionsUseCase,
    observeExperimentEvents: ObserveExperimentEventsUseCase,
    observeExperimentSummary: ObserveExperimentSummaryUseCase,
    private val runExperimentPingLoop: RunExperimentPingLoopUseCase
) : ViewModel() {

    private val activeSessionId = MutableStateFlow<Long?>(null)
    private val selectedSessionId = MutableStateFlow<Long?>(null)
    private val titleInput = MutableStateFlow("")
    private val checkpointInput = MutableStateFlow("")
    private val hasLocationPermission = MutableStateFlow(false)

    private var pingJob: Job? = null

    private val eventsFlow: Flow<List<ExperimentEvent>> = selectedSessionId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else observeExperimentEvents(id)
    }
    private val summaryFlow: Flow<ExperimentSummary> = selectedSessionId.flatMapLatest { id ->
        if (id == null) flowOf(ExperimentSummary(emptyList(), 0, 0)) else observeExperimentSummary(id)
    }

    private val sessionsState = combine(
        observeExperimentSessions(),
        activeSessionId,
        selectedSessionId
    ) { sessions, activeId, selectedId -> Triple(sessions, activeId, selectedId) }

    private val detailState = combine(eventsFlow, summaryFlow) { events, summary -> events to summary }

    private val inputState = combine(
        titleInput,
        checkpointInput,
        hasLocationPermission
    ) { title, checkpoint, permission -> Triple(title, checkpoint, permission) }

    val uiState: StateFlow<ExperimentUiState> = combine(
        sessionsState,
        detailState,
        inputState
    ) { (sessions, activeId, selectedId), (events, summary), (title, checkpoint, permission) ->
        ExperimentUiState(
            sessions = sessions,
            activeSessionId = activeId,
            selectedSessionId = selectedId,
            events = events,
            summary = summary,
            titleInput = title,
            checkpointInput = checkpoint,
            hasLocationPermission = permission
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExperimentUiState()
    )

    fun onTitleChange(value: String) {
        titleInput.value = value
    }

    fun onCheckpointInputChange(value: String) {
        checkpointInput.value = value
    }

    fun onLocationPermissionResult(granted: Boolean) {
        hasLocationPermission.value = granted
    }

    fun startSession() {
        if (activeSessionId.value != null) return
        val title = titleInput.value.trim().ifBlank { defaultSessionTitle() }
        viewModelScope.launch {
            val id = startExperimentSession(title)
            activeSessionId.value = id
            selectedSessionId.value = id
            titleInput.value = ""
            pingJob?.cancel()
            pingJob = viewModelScope.launch { runExperimentPingLoop(id).collect {} }
        }
    }

    fun stopSession() {
        val id = activeSessionId.value ?: return
        pingJob?.cancel()
        pingJob = null
        viewModelScope.launch {
            endExperimentSession(id)
            activeSessionId.value = null
        }
    }

    fun markCheckpoint() {
        val id = activeSessionId.value ?: return
        val label = checkpointInput.value.trim()
        if (label.isEmpty()) return
        viewModelScope.launch {
            recordExperimentCheckpoint(id, label)
            checkpointInput.value = ""
        }
    }

    fun selectSession(session: ExperimentSession) {
        selectedSessionId.value = session.id
    }

    fun deleteSession(session: ExperimentSession) {
        viewModelScope.launch {
            if (activeSessionId.value == session.id) {
                pingJob?.cancel()
                pingJob = null
                activeSessionId.value = null
            }
            deleteExperimentSession(session.id)
            if (selectedSessionId.value == session.id) {
                selectedSessionId.value = null
            }
        }
    }

    private fun defaultSessionTitle(): String =
        "Замер " + SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date())
}
