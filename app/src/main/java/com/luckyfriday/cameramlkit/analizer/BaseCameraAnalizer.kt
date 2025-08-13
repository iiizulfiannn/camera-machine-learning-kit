package com.luckyfriday.cameramlkit.analizer

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.luckyfriday.cameramlkit.custom.CustomGraphicOverlay

abstract class BaseCameraAnalyzer <T: List<Face>> : ImageAnalysis.Analyzer {

    abstract val customGraphicOverlay: CustomGraphicOverlay<*>

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        mediaImage?.let {image ->
            // detect image
            detectInImage(InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees))
                .addOnSuccessListener { result ->
                    onSuccess(result, customGraphicOverlay, image.cropRect)
                    imageProxy.close()
                }
                .addOnFailureListener { e->
                    onFailure(e)
                    imageProxy.close()
                }
        }
    }
    abstract fun onStop()

    abstract fun onFailure(e: Exception)

    abstract fun onSuccess(
        results: List<Face>,
        graphicOverlay: CustomGraphicOverlay<*>,
        rect: Rect
    )

    protected abstract fun detectInImage(image: InputImage) : Task<T>
}