package org.gnu.itsmoroto.midandpad

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.slider.Slider

open class VerticalSlider : Slider {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var isHorizontal: Boolean = false

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
        if (!isHorizontal) {
            canvas.rotate(-90f)
            canvas.translate(-height.toFloat(), 0f)
        }
        super.onDraw(canvas)
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
