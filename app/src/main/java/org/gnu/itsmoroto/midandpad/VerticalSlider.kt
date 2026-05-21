package org.gnu.itsmoroto.midandpad

import android.content.res.ColorStateList
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
    private val centeredTrackPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Material Slider track side padding: max(defaultThumbRadius=10dp, minTouchTargetSize/2=24dp) = 24dp
    private val neutralMarkSidePadPx by lazy {
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics)
    }

    // Captured once at attach time so thumb press animation doesn't change the mark size
    private var neutralMarkHalfThumbPx: Float = 0f
    private var centeredTrackEnabled: Boolean = false
    private var defaultTrackActiveTintList: ColorStateList? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        neutralMarkHalfThumbPx = thumbRadius.toFloat()
        neutralMarkPaint.color = thumbTintList?.defaultColor ?: 0xFFAAAAAA.toInt()
        defaultTrackActiveTintList = trackActiveTintList
        centeredTrackPaint.color = defaultTrackActiveTintList?.defaultColor
            ?: (thumbTintList?.defaultColor ?: 0xFFAAAAAA.toInt())
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
        updateCenteredTrackState()
        if (isHorizontal) {
            super.onDraw(canvas)
        } else {
            canvas.save()
            canvas.rotate(-90f)
            canvas.translate(-height.toFloat(), 0f)
            super.onDraw(canvas)
            canvas.restore()
        }
        if (neutralMarkEnabled && valueTo > valueFrom && width > 0 && height > 0) {
            if (shouldUseCenteredTrack()) {
                drawCenteredTrack(canvas)
            }
            drawNeutralMark(canvas)
        }
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

    private fun shouldUseCenteredTrack(): Boolean {
        return neutralMarkEnabled &&
            valueTo > valueFrom &&
            neutralMarkValue > valueFrom &&
            neutralMarkValue < valueTo
    }

    private fun updateCenteredTrackState() {
        val shouldEnable = shouldUseCenteredTrack()
        if (shouldEnable && !centeredTrackEnabled) {
            trackInactiveTintList?.let { trackActiveTintList = it }
            centeredTrackEnabled = true
        } else if (!shouldEnable && centeredTrackEnabled) {
            trackActiveTintList = defaultTrackActiveTintList
            centeredTrackEnabled = false
        }
    }

    private fun drawCenteredTrack(canvas: Canvas) {
        if (valueTo == valueFrom) return
        centeredTrackPaint.color = defaultTrackActiveTintList?.defaultColor
            ?: (trackActiveTintList?.defaultColor ?: neutralMarkPaint.color)

        val thumbFraction = (value - valueFrom) / (valueTo - valueFrom)
        val centerFraction = (neutralMarkValue - valueFrom) / (valueTo - valueFrom)
        val halfTrack = trackHeight / 2f

        if (isHorizontal) {
            val centerX = neutralMarkSidePadPx + centerFraction * (width - 2f * neutralMarkSidePadPx)
            val thumbX = neutralMarkSidePadPx + thumbFraction * (width - 2f * neutralMarkSidePadPx)
            val left = minOf(centerX, thumbX)
            val right = maxOf(centerX, thumbX)
            if (right > left) {
                val rect = RectF(left, (height / 2f) - halfTrack, right, (height / 2f) + halfTrack)
                canvas.drawRoundRect(rect, halfTrack, halfTrack, centeredTrackPaint)
            }
        } else {
            val centerY = neutralMarkSidePadPx + (1f - centerFraction) * (height - 2f * neutralMarkSidePadPx)
            val thumbY = neutralMarkSidePadPx + (1f - thumbFraction) * (height - 2f * neutralMarkSidePadPx)
            val top = minOf(centerY, thumbY)
            val bottom = maxOf(centerY, thumbY)
            if (bottom > top) {
                val rect = RectF((width / 2f) - halfTrack, top, (width / 2f) + halfTrack, bottom)
                canvas.drawRoundRect(rect, halfTrack, halfTrack, centeredTrackPaint)
            }
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
