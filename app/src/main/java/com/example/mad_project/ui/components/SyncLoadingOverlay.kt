package com.example.mad_project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.example.mad_project.ui.theme.ShelfScanGreen

/**
 * Full-screen overlay shown while a Firebase write is in progress.
 * Dims the screen and displays a green spinner in the centre.
 * All touch events are consumed so the user cannot trigger further actions
 * until the sync completes.
 */
@Composable
fun SyncLoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = ShelfScanGreen)
    }
}
