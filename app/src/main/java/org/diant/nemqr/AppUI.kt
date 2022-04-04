package org.diant.nemqr

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AppUI(private val context: Context, private val brain: ScanBrain) {

    var inputsLocked = false
    var resultRevealed = false

    private val activity = context as AppCompatActivity
    private val appSound = AppSound(context)

    private val scanlineIV: ImageView = activity.findViewById(R.id.scanline)
    private val scanlineLoop = Loop(4500) {
        if (scanlineIV.background == null)
            scanlineIV.setBackgroundResource(R.drawable.scanline_anim)
        (scanlineIV.background as AnimationDrawable).stop()
        (scanlineIV.background as AnimationDrawable).start()
        appSound.playScanline()
    }

    private val textBGIV: ImageView = activity.findViewById(R.id.text_bg)

    private val outRingIV: ImageView = activity.findViewById(R.id.circle_out)
    private val outRingResultIV: ImageView = activity.findViewById(R.id.circle_out_result)
    private val inRingIV: ImageView = activity.findViewById(R.id.circle_in)
    private val inRingResultIV: ImageView = activity.findViewById(R.id.circle_in_result)

    private val textAnalysisIV: ImageView = activity.findViewById(R.id.text_analysis)
    private val textSuccessIV: ImageView = activity.findViewById(R.id.text_success)
    private val textFailureIV: ImageView = activity.findViewById(R.id.text_failure)

    private val buttonStartIV: ImageView = activity.findViewById(R.id.button_start_iv)
    private val buttonResetIV: ImageView = activity.findViewById(R.id.button_reset_iv)

    var startButtonArmed = false
    private val buttonStartClickable: Button = activity.findViewById(R.id.button_start)
    var resetButtonArmed = false
    private val buttonResetClickable: Button = activity.findViewById(R.id.button_reset)

    fun onCreate() {
        appSound.onCreate()

        textBGIV.alpha = 0.0f

        outRingIV.scaleX = 0.0f
        outRingIV.scaleY = 0.0f
        outRingIV.setColorFilter(Color.rgb(0.0f, 0.7f, 0.9f))

        outRingResultIV.alpha = 0.0f

        inRingIV.scaleX = 0.0f
        inRingIV.scaleY = 0.0f
        inRingIV.setColorFilter(Color.rgb(0.0f, 0.7f, 0.9f))

        inRingResultIV.alpha = 0.0f

        textAnalysisIV.alpha = 0.0f
        textSuccessIV.alpha = 0.0f
        textFailureIV.alpha = 0.0f

        buttonStartClickable.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (!inputsLocked and !resultRevealed and brain.isResultOccupied()) {
                            startButtonArmed = true
                            buttonStartIV.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
                        } else {
                            appSound.playButton(false)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (startButtonArmed) {
                            buttonStartIV.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
                            if (brain.isResultOccupied()) {
                                inputsLocked = true
                                analysisAnim(brain.isResultCheckPassed())
                                appSound.playButton(true)
                            }
                            startButtonArmed = false
                        }
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })

        buttonResetClickable.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (!inputsLocked and (resultRevealed or brain.isResultOccupied())) {
                            resetButtonArmed = true
                            buttonResetIV.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
                        } else {
                            appSound.playButton(false)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (resetButtonArmed) {
                            buttonResetIV.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
                            if (brain.isResultOccupied()) {
                                inputsLocked = true
                                appSound.playButton(true)
                                brain.resetBrain()
                                resetResult()
                            }
                            resetButtonArmed = false
                        }
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })
    }

    fun onFocus(hasFocus: Boolean) {

        if (hasFocus) {
            scanlineLoop.start(2000)

            outRingIV.animation?.cancel()
            val outCircleLoop: Runnable = object : Runnable {
                override fun run() {
                    outRingIV.animate().rotationBy(360.0f).setDuration(3500).setInterpolator(LinearInterpolator())
                        .withEndAction(this).start()
                }
            }
            outCircleLoop.run()

            outRingResultIV.animation?.cancel()
            val outCircleResultLoop: Runnable = object : Runnable {
                override fun run() {
                    outRingResultIV.animate().rotationBy(360.0f).setDuration(3500).setInterpolator(LinearInterpolator())
                        .withEndAction(this).start()
                }
            }
            outCircleResultLoop.run()

        } else {
            scanlineLoop.stop()
        }
        appSound.onFocus(hasFocus)
    }

    fun resultReceived() {
        outRingStatus(true)
    }

    fun resetResult() {

        textSuccessIV.animation?.cancel()
        textFailureIV.animation?.cancel()

        switchOuterRing(false, Color.WHITE)

        textBGIV.animation?.cancel()
        textBGIV.animate().alpha(0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()

        appSound.stopResultSound()

        textSuccessIV.animate().alpha(0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction{
            inputsLocked = false
            resultRevealed = false
        }.start()
        textFailureIV.animate().alpha(0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        outRingStatus(false)
        inRingIV.animate().scaleX(0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        inRingIV.animate().scaleY(0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        inRingIV.animate().alpha(0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        inRingResultIV.animate().scaleX(0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        inRingResultIV.animate().scaleY(0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        inRingResultIV.animate().alpha(0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()

    }

    private fun switchOuterRing(result: Boolean, color: Int) {
        outRingIV.animate().alpha(if (result) 0.0f else 1.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        if (result)
            outRingResultIV.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        outRingResultIV.animate().alpha(if (result) 1.0f else 0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    private fun textBGPulse() {
        textBGIV.animate().alpha(0.6f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction{
            textBGIV.animate().alpha(0.3f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction{
                textBGPulse()
            }.start()
        }.start()
    }

    private fun outRingStatus(active: Boolean) {
        appSound.playResult(active)
        outRingIV.animation?.cancel()
        outRingIV.animate().scaleX(if (active) 1.0f else 0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        outRingIV.animate().scaleY(if (active) 1.0f else 0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        outRingResultIV.animation?.cancel()
        outRingResultIV.animate().scaleX(if (active) 1.0f else 0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        outRingResultIV.animate().scaleY(if (active) 1.0f else 0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    private fun analysisAnim(checkPassed: Boolean) {
        appSound.playAnalysis()
        outRingIV.animation?.cancel()
        outRingResultIV.animation?.cancel()
        inRingIV.animate().alpha(1.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        inRingIV.animate().scaleX(1.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        inRingIV.animate().scaleY(1.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        inRingIV.animate().rotationBy(-180.0f).setDuration(2000).setInterpolator(
            LinearInterpolator()
        ).withEndAction {
            showResult(checkPassed)
        }.start()
        inRingResultIV.animate().scaleX(1.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        inRingResultIV.animate().scaleY(1.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()

        textAnalysisIV.animation?.cancel()
        textAnalysisIV.animate().alpha(1.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction{
            textAnalysisIV.animate().alpha(0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).setStartDelay(750).start()
        }.start()
    }

    private fun showResult(checkPassed: Boolean) {
        appSound.playTextReveal()
        switchOuterRing(true, if (checkPassed) Color.GREEN else Color.RED)

        appSound.playResultSound(checkPassed)

        inRingIV.animate().alpha(0.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()
        inRingResultIV.setColorFilter(if (checkPassed) Color.GREEN else Color.RED, PorterDuff.Mode.MULTIPLY)
        inRingResultIV.animate().alpha(1.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).start()

        inRingResultIV.rotation = inRingIV.rotation

        textSuccessIV.animation?.cancel()
        textFailureIV.animation?.cancel()
        (if (checkPassed) textSuccessIV else textFailureIV).animate().alpha(1.0f).setDuration(500).setInterpolator(AccelerateDecelerateInterpolator()).withEndAction{
            inputsLocked = false
            resultRevealed = true
        }.start()

        textBGIV.setColorFilter(if (checkPassed) Color.GREEN else Color.RED, PorterDuff.Mode.MULTIPLY)

        textBGPulse()
    }

    class Loop(private val cycleLength: Long, private val onCycle: Runnable) {

        private val handler = Handler()
        var running = false

        private fun cycleWrapper() {
            if (!running) return
            onCycle.run()
            handler.postDelayed(::cycleWrapper, cycleLength)
        }

        fun start(initalDelay: Long = 0) {

            running = true

            if (initalDelay != 0L)
                handler.postDelayed(::cycleWrapper, initalDelay)
            else
                cycleWrapper()

        }

        fun stop() {
            handler.removeCallbacksAndMessages(null)
            running = false
        }

    }

}