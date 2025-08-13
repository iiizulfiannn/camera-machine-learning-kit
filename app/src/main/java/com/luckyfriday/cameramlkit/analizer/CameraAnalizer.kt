package com.luckyfriday.cameramlkit.analizer

import android.graphics.Rect
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.luckyfriday.cameramlkit.custom.CustomGraphicOverlay
import com.luckyfriday.cameramlkit.custom.RectangleOverlay

class CameraAnalizer(private val overlay: CustomGraphicOverlay<*>) :
    BaseCameraAnalyzer<List<Face>>() {
    override val customGraphicOverlay: CustomGraphicOverlay<*>
        get() = overlay

    private val cameraOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.15f)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(cameraOptions)

    private var emotionListener: EmotionListener? = null

    fun setEmotionListener(listener: EmotionListener) {
        emotionListener = listener
    }

    override fun onStop() {
        try {
            detector.close()
        } catch (e: Exception) {
//            e.printStackTrace()
            Log.e("CAMERA ANALYZER", "ERROR :$e")
        }
    }

    override fun onFailure(e: Exception) {
        Log.e("CAMERA ANALYZER", "Failure :$e")
    }

    override fun onSuccess(
        results: List<Face>,
        graphicOverlay: CustomGraphicOverlay<*>,
        rect: Rect
    ) {
        graphicOverlay.clear()
        results.forEach {
            val faceGraphic = RectangleOverlay(graphicOverlay, it, rect)
            graphicOverlay.add(faceGraphic)
        }
        graphicOverlay.postInvalidate()
    }

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
            .addOnSuccessListener { faces ->
                val emotion = faces.firstOrNull()?.let {
                    //getEmotion
                    getEmotion(it)
                }
                if (emotion != null) {
                    emotionListener?.onEmotionDetected(emotion)
                }
                onSuccess(faces, overlay, Rect())
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun getEmotion(face: Face): String {
        val smiling = face.smilingProbability
        val rightEyeOpen = face.rightEyeOpenProbability
        val leftEyeOpen = face.leftEyeOpenProbability
        return when {
            smiling != null && smiling > 0.5 -> "Happy"
            leftEyeOpen != null && leftEyeOpen < 0.2 && rightEyeOpen != null && rightEyeOpen < 0.2 -> "Closed Eye"
            else -> "None"
        }

    }
}

interface EmotionListener {
    fun onEmotionDetected(emotion: String)
}
