package com.example.arproductdetector

import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

/**
 * Represents a detected product with its bounding box and detection metadata
 */
data class DetectedProduct(
    val id: String,
    val boundingBox: RectF,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val label: String = "Product"
) {
    /**
     * Convert Android RectF to Compose Rect
     */
    fun toComposeRect(): Rect {
        return Rect(
            left = boundingBox.left,
            top = boundingBox.top,
            right = boundingBox.right,
            bottom = boundingBox.bottom
        )
    }

    /**
     * Get center point of the bounding box
     */
    fun getCenterPoint(): Offset {
        return Offset(
            x = boundingBox.centerX(),
            y = boundingBox.centerY()
        )
    }

    /**
     * Get size of the bounding box
     */
    fun getSize(): Size {
        return Size(
            width = boundingBox.width(),
            height = boundingBox.height()
        )
    }

    /**
     * Check if this product overlaps significantly with another detected product
     * Used for duplicate detection
     */
    fun overlapsWith(other: DetectedProduct, threshold: Float = 0.5f): Boolean {
        val intersection = RectF()
        if (!intersection.setIntersect(this.boundingBox, other.boundingBox)) {
            return false
        }

        val intersectionArea = intersection.width() * intersection.height()
        val thisArea = boundingBox.width() * boundingBox.height()
        val otherArea = other.boundingBox.width() * other.boundingBox.height()

        val iou = intersectionArea / (thisArea + otherArea - intersectionArea)
        return iou >= threshold
    }

    companion object {
        /**
         * Generate a unique ID based on bounding box position
         */
        fun generateId(boundingBox: RectF): String {
            return "${boundingBox.centerX().toInt()}_${boundingBox.centerY().toInt()}_${boundingBox.width().toInt()}"
        }
    }
}