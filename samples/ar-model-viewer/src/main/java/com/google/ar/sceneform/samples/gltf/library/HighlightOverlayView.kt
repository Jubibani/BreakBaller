package com.google.ar.sceneform.samples.gltf.library

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.mlkit.vision.text.Text

class HighlightOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val highlightRects = mutableListOf<RectF>()
    private val paint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    var recognizableModelNames: Set<String> = emptySet()
        set(value) {
            field = value.map { it.lowercase() }.toSet()
            Log.d("HighlightOverlay", "Updated recognizable names: $field")
        }

    @Volatile
    var imageWidth: Int = 1
    @Volatile
    var imageHeight: Int = 1
    @Volatile
    private var shouldDraw = false

    fun updateTextResult(filteredTextBlocks: List<Text.TextBlock>, imageWidth: Int, imageHeight: Int) {
        Log.d("HighlightOverlay", "Updating text result with ${filteredTextBlocks.size} blocks")
        clearOverlay()
        val scaleX = width.toFloat() / imageWidth
        val scaleY = height.toFloat() / imageHeight

        for (block in filteredTextBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    val boundingBox = element.boundingBox ?: continue
                    val left = boundingBox.left * scaleX
                    val top = boundingBox.top * scaleY
                    val right = boundingBox.right * scaleX
                    val bottom = boundingBox.bottom * scaleY
                    addHighlightRect(RectF(left, top, right, bottom))
                    Log.d("HighlightOverlay", "Added rect: $left, $top, $right, $bottom")
                }
            }
        }

        shouldDraw = highlightRects.isNotEmpty()
        Log.d("HighlightOverlay", "Should draw: $shouldDraw")
        invalidate()
    }

    fun addHighlightRect(rect: RectF) {
        highlightRects.add(rect)
        shouldDraw = true
        Log.d("HighlightOverlay", "Added highlight rect: $rect")
    }

    fun clearOverlay() {
        this.shouldDraw = false
        highlightRects.clear()
        postInvalidate()
        Log.d("HighlightOverlay", "Overlay cleared.")
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!shouldDraw) {
            Log.v("HighlightOverlay", "Skipping draw: shouldDraw is false")
            return
        }

        for (rect in highlightRects) {
            canvas.drawRect(rect, paint)
        }

        Log.d("HighlightOverlay", "Draw finished. Drew ${highlightRects.size} boxes.")
    }
}