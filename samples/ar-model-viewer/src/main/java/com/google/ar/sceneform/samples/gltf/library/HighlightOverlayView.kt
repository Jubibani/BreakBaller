package com.google.ar.sceneform.samples.gltf.library

import android.annotation.SuppressLint
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

    // Image dimensions (set these when processing the image)
    var imageWidth: Int = 0
    var imageHeight: Int = 0

    fun updateTextResult(result: Text, imageWidth: Int, imageHeight: Int) {
        textResult = result
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        invalidate() // Request a redraw of the view
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        textResult?.let { result ->
            val scaleX = width.toFloat() / imageWidth
            val scaleY = height.toFloat() / imageHeight

            for (block in result.textBlocks) {
                for (line in block.lines) {
                    if (recognizableModelNames.contains(line.text)) {
                        line.boundingBox?.let { box ->
                            // Map the bounding box to the view's coordinates
                            val mappedBox = RectF(
                                box.left * scaleX,
                                box.top * scaleY,
                                box.right * scaleX,
                                box.bottom * scaleY
                            )
                            canvas.drawRect(mappedBox, paint)
                        }
                    }
                }
            }
        }
    }
}