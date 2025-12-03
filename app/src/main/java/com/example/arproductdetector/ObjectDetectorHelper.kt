package com.example.arproductdetector

import android.content.Context
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

/**
 * Helper class for ML Kit Object Detection
 * Optimized for retail product detection
 */
class ObjectDetectorHelper(
    context: Context,
    private val onResults: (List<DetectedProduct>) -> Unit = {},
    private val onError: (Exception) -> Unit = {}
) {

    private val objectDetector: ObjectDetector

    init {
        // Configure object detector with optimized settings for products
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects() // Detect multiple products
            .enableClassification() // Enable object classification
            .build()

        objectDetector = ObjectDetection.getClient(options)
        Log.d(TAG, "ObjectDetector initialized")
    }

    /**
     * Process camera frame and detect objects
     */
    @androidx.camera.core.ExperimentalGetImage
    fun detectObjects(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        objectDetector.process(inputImage)
            .addOnSuccessListener { detectedObjects ->
                Log.d(TAG, "Detected ${detectedObjects.size} objects")
                val products = convertToDetectedProducts(detectedObjects)
                Log.d(TAG, "Converted to ${products.size} products")
                onResults(products)
                imageProxy.close()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Detection failed", exception)
                onError(exception)
                imageProxy.close()
            }
    }

    /**
     * Convert ML Kit detected objects to DetectedProduct
     * With improved filtering for retail products
     */
    private fun convertToDetectedProducts(
        objects: List<DetectedObject>
    ): List<DetectedProduct> {
        return objects.mapNotNull { obj ->
            val boundingBox = obj.boundingBox

            // Convert to RectF
            val rectF = RectF(
                boundingBox.left.toFloat(),
                boundingBox.top.toFloat(),
                boundingBox.right.toFloat(),
                boundingBox.bottom.toFloat()
            )

            // Get label and confidence
            val label = if (obj.labels.isNotEmpty()) {
                obj.labels[0].text
            } else {
                "Product"
            }

            val confidence = if (obj.labels.isNotEmpty()) {
                obj.labels[0].confidence
            } else {
                if (obj.trackingId != null) 0.7f else 0.5f
            }

            // Box dimensions
            val boxWidth = boundingBox.width()
            val boxHeight = boundingBox.height()

            // Lower threshold for better detection
            // Accept smaller boxes and lower confidence
            val isValidBox = boxWidth > 30 && boxHeight > 30
            val isValidConfidence = confidence >= 0.2f // Lowered from 0.3f

            if (isValidBox && isValidConfidence) {
                Log.d(TAG, "Valid product: $label, confidence: $confidence, box: ${boxWidth}x${boxHeight}")
                DetectedProduct(
                    id = DetectedProduct.generateId(rectF),
                    boundingBox = rectF,
                    confidence = confidence,
                    label = label
                )
            } else {
                Log.d(TAG, "Filtered out: box=${boxWidth}x${boxHeight}, conf=$confidence")
                null
            }
        }
    }

    /**
     * Close the detector when done
     */
    fun close() {
        objectDetector.close()
        Log.d(TAG, "ObjectDetector closed")
    }

    companion object {
        private const val TAG = "ObjectDetectorHelper"
    }
}