package com.example.mad_project.ui.alerts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mad_project.ui.components.AppTopBar
import com.example.mad_project.ui.components.EmptyView
import com.example.mad_project.ui.theme.MADProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryAlertsScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Expiry Alerts",
                showBack = true,
                onBackClick = onBackClick
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            EmptyView("No expiry alerts")
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun ExpiryAlertsScreenPreview() {
    MADProjectTheme(dynamicColour = false) {
        ExpiryAlertsScreen(onBackClick = {})
    }
}
