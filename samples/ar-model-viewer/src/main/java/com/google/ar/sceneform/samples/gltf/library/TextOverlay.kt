package com.google.ar.sceneform.samples.gltf.library

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.text.Text

class TextOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private var recognizedText: Text? = null

    fun setRecognizedText(text: Text) {
        recognizedText = text
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        recognizedText?.let { text ->
            for (block in text.textBlocks) {
                for (line in block.lines) {
                    line.boundingBox?.let { rect ->
                        canvas.drawRect(rect, paint)
                    }
                }
            }
        }
    }
}