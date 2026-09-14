package com.lapoushko.feature_connection.impl.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lapoushko.core_ui.di.LocalViewModelFactory
import com.lapoushko.feature_connection.api.domain.ConnectionState

@Composable
fun ConnectionRoute(
    onConnected: () -> Unit,
    viewModel: ConnectionViewModel = viewModel(factory = LocalViewModelFactory.current)
) {
    val uiState by viewModel.uiState.collectAsState()

    ConnectionScreen(
        uiState = uiState,
        onAddressChange = viewModel::onAddressChange,
        onConnectClick = viewModel::connect
    )

    LaunchedEffect(uiState.connectionState) {
        if (uiState.connectionState is ConnectionState.Connected) {
            onConnected()
        }
    }
}

@Composable
private fun ConnectionScreen(
    uiState: ConnectionUiState,
    onAddressChange: (String) -> Unit,
    onConnectClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            text = "Подключение к серверу «Лазерный биатлон»",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = uiState.addressInput,
            onValueChange = onAddressChange,
            label = { Text("Адрес сервера, например 192.168.1.10:8080") },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState.connectionState) {
            is ConnectionState.Connecting -> CircularProgressIndicator()
            is ConnectionState.Error -> Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error
            )
            else -> Unit
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onConnectClick,
            enabled = uiState.connectionState !is ConnectionState.Connecting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Подключиться")
        }
    }
}
