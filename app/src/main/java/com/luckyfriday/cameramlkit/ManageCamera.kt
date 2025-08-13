package com.luckyfriday.cameramlkit

import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.luckyfriday.cameramlkit.analizer.CameraAnalyzer
import com.luckyfriday.cameramlkit.analizer.EmotionListener
import com.luckyfriday.cameramlkit.custom.CameraUtils
import com.luckyfriday.cameramlkit.custom.CustomGraphicOverlay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ManageCamera(
    private val context: Context,
    private val previewView: PreviewView,
    private val customGraphicOverlay: CustomGraphicOverlay<*>,
    private val emotionListener: EmotionListener,
    private val lifecycleOwner: LifecycleOwner
) {

    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var camera: Camera
    private lateinit var cameraAnalyzer: CameraAnalyzer
    private var cameraExecutorService: ExecutorService = Executors.newSingleThreadExecutor()

    fun cameraStart() {
        val cameraProcessProvider = ProcessCameraProvider.getInstance(context)
        cameraProcessProvider.addListener(
            {
                cameraProvider = cameraProcessProvider.get()
                preview = Preview.Builder().build()
                cameraAnalyzer.setEmotionListener(emotionListener)
                imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(cameraExecutorService, cameraAnalyzer) }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraOptions)
                    .build()

                // camera config
                setCameraConfiguration(cameraProvider, cameraSelector)
            }, ContextCompat.getMainExecutor(context)
        )
    }


    private fun setCameraConfiguration(
        cameraProvider: ProcessCameraProvider,
        cameraSelector: CameraSelector
    ) {

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector,
                preview, imageAnalysis
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
        } catch (e: Exception) {
            Log.e(TAG, "setCameraConfiguration: ${e.message}")
        }

    }


    fun changeCamera() {
        cameraStop()
        cameraOptions =
            if (cameraOptions == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        CameraUtils.toggleSelector()
        cameraStart()
    }

    fun cameraStop() {
        cameraProvider.unbindAll()
    }

    companion object {
        private const val TAG = "Camera Manager"
        var cameraOptions: Int = CameraSelector.LENS_FACING_FRONT

    }
}