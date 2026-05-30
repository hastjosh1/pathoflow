package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Random

@Composable
fun OfflineUpiQrCode(
    upiId: String,
    amount: Double,
    patientName: String,
    modifier: Modifier = Modifier
) {
    CardContainer(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Accurate Lab UPI Payment QR",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Render QR Matrix
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawQrCodeVisualMatrix(upiId, amount)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Payable Amount: ₹$amount",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "UPI: $upiId",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Ref: $patientName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun CardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(4.dp)
    ) {
        content()
    }
}

// Procedurally draws finder targets and QR data grids
private fun DrawScope.drawQrCodeVisualMatrix(upiId: String, amount: Double) {
    val sizePx = size.width
    val colCount = 21 // Version 1 QR code size
    val cellSize = sizePx / colCount
    
    // Draw 3 Finder Patterns at corners
    drawFinderPattern(0f, 0f, cellSize)
    drawFinderPattern((colCount - 7) * cellSize, 0f, cellSize)
    drawFinderPattern(0f, (colCount - 7) * cellSize, cellSize)
    
    // Use a deterministic seeds from upiId + amount to draw data bits
    val seed = (upiId.hashCode().toLong() + amount.toLong()).absoluteValue
    val rng = Random(seed)
    
    for (row in 0 until colCount) {
        for (col in 0 until colCount) {
            // Skip corners where finders are constructed
            if ((row < 8 && col < 8) || (row < 8 && col >= colCount - 8) || (row >= colCount - 8 && col < 8)) {
                continue
            }
            
            // Procedurally paint data bits
            if (rng.nextBoolean()) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(col * cellSize, row * cellSize),
                    size = Size(cellSize + 0.5f, cellSize + 0.5f) // overlapping fraction avoids pixel line gaps
                )
            }
        }
    }
}

private fun DrawScope.drawFinderPattern(x: Float, y: Float, cellSize: Float) {
    val block = cellSize
    // Outer 7x7 block
    drawRect(
        color = Color.Black,
        topLeft = Offset(x, y),
        size = Size(block * 7, block * 7)
    )
    // Inner 5x5 white
    drawRect(
        color = Color.White,
        topLeft = Offset(x + block, y + block),
        size = Size(block * 5, block * 5)
    )
    // Middle 3x3 black
    drawRect(
        color = Color.Black,
        topLeft = Offset(x + block * 2, y + block * 2),
        size = Size(block * 3, block * 3)
    )
}

private val Long.absoluteValue: Long get() = if (this < 0) -this else this
