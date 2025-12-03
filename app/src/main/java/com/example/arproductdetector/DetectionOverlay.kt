package com.example.arproductdetector

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

/**
 * AR Overlay that draws bounding boxes and tick marks on detected products
 * FIXED VERSION with proper coordinate mapping
 */
@Composable
fun DetectionOverlay(
    detectedProducts: List<DetectedProduct>,
    previewView: PreviewView,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Canvas(modifier = modifier.fillMaxSize()) {
        if (detectedProducts.isEmpty()) return@Canvas

        // Get preview dimensions
        val viewWidth = size.width
        val viewHeight = size.height

        detectedProducts.forEach { product ->
            // Direct mapping - ML Kit already returns screen coordinates
            // We just need to ensure they're within bounds
            val rect = Rect(
                left = product.boundingBox.left.coerceIn(0f, viewWidth),
                top = product.boundingBox.top.coerceIn(0f, viewHeight),
                right = product.boundingBox.right.coerceIn(0f, viewWidth),
                bottom = product.boundingBox.bottom.coerceIn(0f, viewHeight)
            )

            // Only draw if the box is valid and visible
            if (rect.width > 10f && rect.height > 10f) {
                // Draw bounding box
                drawBoundingBox(rect)

                // Draw tick mark
                drawTickMark(rect)

                // Draw confidence label
                drawConfidenceLabel(rect, product.confidence)
            }
        }
    }
}

/**
 * Draw bounding box around detected product
 */
private fun DrawScope.drawBoundingBox(rect: Rect) {
    drawRect(
        color = Color(0xFF00FF00), // Bright green
        topLeft = Offset(rect.left, rect.top),
        size = Size(rect.width, rect.height),
        style = Stroke(width = 6f)
    )
}

/**
 * Draw green tick mark indicating detected product
 */
private fun DrawScope.drawTickMark(rect: Rect) {
    // Calculate tick size based on bounding box size
    val tickSize = min(rect.width, rect.height) * 0.4f
    val centerX = rect.left + rect.width / 2
    val centerY = rect.top + rect.height / 2

    // Circle radius
    val circleRadius = tickSize / 1.5f

    // Draw circle background
    drawCircle(
        color = Color(0xFF00FF00), // Bright green
        radius = circleRadius,
        center = Offset(centerX, centerY)
    )

    // Draw white tick/check mark
    val tickPath = Path().apply {
        val tickScale = tickSize / 2

        // Start point (left of tick)
        moveTo(centerX - tickScale * 0.5f, centerY)

        // Middle point (bottom of tick)
        lineTo(centerX - tickScale * 0.15f, centerY + tickScale * 0.5f)

        // End point (top right of tick)
        lineTo(centerX + tickScale * 0.6f, centerY - tickScale * 0.5f)
    }

    drawPath(
        path = tickPath,
        color = Color.White,
        style = Stroke(width = 8f)
    )
}

/**
 * Draw confidence score label
 */
private fun DrawScope.drawConfidenceLabel(rect: Rect, confidence: Float) {
    val confidenceText = "${(confidence * 100).toInt()}%"

    // Draw background rectangle for text
    val labelHeight = 40f
    val labelWidth = 80f
    val labelTop = max(0f, rect.top - labelHeight - 5f)

    drawRect(
        color = Color(0xFF00FF00).copy(alpha = 0.9f),
        topLeft = Offset(rect.left, labelTop),
        size = Size(labelWidth, labelHeight)
    )

    // Note: For actual text rendering, you would use drawText with TextPainter
    // This is simplified for the demo
}