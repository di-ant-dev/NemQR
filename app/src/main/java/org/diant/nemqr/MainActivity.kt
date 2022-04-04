package org.diant.nemqr

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var systemUI: SystemUI
    private lateinit var appUI: AppUI
    private lateinit var cameraScanner: CameraScanner
    private val brain = ScanBrain()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        systemUI = SystemUI(window)
        systemUI.onCreate()

        setContentView(R.layout.activity_main)

        appUI = AppUI(this, brain)
        appUI.onCreate()

        cameraScanner = CameraScanner(this) {
            if (it != null) {
                if (brain.tryOccupyResult(it)) {
                    appUI.resultReceived()
                }
            }
        }
        cameraScanner.onCreate() {
            val camPermission: Int = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            if (camPermission == PackageManager.PERMISSION_GRANTED) {
                cameraScanner.attachPreview()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 101)
            }
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        systemUI.onFocusChanged(hasFocus)
        appUI.onFocus(hasFocus)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Camera needed for this app to function", Toast.LENGTH_SHORT).show()
                else cameraScanner.attachPreview()
            }
        }
    }

}