package org.diant.nemqr

import android.view.View
import android.view.Window

class SystemUI(private val window: Window) {

    fun onCreate() {
        window.decorView.setOnSystemUiVisibilityChangeListener {
            if (it == 0) {
                window.decorView.systemUiVisibility = getFlags()
            }
        }
    }

    fun onFocusChanged(isFocused: Boolean) {

        if (isFocused) {
            window.decorView.systemUiVisibility = getFlags()
        }

    }

    private fun getFlags(): Int {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

}