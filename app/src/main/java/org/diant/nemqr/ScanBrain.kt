package org.diant.nemqr

import androidx.core.text.isDigitsOnly

class ScanBrain {

    private var slotOccupied: Boolean = false
    private var textCache: String = ""

    fun resetBrain() {
        slotOccupied = false
        textCache = ""
    }

    fun isResultOccupied(): Boolean {
        return slotOccupied
    }

    fun tryOccupyResult(text: String): Boolean {
        if (slotOccupied)
            return false
        if (text.startsWith("Nem") and text.substringAfter("Nem").isDigitsOnly()) {
            slotOccupied = true
            textCache = text
            return true
        }
        return false
    }

    fun isResultCheckPassed(): Boolean {
        return textCache.substringAfter("Nem").toInt() % 2 != 0
    }

}