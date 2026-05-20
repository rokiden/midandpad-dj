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
    private val neutralMarkStrokeWidthPx by lazy {
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(canvas: Canvas) {
        if (neutralMarkEnabled && valueTo > valueFrom && height > 0) {
            drawNeutralMark(canvas)
        }
        canvas.rotate(-90f)
        canvas.translate(-height.toFloat(), 0f)
        super.onDraw(canvas)
    }

    private fun drawNeutralMark(canvas: Canvas) {
        if (valueTo == valueFrom) return
        val fraction = (neutralMarkValue - valueFrom) / (valueTo - valueFrom)
        val halfHeight = neutralMarkStrokeWidthPx / 2f
        val markY = neutralMarkSidePadPx + (1f - fraction) * (height - 2f * neutralMarkSidePadPx)
        neutralMarkPaint.color = trackInactiveTintList.defaultColor
        val rect = RectF(0f, markY - halfHeight, width.toFloat(), markY + halfHeight)
        canvas.drawRoundRect(rect, halfHeight, halfHeight, neutralMarkPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        val rotatedEvent = MotionEvent.obtain(event)
        rotatedEvent.setLocation(height - event.y, event.x)

        val result = super.onTouchEvent(rotatedEvent)
        rotatedEvent.recycle()

        return result
    }
}
