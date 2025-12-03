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
import kotlin.math.min


@Composable
fun DetectionOverlay(
    detectedProducts: List<DetectedProduct>,
    previewView: PreviewView,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (detectedProducts.isEmpty()) return@Canvas

        val viewWidth = size.width
        val viewHeight = size.height

        detectedProducts.forEach { product ->
            val rect = Rect(
                left = product.boundingBox.left.coerceIn(0f, viewWidth),
                top = product.boundingBox.top.coerceIn(0f, viewHeight),
                right = product.boundingBox.right.coerceIn(0f, viewWidth),
                bottom = product.boundingBox.bottom.coerceIn(0f, viewHeight)
            )

            if (rect.width > 10f && rect.height > 10f) {
                drawBoundingBoxWithShadow(rect)

                drawProminentTickMark(rect)
            }
        }
    }
}

private fun DrawScope.drawBoundingBoxWithShadow(rect: Rect) {
    drawRect(
        color = Color.Black.copy(alpha = 0.3f),
        topLeft = Offset(rect.left + 4f, rect.top + 4f),
        size = Size(rect.width, rect.height),
        style = Stroke(width = 8f)
    )

    drawRect(
        color = Color(0xFF00FF00), // Bright green
        topLeft = Offset(rect.left, rect.top),
        size = Size(rect.width, rect.height),
        style = Stroke(width = 8f)
    )

    // Inner lighter green border
    drawRect(
        color = Color(0xFF80FF80).copy(alpha = 0.5f),
        topLeft = Offset(rect.left + 4f, rect.top + 4f),
        size = Size(rect.width - 8f, rect.height - 8f),
        style = Stroke(width = 4f)
    )
}


private fun DrawScope.drawProminentTickMark(rect: Rect) {
    val centerX = rect.left + rect.width / 2
    val centerY = rect.top + rect.height / 2

    val baseSize = min(rect.width, rect.height) * 0.35f
    val tickSize = baseSize.coerceAtLeast(60f) // Minimum 60px
    val circleRadius = tickSize / 1.5f

    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = circleRadius,
        center = Offset(centerX + 3f, centerY + 3f)
    )

    drawCircle(
        color = Color(0xFF00FF00), // Bright green
        radius = circleRadius,
        center = Offset(centerX, centerY)
    )

    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = circleRadius * 0.8f,
        center = Offset(centerX, centerY)
    )

    val tickPath = Path().apply {
        val scale = tickSize / 2.5f

        moveTo(centerX - scale * 0.6f, centerY - scale * 0.1f)

        lineTo(centerX - scale * 0.2f, centerY + scale * 0.6f)

        lineTo(centerX + scale * 0.7f, centerY - scale * 0.7f)
    }

    val tickPathShadow = Path().apply {
        val scale = tickSize / 2.5f
        moveTo(centerX - scale * 0.6f + 2f, centerY - scale * 0.1f + 2f)
        lineTo(centerX - scale * 0.2f + 2f, centerY + scale * 0.6f + 2f)
        lineTo(centerX + scale * 0.7f + 2f, centerY - scale * 0.7f + 2f)
    }

    drawPath(
        path = tickPathShadow,
        color = Color.Black.copy(alpha = 0.3f),
        style = Stroke(width = 12f)
    )

    drawPath(
        path = tickPath,
        color = Color.White,
        style = Stroke(
            width = 10f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round
        )
    )
}