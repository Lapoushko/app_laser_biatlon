package com.lapoushko.feature_targets.impl

import com.lapoushko.feature_targets.api.domain.Target
import com.lapoushko.feature_targets.api.domain.usecase.ObserveTargetsUseCase
import com.lapoushko.feature_targets.impl.presentation.TargetsViewModel
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

        val viewModel = TargetsViewModel(observeTargets)

        assertEquals(targets, viewModel.uiState.value.targets)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loading stops even when match has not started yet`() = runTest {
        // До старта матча /get_match_data не отдаёт мишеней — репозиторий эмитит пустой список,
        // а не молчит, иначе экран навсегда останется в состоянии загрузки.
        every { observeTargets() } returns flowOf(emptyList())

        val viewModel = TargetsViewModel(observeTargets)

        assertEquals(emptyList<Target>(), viewModel.uiState.value.targets)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }
}
