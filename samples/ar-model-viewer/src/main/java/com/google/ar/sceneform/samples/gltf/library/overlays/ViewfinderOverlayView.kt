package com.google.ar.sceneform.samples.gltf.library.overlays
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class ViewfinderOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.parseColor("#80000000") // Semi-transparent dark color
        style = Paint.Style.FILL
    }

    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val borderPaint = Paint().apply {
        color = Color.RED // Border color for debugging
        style = Paint.Style.STROKE
        strokeWidth = 5f // Border thickness
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val radius = Math.min(width, height) / 3 // Adjust the radius as needed

        // Draw the semi-transparent overlay
        canvas.drawRect(0f, 0f, width, height, paint)

        // Draw the circular cut-out
        canvas.drawCircle(width / 2, height / 2, radius, clearPaint)

        // Draw the border around the circular cut-out
        canvas.drawCircle(width / 2, height / 2, radius, borderPaint)
    }
}