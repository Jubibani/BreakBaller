package com.google.ar.sceneform.samples.gltf.library

import android.content.Context
import android.graphics.*
import android.view.View
import com.google.mlkit.vision.text.Text

class HighlightOverlayView(context: Context) : View(context) {

    private var textResult: Text? = null
    private val paint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    var recognizableModelNames: List<String> = emptyList()

    fun updateTextResult(result: Text) {
        textResult = result
        invalidate() // Request a redraw of the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textResult?.let { result ->
            for (block in result.textBlocks) {
                for (line in block.lines) {
                    if (recognizableModelNames.contains(line.text)) {
                        line.boundingBox?.let { box ->
                            // Need to map the bounding box to the view's coordinates
                            // This is a simplified example and might need adjustments
                            canvas.drawRect(box, paint)
                        }
                    }
                }
            }
        }
    }
}