package com.example.mad_project.ui.components

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.tooling.preview.Preview
import com.example.mad_project.ui.theme.MADProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    showBack: Boolean = false,
    onBackClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = { onBackClick?.invoke() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarPreview() {
    MADProjectTheme(dynamicColour = false) {
        AppTopBar(title = "Screen Title")
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTopBarWithBackPreview() {
    MADProjectTheme(dynamicColour = false) {
        AppTopBar(title = "Detail", showBack = true, onBackClick = {})
    }
}

