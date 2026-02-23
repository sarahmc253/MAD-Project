package com.example.mad_project.ui.live

import com.example.mad_project.ui.components.AppTopBar
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mad_project.ui.theme.MADProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveExpiryStatusPanelScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = { AppTopBar(title = "Live Expiry Status", showBack = true, onBackClick = onBackClick) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text("Streaming Status: Simulated")
            Text("Last Update: --")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LiveExpiryStatusPanelScreenPreview() {
    MADProjectTheme(dynamicColour = false) {
        LiveExpiryStatusPanelScreen(onBackClick = {})
    }
}