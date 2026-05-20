package org.gnu.itsmoroto.midandpad

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
        color = Color.WHITE
        style = Paint.Style.STROKE
        isAntiAlias = true
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
        val fraction = (neutralMarkValue - valueFrom) / (valueTo - valueFrom)
        // Material Slider track side padding: max(defaultThumbRadius=10dp, minTouchTargetSize/2=24dp) = 24dp
        val sidePadPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics
        )
        neutralMarkPaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics
        )
        // In vertical view: y=0 is top (high value end), y=height is bottom (low value end)
        val markY = sidePadPx + (1f - fraction) * (height - 2f * sidePadPx)
        canvas.drawLine(0f, markY, width.toFloat(), markY, neutralMarkPaint)
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
