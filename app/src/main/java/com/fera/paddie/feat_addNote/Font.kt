package com.fera.paddie.feat_addNote

import android.graphics.Typeface

class _Font {
    companion object {
        private const val fontSize = 17F
        private val typeFace = Typeface.DEFAULT

        fun defaultFontProperties(): FontProperties {
            return FontProperties(fontSize, typeFace)
        }
    }
}

data class FontProperties (
    val fontSize: Float,
    val typeface: Typeface
)