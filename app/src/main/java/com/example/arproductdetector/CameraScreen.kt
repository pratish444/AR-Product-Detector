package com.example.arproductdetector

import android.Manifest
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(viewModel: ProductDetectionViewModel) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            cameraPermissionState.status.isGranted -> {
                CameraPreviewWithDetection(viewModel)
            }
            else -> {
                PermissionRequestScreen(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Camera permission is required for product detection")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithDetection(viewModel: ProductDetectionViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val detectedProducts by viewModel.detectedProducts.collectAsState()
    val detectionCount by viewModel.detectionCount.collectAsState()

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // Keep reference to detector
    var detector by remember { mutableStateOf<ObjectDetectorHelper?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            detector?.close()
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    previewView = this
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER

                    // Setup camera
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()

                            // Preview use case
                            val preview = Preview.Builder()
                                .build()
                                .also {
                                    it.setSurfaceProvider(surfaceProvider)
                                }

                            // Image Analysis for detection
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                                .build()

                            // Create detector
                            detector = ObjectDetectorHelper(
                                context = ctx,
                                onResults = { products ->
                                    if (products.isNotEmpty()) {
                                        viewModel.addDetectedProducts(products)
                                    }
                                },
                                onError = { exception ->
                                    Log.e("CameraScreen", "Detection failed", exception)
                                }
                            )

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                detector?.detectObjects(imageProxy)
                            }

                            // Camera selector - back camera
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            // Unbind all before rebinding
                            cameraProvider.unbindAll()

                            // Bind use cases to camera
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )

                            Log.d("CameraScreen", "Camera setup successful")

                        } catch (e: Exception) {
                            Log.e("CameraScreen", "Camera binding failed", e)
                        }

                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // AR Overlay for detected products
        previewView?.let { preview ->
            DetectionOverlay(
                detectedProducts = detectedProducts,
                previewView = preview,
                modifier = Modifier.fillMaxSize()
            )
        }

        // UI Controls - Detection Count Card
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Products Detected: $detectionCount",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Clear button at bottom
        Button(
            onClick = { viewModel.clearDetections() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Clear", style = MaterialTheme.typography.labelLarge)
        }
    }
}