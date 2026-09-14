package com.lapoushko.feature_targets.impl

import com.lapoushko.feature_targets.api.domain.Target
import com.lapoushko.feature_targets.api.domain.usecase.ObserveTargetsUseCase
import com.lapoushko.feature_targets.api.domain.usecase.StartMatchUseCase
import com.lapoushko.feature_targets.api.domain.usecase.StopMatchUseCase
import com.lapoushko.feature_targets.impl.presentation.TargetsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TargetsViewModelTest {

    private val observeTargets: ObserveTargetsUseCase = mockk()
    private val startMatch: StartMatchUseCase = mockk(relaxed = true)
    private val stopMatch: StopMatchUseCase = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ui state exposes targets`() {
        val targets = listOf(Target(id = 1, batteryCharge = 100, hitsCount = 3, players = mapOf("Иванов" to 3)))
        every { observeTargets() } returns flowOf(targets)

        val viewModel = TargetsViewModel(observeTargets, startMatch, stopMatch)

        assertEquals(targets, viewModel.uiState.value.targets)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `start match delegates to use case with current settings`() = runTest {
        every { observeTargets() } returns flowOf(emptyList())
        coEvery { startMatch(false, 90) } returns Result.success(Unit)
        val viewModel = TargetsViewModel(observeTargets, startMatch, stopMatch)

        viewModel.onInfiniteModeChange(false)
        viewModel.onDurationChange("90")
        viewModel.onStartMatch()

        coVerify(exactly = 1) { startMatch(false, 90) }
    }

    @Test
    fun `stop match delegates to use case`() = runTest {
        every { observeTargets() } returns flowOf(emptyList())
        coEvery { stopMatch(0) } returns Result.success(Unit)
        val viewModel = TargetsViewModel(observeTargets, startMatch, stopMatch)

        viewModel.onStopMatch()

        coVerify(exactly = 1) { stopMatch(0) }
    }
}
