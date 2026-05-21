package org.gnu.itsmoroto.midandpad

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import com.google.android.material.slider.Slider

open class VerticalSlider : Slider {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var isHorizontal: Boolean = false
    var neutralMarkEnabled: Boolean = false
    var neutralMarkValue: Float = 64f

    private val neutralMarkPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Material Slider track side padding: max(defaultThumbRadius=10dp, minTouchTargetSize/2=24dp) = 24dp
    private val neutralMarkSidePadPx by lazy {
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics)
    }

    // Captured once at attach time so thumb press animation doesn't change the mark size
    private var neutralMarkHalfThumbPx: Float = 0f

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        neutralMarkHalfThumbPx = thumbRadius.toFloat()
        neutralMarkPaint.color = thumbTintList?.defaultColor ?: 0xFFAAAAAA.toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (isHorizontal) {
            super.onSizeChanged(w, h, oldw, oldh)
        } else {
            super.onSizeChanged(h, w, oldh, oldw)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isHorizontal) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            super.onMeasure(heightMeasureSpec, widthMeasureSpec)
            setMeasuredDimension(measuredHeight, measuredWidth)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (neutralMarkEnabled && valueTo > valueFrom && width > 0 && height > 0) {
            drawNeutralMark(canvas)
        }
        if (!isHorizontal) {
            canvas.rotate(-90f)
            canvas.translate(-height.toFloat(), 0f)
        }
        super.onDraw(canvas)
    }

    private fun drawNeutralMark(canvas: Canvas) {
        if (valueTo == valueFrom) return
        val fraction = (neutralMarkValue - valueFrom) / (valueTo - valueFrom)
        val halfThumb = neutralMarkHalfThumbPx
        if (isHorizontal) {
            val markX = neutralMarkSidePadPx + fraction * (width - 2f * neutralMarkSidePadPx)
            val rect = RectF(markX - halfThumb, 0f, markX + halfThumb, height.toFloat())
            canvas.drawRoundRect(rect, halfThumb, halfThumb, neutralMarkPaint)
        } else {
            val markY = neutralMarkSidePadPx + (1f - fraction) * (height - 2f * neutralMarkSidePadPx)
            val rect = RectF(0f, markY - halfThumb, width.toFloat(), markY + halfThumb)
            canvas.drawRoundRect(rect, halfThumb, halfThumb, neutralMarkPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        if (isHorizontal) {
            return super.onTouchEvent(event)
        }

        val rotatedEvent = MotionEvent.obtain(event)
        rotatedEvent.setLocation(height - event.y, event.x)

        val result = super.onTouchEvent(rotatedEvent)
        rotatedEvent.recycle()

        return result
    }
}
