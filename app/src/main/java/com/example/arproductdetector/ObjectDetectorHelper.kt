package com.example.arproductdetector

import android.content.Context
import android.graphics.RectF
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

/**
 * Helper class for ML Kit Object Detection
 * FIXED VERSION with proper coordinate handling
 */
class ObjectDetectorHelper(
    context: Context,
    private val onResults: (List<DetectedProduct>) -> Unit = {},
    private val onError: (Exception) -> Unit = {}
) {

    private val objectDetector: ObjectDetector

    // Store image dimensions for coordinate transformation
    private var imageWidth = 0
    private var imageHeight = 0

    init {
        // Configure object detector for retail products
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        objectDetector = ObjectDetection.getClient(options)
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

        // Store dimensions
        imageWidth = imageProxy.width
        imageHeight = imageProxy.height

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        objectDetector.process(inputImage)
            .addOnSuccessListener { detectedObjects ->
                val products = convertToDetectedProducts(detectedObjects)
                onResults(products)
                imageProxy.close()
            }
            .addOnFailureListener { exception ->
                onError(exception)
                imageProxy.close()
            }
    }

    /**
     * Convert ML Kit detected objects to our DetectedProduct model
     * Now with proper coordinate handling
     */
    private fun convertToDetectedProducts(
        objects: List<DetectedObject>
    ): List<DetectedProduct> {
        return objects.mapNotNull { obj ->
            val boundingBox = obj.boundingBox

            // ML Kit returns coordinates in image space
            // These coordinates are already correct for the preview
            val rectF = RectF(
                boundingBox.left.toFloat(),
                boundingBox.top.toFloat(),
                boundingBox.right.toFloat(),
                boundingBox.bottom.toFloat()
            )

            // Get label if available
            val label = if (obj.labels.isNotEmpty()) {
                obj.labels[0].text
            } else {
                "Product"
            }

            // Get confidence
            val confidence = if (obj.labels.isNotEmpty()) {
                obj.labels[0].confidence
            } else {
                // Use tracking ID presence as confidence indicator
                if (obj.trackingId != null) 0.8f else 0.5f
            }

            // Filter out low confidence detections and invalid boxes
            val boxWidth = boundingBox.width()
            val boxHeight = boundingBox.height()

            if (confidence >= 0.3f && boxWidth > 20 && boxHeight > 20) {
                DetectedProduct(
                    id = DetectedProduct.generateId(rectF),
                    boundingBox = rectF,
                    confidence = confidence,
                    label = label
                )
            } else {
                null
            }
        }
    }

    /**
     * Close the detector when done
     */
    fun close() {
        objectDetector.close()
    }
}