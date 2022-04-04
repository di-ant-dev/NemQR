package org.diant.nemqr

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class AppSound(private val context: Context) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build())
        .build()

    private val idSoundBackLoop = pool.load(context, R.raw.nemesis_scan_background_loop, 1)
    private val idSoundButton = pool.load(context, R.raw.nemesis_scan_button, 1)
    private val idSoundButtonInactive = pool.load(context, R.raw.nemesis_scan_button_inactive, 1)
    private val idSoundScreenGlare = pool.load(context, R.raw.nemesis_scan_display_glare, 1)
    private val idSoundAnalysis = pool.load(context, R.raw.nemesis_scan_scan, 1)
    private val idSoundGrab = pool.load(context, R.raw.nemesis_scan_qr_target_in, 1)
    private val idSoundUngrab = pool.load(context, R.raw.nemesis_scan_qr_target_out, 1)
    private val idSoundTextReveal = pool.load(context, R.raw.nemesis_scan_text_in, 1)
    private val idSoundCheckFail = pool.load(context, R.raw.nemesis_scan_alarm, 1)
    private val idSoundCheckPass = pool.load(context, R.raw.nemesis_scan_success, 1)

    fun onCreate() {

        pool.setOnLoadCompleteListener { pool, id, status ->
            if (status == 0) {
                when (id) {
                    idSoundBackLoop -> pool.play(id, 1.0f, 1.0f, 0, -1, 1.0f)
                }
            }
        }
    }

    fun onFocus(hasFocus: Boolean) {
        if (hasFocus) {
            pool.autoResume()
        } else {
            pool.autoPause()
        }
    }

    fun playScanline() {
        pool.play(idSoundScreenGlare, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    fun playResult(grabbed: Boolean) {
        pool.play(if (grabbed) idSoundGrab else idSoundUngrab, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    fun playButton(active: Boolean) {
        pool.play(if (active) idSoundButton else idSoundButtonInactive, 1.0f, 1.0f, 0, 0, 1.0f)
    }


    var resultHandle: Int = -1
    fun playResultSound(check: Boolean) {
        resultHandle = pool.play(if (check) idSoundCheckPass else idSoundCheckFail, 1.0f, 1.0f, 0, if (check) 0 else -1, 1.0f)
    }

    fun stopResultSound() {
        pool.stop(resultHandle)
    }

    fun playTextReveal() {
        pool.play(idSoundTextReveal, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    fun playAnalysis() {
        pool.play(idSoundAnalysis, 1.0f, 1.0f, 0,0,1.0f)
    }

}