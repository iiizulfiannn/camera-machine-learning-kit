package com.luckyfriday.cameramlkit.custom

import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.CameraSelector
import kotlin.math.ceil

object CameraUtils {

    private var mScale: Float? = null
    private var mOffSetX: Float? = null
    private var mOffSetY: Float? = null
    private var cameraSelector: Int = CameraSelector.LENS_FACING_FRONT


    private fun isFrontMode() = cameraSelector == CameraSelector.LENS_FACING_FRONT

    fun calculateRect(
        overlay: CustomGraphicOverlay<*>,
        height: Float,
        width: Float,
        boundingBoxT: Rect
    ): RectF {


        fun landscapeMode(): Boolean {
            return overlay.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }

        fun whenLandscapeModeWidth(): Float {
            return when (landscapeMode()) {
                true -> width
                false -> height
            }
        }

        fun whenLandscapeModeHeight(): Float {
            return when (landscapeMode()) {
                true -> height
                false -> width
            }
        }

        val scaleX = overlay.width.toFloat() / whenLandscapeModeWidth()
        val scaleY = overlay.height.toFloat() / whenLandscapeModeHeight()
        val scale = scaleX.coerceAtLeast(scaleY)
        this.mScale = scale

        val offSetX = (overlay.width.toFloat() - ceil(whenLandscapeModeWidth() * scale)) / 2.0f
        val offSetY = (overlay.height.toFloat() - ceil(whenLandscapeModeHeight() * scale)) / 2.0f
        this.mOffSetX = offSetX
        this.mOffSetY = offSetY

        val mappedBox = RectF().apply {
            left = boundingBoxT.right * scale + offSetX
            right = boundingBoxT.left * scale + offSetX
            top = boundingBoxT.top * scale + offSetY
            bottom = boundingBoxT.bottom * scale + offSetY
        }

        if (isFrontMode()) {
            val centerX = overlay.width.toFloat() / 2
            mappedBox.apply {
                left = centerX + (centerX - left)
                right = centerX - (right - centerX)
            }
        }

        return mappedBox
    }

    fun toggleSelector() {
        cameraSelector =
            if (cameraSelector == CameraSelector.LENS_FACING_BACK)
                CameraSelector.LENS_FACING_FRONT
            else
                CameraSelector.LENS_FACING_BACK
    }

}