package com.gonchimonchi.dragrace.utils

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.FrameLayout
import kotlin.math.max
import kotlin.math.min

class ZoomLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var scaleFactor = 1f
    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val factor = detector.scaleFactor
            scaleFactor = max(0.5f, min(scaleFactor * factor, 3.0f))
            Log.d("ZoomLayout", "Zoom factor: $scaleFactor")
            onScaleChanged?.invoke(scaleFactor)
            requestLayout() // Fuerza redibujar con nueva escala (tamaño)
            return true
        }
    })

    var onScaleChanged: ((Float) -> Unit)? = null

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Si hay multitouch, interceptamos para zoom
        if (ev.pointerCount > 1) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val scaledWidthSpec = MeasureSpec.makeMeasureSpec(
            (MeasureSpec.getSize(widthMeasureSpec) * scaleFactor).toInt(),
            MeasureSpec.getMode(widthMeasureSpec)
        )
        val scaledHeightSpec = MeasureSpec.makeMeasureSpec(
            (MeasureSpec.getSize(heightMeasureSpec) * scaleFactor).toInt(),
            MeasureSpec.getMode(heightMeasureSpec)
        )
        super.onMeasure(scaledWidthSpec, scaledHeightSpec)
    }
}
