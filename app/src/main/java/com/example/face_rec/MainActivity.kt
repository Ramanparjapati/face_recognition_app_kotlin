package com.example.face_rec

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Button
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService


class MainActivity : AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private lateinit var viewFinder: PreviewView
    private lateinit var captureButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ... rest of onCreate() remains the same ...
    }

    private fun startCamera() {
        // ... rest of startCamera() remains the same ...
    }

    private fun takePhoto() {
        // TODO: Implement takePhoto() functionality
    }

    private fun registerFace() {
        // TODO: Implement registerFace() functionality
    }

    private inner class FaceAnalyzer : ImageAnalysis.Analyzer {
        private val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()
        )

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage, imageProxy.imageInfo.rotationDegrees)

                detector.process(image)
                    .addOnSuccessListener { faces ->
                        for (face in faces) {
                            // Get face features
                            val bounds = face.boundingBox
                            val rotY = face.headEulerAngleY // Head is rotated right
                            val rotZ = face.headEulerAngleZ // Head is tilted sideways

                            // If landmark detection was enabled
                            if (face.rightEyeOpenProbability != null) {
                                val rightEyeOpenProb = face.rightEyeOpenProbability
                            }
                            if (face.leftEyeOpenProbability != null) {
                                val leftEyeOpenProb = face.leftEyeOpenProbability
                            }

                            // If classification was enabled
                            if (face.smilingProbability != null) {
                                val smileProb = face.smilingProbability
                            }

                            // Store face data in Firebase
                            storeFaceData(face)
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle any errors
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
    }

    private fun storeFaceData(face: com.google.mlkit.vision.face.Face) {
        val database = FirebaseDatabase.getInstance().reference
        val storage = FirebaseStorage.getInstance().reference

        // Create a unique ID for the face
        val faceId = database.child("faces").push().key

        // Create face data object
        val faceData = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "leftEyeOpenProb" to (face.leftEyeOpenProbability ?: 0f),
            "rightEyeOpenProb" to (face.rightEyeOpenProbability ?: 0f),
            "smileProb" to (face.smilingProbability ?: 0f),
            "rotationY" to face.headEulerAngleY,
            "rotationZ" to face.headEulerAngleZ
        )

        // Store data in Firebase Realtime Database
        if (faceId != null) {
            database.child("faces").child(faceId).setValue(faceData)
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) ==
                PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}