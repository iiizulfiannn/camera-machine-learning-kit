package com.luckyfriday.cameramlkit

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.luckyfriday.cameramlkit.analizer.CameraAnalyzer
import com.luckyfriday.cameramlkit.analizer.EmotionListener
import com.luckyfriday.cameramlkit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), EmotionListener {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var manageCamera: ManageCamera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        manageCamera = ManageCamera(
            this,
            mainBinding.viewCamera,
            mainBinding.customOverlay,
            this,
            this
        )
        val cameraAnalyzer = CameraAnalyzer(mainBinding.customOverlay)
        cameraAnalyzer.setEmotionListener(this)
        askCameraPermission()

        mainBinding.buttonStartCamera.setOnClickListener {
            manageCamera.cameraStart()
            buttonManage(true)
        }
        mainBinding.buttonStopCamera.setOnClickListener {
            manageCamera.cameraStop()
            buttonManage(false)
        }
        mainBinding.buttonTurnCamera.setOnClickListener {
            manageCamera.changeCamera()
        }

    }

    private fun askCameraPermission() {
        if (arrayOf(android.Manifest.permission.CAMERA).all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            manageCamera.cameraStart()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            manageCamera.cameraStart()
        } else {
            Toast.makeText(this, "CAMERA PERMISSION DENIED!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buttonManage(isStart: Boolean) {
        if (isStart) {
            mainBinding.buttonStopCamera.visibility = View.VISIBLE
            mainBinding.buttonStartCamera.visibility = View.GONE
        } else {
            mainBinding.buttonStopCamera.visibility = View.GONE
            mainBinding.buttonStartCamera.visibility = View.VISIBLE
        }
    }

    private val emotionStickers = mapOf(
        "Happy" to R.drawable.ic_happy,
        "Closed Eye" to R.drawable.ic_closed_eyes,
        "None" to R.drawable.ic_nope,
    )

    override fun onEmotionDetected(emotion: String) {
        runOnUiThread {
            val stickerResources = emotionStickers[emotion]
            if (stickerResources != null) {
                mainBinding.ivEmotion.setImageResource(stickerResources)
                mainBinding.ivEmotion.visibility = View.VISIBLE
            } else {
                mainBinding.ivEmotion.visibility = View.GONE
            }
        }
    }
}