# AR Product Detector

An Android application that uses real-time camera feed and ML Kit Object Detection to identify and mark products on retail shelves with augmented reality overlays.

## Overview

This app provides a live camera scanning experience where products are detected, marked with visual indicators (bounding boxes and tick marks), and tracked to prevent duplicate detections as users pan across shelves.

## Features

- **Real-time Product Detection**: Uses ML Kit Object Detection to identify products in live camera feed
- **AR Overlay Visualization**: Displays green bounding boxes and tick marks directly on detected products
- **Duplicate Prevention**: Tracks already-detected products using overlap detection to avoid re-marking
- **Continuous Scanning**: Seamlessly detects new products as the camera moves across shelf sections
- **Detection Counter**: Shows the total number of unique products detected
- **Clear Functionality**: Reset all detections with a single button

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Camera**: CameraX API
- **Object Detection**: Google ML Kit Vision API
- **Permissions**: Accompanist Permissions library

## Project Structure

```
com.example.arproductdetector/
├── MainActivity.kt                  # Entry point of the application
├── CameraScreen.kt                  # Main camera UI and permissions handling
├── DetectionOverlay.kt              # AR overlay rendering (bounding boxes + tick marks)
├── ObjectDetectorHelper.kt          # ML Kit object detection wrapper
├── ProductDetectionViewModel.kt     # State management and business logic
└── DetectedProduct.kt               # Data model for detected products
```

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24 (Marshmallow) or higher
- Device or emulator with camera support

### Dependencies
Add these dependencies to your `build.gradle.kts` (Module: app):

```kotlin
dependencies {
    // CameraX
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    
    // ML Kit Object Detection
    implementation("com.google.mlkit:object-detection:17.0.1")
    
    // Accompanist Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
}
```

### Permissions
Add to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
```

## How It Works

### Detection Flow
1. **Camera Setup**: CameraX initializes with Preview and ImageAnalysis use cases
2. **Frame Processing**: Each camera frame is passed to ML Kit Object Detector
3. **Object Detection**: ML Kit identifies objects and returns bounding boxes with confidence scores
4. **Filtering**: Results are filtered based on minimum size (30x30px) and confidence (≥0.2)
5. **Duplicate Check**: New detections are compared with existing ones using IoU overlap detection
6. **AR Rendering**: Valid, non-duplicate products are rendered with bounding boxes and tick marks
7. **State Update**: ViewModel updates detection count and product list



**No detections appearing:**
- Ensure good lighting conditions
- Try different distances from products
- Check if objects are clearly visible and not too small
- Verify ML Kit dependencies are properly installed

**Duplicate detections:**
- Adjust IoU threshold in ViewModel
- Increase overlap detection sensitivity
---

