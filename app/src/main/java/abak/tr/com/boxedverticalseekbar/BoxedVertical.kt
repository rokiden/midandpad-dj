package abak.tr.com.boxedverticalseekbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.graphics.drawable.DrawableCompat
import org.gnu.itsmoroto.midandpad.R
import kotlin.math.roundToInt

open class BoxedVertical : AppCompatSeekBar, SeekBar.OnSeekBarChangeListener {
    interface OnValuesChangeListener {
        fun onPointsChanged(boxedPoints: BoxedVertical?, points: Int)
        fun onStartTrackingTouch(boxedPoints: BoxedVertical?)
        fun onStopTrackingTouch(boxedPoints: BoxedVertical?)
    }

    private var boxedListener: OnValuesChangeListener? = null
    private var touchDisabled = false
    private var stepSize = 1

    constructor(context: Context?) : super(requireNotNull(context)) {
        initialize(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(requireNotNull(context), attrs) {
        initialize(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        requireNotNull(context),
        attrs,
        defStyleAttr
    ) {
        initialize(attrs)
    }

    private fun initialize(attrs: AttributeSet?) {
        setOnSeekBarChangeListener(this)
        if (attrs == null) return

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BoxedVertical)
        try {
            max = typedArray.getInt(R.styleable.BoxedVertical_max, max)
            stepSize = typedArray.getInt(R.styleable.BoxedVertical_step, 1).coerceAtLeast(1)
            touchDisabled = typedArray.getBoolean(R.styleable.BoxedVertical_touchDisabled, false)

            val defaultValue = typedArray.getInt(R.styleable.BoxedVertical_defaultValue, progress)
            value = defaultValue

            if (typedArray.hasValue(R.styleable.BoxedVertical_backgroundColor)) {
                val backgroundColor = typedArray.getColor(
                    R.styleable.BoxedVertical_backgroundColor,
                    Color.TRANSPARENT
                )
                progressBackgroundTintList = android.content.res.ColorStateList.valueOf(backgroundColor)
            }

            if (typedArray.hasValue(R.styleable.BoxedVertical_progressColor)) {
                val progressColor = typedArray.getColor(
                    R.styleable.BoxedVertical_progressColor,
                    Color.TRANSPARENT
                )
                progressDrawable?.let {
                    DrawableCompat.setTint(it.mutate(), progressColor)
                }
                thumbTintList = android.content.res.ColorStateList.valueOf(progressColor)
            }
        } finally {
            typedArray.recycle()
        }
    }

    fun setOnBoxedPointsChangeListener(listener: OnValuesChangeListener?) {
        boxedListener = listener
    }

    var value: Int
        get() = progress
        set(newValue) {
            val clamped = newValue.coerceIn(0, max)
            progress = if (stepSize > 1) (clamped / stepSize) * stepSize else clamped
        }

    override fun onDraw(canvas: Canvas) {
        canvas.rotate(-90f)
        canvas.translate(-height.toFloat(), 0f)
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled || touchDisabled) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP -> {
                val ratio = (1f - event.y / height.toFloat()).coerceIn(0f, 1f)
                value = (ratio * max).roundToInt()
                onSizeChanged(width, height, 0, 0)
            }
        }

        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            boxedListener?.onStopTrackingTouch(this)
        }

        return true
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        boxedListener?.onPointsChanged(this, progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        boxedListener?.onStartTrackingTouch(this)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        boxedListener?.onStopTrackingTouch(this)
    }
}
