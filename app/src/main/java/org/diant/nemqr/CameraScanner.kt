package org.diant.nemqr

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class CameraScanner(private val context: Context, private val qrCallback: (String?) -> Unit) {

    private val previewView = (context as AppCompatActivity).findViewById<PreviewView>(R.id.camera_preview)
    private val camExecutor = Executors.newSingleThreadExecutor()
    private lateinit var camProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var camProvider: ProcessCameraProvider

    fun onCreate(onFutureReturned: () -> Unit) {

        camProviderFuture =  ProcessCameraProvider.getInstance(context)

        camProviderFuture.addListener({
            Log.i("nmqr", "cam provider future returned")
            camProvider = camProviderFuture.get()
            onFutureReturned()
        }, ContextCompat.getMainExecutor(context))

    }

    fun attachPreview() {
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val camSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val imgCapture = ImageCapture.Builder().build()
        val imgAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(Size(1280, 720))
            .build()
        imgAnalysis.setAnalyzer(camExecutor, analyzer)
        camProvider.bindToLifecycle(context as AppCompatActivity, camSelector, preview, imgCapture, imgAnalysis)

    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun decodeImage(imageProxy: ImageProxy) {

        val image = imageProxy.image!!
        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        val scanOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val scanner = BarcodeScanning.getClient(scanOptions)

        scanner.process(inputImage).addOnSuccessListener {
            for (c in it) {
                when (c.valueType) {
                    Barcode.TYPE_TEXT -> qrCallback(c.rawValue)
                }
            }
            imageProxy.close()
        }.addOnFailureListener {
            Log.e("nmqr", "${it.message}")
        }

    }

    private val analyzer = Analyzer(::decodeImage)

    class Analyzer(private val callback: (ImageProxy) -> Unit) : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            callback(imageProxy)
        }
    }

}