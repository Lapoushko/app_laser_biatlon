package com.lapoushko.feature_statistics.impl

import com.lapoushko.feature_statistics.api.domain.GameMode
import com.lapoushko.feature_statistics.api.domain.MatchStatistics
import com.lapoushko.feature_statistics.api.domain.Participant
import com.lapoushko.feature_statistics.api.domain.usecase.ObserveMatchStatisticsUseCase
import com.lapoushko.feature_statistics.impl.presentation.StatisticsUiState
import com.lapoushko.feature_statistics.impl.presentation.StatisticsViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private val observeMatchStatistics: ObserveMatchStatisticsUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ui state exposes latest statistics as content`() = runTest {
        val statistics = MatchStatistics(
            gameMode = GameMode.SINGLE,
            isInfiniteMode = true,
            durationSeconds = 0,
            remainingSeconds = 0,
            startTimeIso = "2026-01-01T00:00:00.000",
            participants = listOf(
                Participant(
                    riflemanId = "1",
                    fullName = "Иванов",
                    teamName = null,
                    targetIds = listOf(1),
                    batteryCharge = 100,
                    magazinesLeft = 5,
                    roundsLeft = 5,
                    shots = 5,
                    hits = 5,
                    targetHitPatterns = mapOf(1 to "11111")
                )
            ),
            targets = emptyList()
        )
        every { observeMatchStatistics() } returns flowOf(statistics)
        val viewModel = StatisticsViewModel(observeMatchStatistics)

        val collectorJob = launch { viewModel.uiState.collect {} }
        runCurrent()

        assertEquals(StatisticsUiState.Content(statistics), viewModel.uiState.value)
        collectorJob.cancel()
    }
}
