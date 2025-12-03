package com.example.arproductdetector

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel to manage detected products state
 * Handles product detection, tracking, and duplicate prevention
 */
class ProductDetectionViewModel : ViewModel() {

    private val _detectedProducts = MutableStateFlow<List<DetectedProduct>>(emptyList())
    val detectedProducts: StateFlow<List<DetectedProduct>> = _detectedProducts.asStateFlow()

    private val _isDetecting = MutableStateFlow(false)
    val isDetecting: StateFlow<Boolean> = _isDetecting.asStateFlow()

    private val _detectionCount = MutableStateFlow(0)
    val detectionCount: StateFlow<Int> = _detectionCount.asStateFlow()

    /**
     * Add newly detected products, preventing duplicates
     */
    fun addDetectedProducts(newProducts: List<DetectedProduct>) {
        val currentProducts = _detectedProducts.value.toMutableList()
        var addedCount = 0

        newProducts.forEach { newProduct ->
            // Check if this product is already detected (duplicate check)
            val isDuplicate = currentProducts.any { existingProduct ->
                existingProduct.overlapsWith(newProduct, threshold = 0.5f)
            }

            if (!isDuplicate) {
                currentProducts.add(newProduct)
                addedCount++
            }
        }

        if (addedCount > 0) {
            _detectedProducts.value = currentProducts
            _detectionCount.value = currentProducts.size
        }
    }

    /**
     * Clear all detected products
     */
    fun clearDetections() {
        _detectedProducts.value = emptyList()
        _detectionCount.value = 0
    }

    /**
     * Set detection state
     */
    fun setDetecting(isDetecting: Boolean) {
        _isDetecting.value = isDetecting
    }

    /**
     * Remove products that are too old (optional - for memory management)
     */
    fun cleanupOldDetections(maxAgeMs: Long = 60000) { // 60 seconds
        val now = System.currentTimeMillis()
        val filtered = _detectedProducts.value.filter {
            now - it.timestamp < maxAgeMs
        }
        if (filtered.size != _detectedProducts.value.size) {
            _detectedProducts.value = filtered
            _detectionCount.value = filtered.size
        }
    }
}