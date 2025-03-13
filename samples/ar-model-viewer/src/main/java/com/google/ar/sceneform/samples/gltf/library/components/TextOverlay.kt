package com.google.ar.sceneform.samples.gltf.library.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class TextOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var recognizedText: String? = null
    private val paint = Paint().apply {
        color = android.graphics.Color.RED
        textSize = 60f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        recognizedText?.let {
            canvas.drawText(it, 10f, 100f, paint)
        }
    }
}