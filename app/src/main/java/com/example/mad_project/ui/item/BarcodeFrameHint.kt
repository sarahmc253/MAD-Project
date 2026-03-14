package com.example.mad_project.ui.item

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mad_project.ui.theme.ShelfScanGreen

@Composable
internal fun BarcodeFrameHint() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .height(160.dp)
                .background(Color.Transparent)
                .border(4.dp, ShelfScanGreen, RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Surface(shape = RoundedCornerShape(20.dp), color = Color.DarkGray.copy(alpha = 0.7f)) {
            Text(
                "Align barcode within the frame",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}
