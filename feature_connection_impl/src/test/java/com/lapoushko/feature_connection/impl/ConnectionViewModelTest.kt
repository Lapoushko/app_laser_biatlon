package com.lapoushko.feature_connection.impl

import com.lapoushko.feature_connection.api.domain.ConnectionState
import com.lapoushko.feature_connection.api.domain.usecase.ConnectToServerUseCase
import com.lapoushko.feature_connection.api.domain.usecase.DisconnectFromServerUseCase
import com.lapoushko.feature_connection.api.domain.usecase.ObserveConnectionStateUseCase
import com.lapoushko.feature_connection.impl.presentation.ConnectionViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionViewModelTest {

    private val connectToServer: ConnectToServerUseCase = mockk(relaxed = true)
    private val disconnectFromServer: DisconnectFromServerUseCase = mockk(relaxed = true)
    private val observeConnectionState: ObserveConnectionStateUseCase = mockk()
    private val connectionStateFlow = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    private lateinit var viewModel: ConnectionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { observeConnectionState() } returns connectionStateFlow
        viewModel = ConnectionViewModel(connectToServer, disconnectFromServer, observeConnectionState)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onAddressChange updates ui state`() {
        viewModel.onAddressChange("192.168.1.10:8080")

        assertEquals("192.168.1.10:8080", viewModel.uiState.value.addressInput)
    }

    @Test
    fun `connect with blank address does not call use case`() = runTest {
        viewModel.onAddressChange("   ")

        viewModel.connect()

        coVerify(exactly = 0) { connectToServer(any()) }
    }

    @Test
    fun `connect with valid address calls use case`() = runTest {
        coEvery { connectToServer(any()) } returns Result.success(Unit)
        viewModel.onAddressChange("192.168.1.10:8080")

        viewModel.connect()

        coVerify(exactly = 1) { connectToServer("192.168.1.10:8080") }
    }

    @Test
    fun `disconnect calls use case`() = runTest {
        viewModel.disconnect()

        coVerify(exactly = 1) { disconnectFromServer() }
    }
}
