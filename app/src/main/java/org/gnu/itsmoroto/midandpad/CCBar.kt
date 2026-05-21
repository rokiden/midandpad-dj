package org.gnu.itsmoroto.midandpad

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import com.google.android.material.slider.Slider
import kotlin.math.roundToInt

class CCBar : VerticalSlider, Slider.OnChangeListener, Slider.OnSliderTouchListener {
    private lateinit var mLabel: TextView
    private var mBaseValueFrom: Float = 0f
    private var mBaseValueTo: Float = 127f
    constructor(context: Context?): super (context!!){
        setOnClick()
        mBaseValueFrom = valueFrom
        mBaseValueTo = valueTo
    }
    constructor(context: Context?, attributeSet: AttributeSet): super (context!!, attributeSet){
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.CCBar)
        mSnapZone = a.getInt(R.styleable.CCBar_snapZone, 0)
        isHorizontal = a.getBoolean(R.styleable.CCBar_horizontal, false)
        a.recycle()
        neutralMarkEnabled = mSnapZone > 0
        setOnClick ()
        mBaseValueFrom = valueFrom
        mBaseValueTo = valueTo
    }



    var mNumber: Int = 0
    var mName: String = ""
    var mChannel: Int = MidandpadDB.DEFAULT_CHANNEL
    private var mControl: Int = 0 //Special controls, like pitch bend are negatives.
    var mRZ: Boolean = false
    var mZeroPos: Int = 0
    var mDefaultValue: Float = 64f
        set(value) {
            field = value
            neutralMarkValue = value
        }
    var mSnapZone: Int = 0
    private var mCurrpos: Int = 0
    var misEdit: Boolean = false
    public fun setLabelWidget (label:TextView){
        mLabel = label
    }

    public fun setName (name: String){
        mLabel.text = name
        mName = name
    }

    private fun setOnClick (){
        addOnChangeListener(this)
        addOnSliderTouchListener(this)
    }

    private fun usesCenteredMidiMapping(): Boolean {
        return valueFrom < 0f && valueTo > 0f &&
            kotlin.math.abs((valueTo - valueFrom) - 127f) < MIDI_CENTERED_EPSILON
    }

    fun sliderToStoredValue(sliderValue: Float): Int {
        val rounded = sliderValue.roundToInt()
        if (usesCenteredMidiMapping()) {
            val from = valueFrom.roundToInt()
            return (rounded - from).coerceIn(0, 127)
        }
        return rounded
    }

    fun storedToSliderValue(storedValue: Int): Float {
        if (usesCenteredMidiMapping()) {
            val from = valueFrom.roundToInt()
            val to = valueTo.roundToInt()
            return (from + storedValue.coerceIn(0, 127)).coerceIn(from, to).toFloat()
        }
        return storedValue.toFloat().coerceIn(valueFrom, valueTo)
    }

    private fun sliderToCcValue(sliderValue: Float): Int {
        return sliderToStoredValue(sliderValue).coerceIn(0, 127)
    }

    companion object {
        const val DO_NOTHING = -2
        const val PITCH_BEND = -1 //zero on msb 64 and lsb 0 (msb and lsb are 7bit bytes)
        const val PITCHCENTER = 0x2000
        const val PITCHCENTERU: UByte = 0x40U
        private const val MIDI_CENTERED_EPSILON = 0.001f
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        if (MainActivity.mConfigParams.mMode == ConfigParams.RUN_MODE &&
            MainActivity.mMidi.haveConnection()) {
            var channel: UByte = MainActivity.mConfigParams.mDefaultChannel
            if (mChannel != MidandpadDB.DEFAULT_CHANNEL)
                channel = mChannel.toUByte()
            if (mControl != PITCH_BEND) {
                val command: UByte = MidiHelper.STATUS_CONTROL_CHANGE or channel
                val msg = ubyteArrayOf(mControl.toUByte(), sliderToCcValue(value).toUByte()
                )
                MainActivity.mMidi.send(command, msg.toByteArray())
            }
            else {
                val points = value.toInt()
                val lsb = (points and 0x7F).toUByte()
                val msb = (points ushr 7).toUByte()
                val command: UByte = MidiHelper.STATUS_PITCH_BEND or channel
                val msg = ubyteArrayOf(lsb, msb)
                MainActivity.mMidi.send(command, msg.toByteArray())
            }
        }
    }

    override fun onStartTrackingTouch(slider: Slider) {
        mCurrpos = slider.value.toInt()
        return
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun onStopTrackingTouch(slider: Slider) {
        if (MainActivity.mConfigParams.mMode == ConfigParams.EDIT_MODE
            && !misEdit) {
            misEdit = true
            editMe()
        }
        else if (!MainActivity.mMidi.haveConnection()){
            org.gnu.itsmoroto.midandpad.showErrorDialog(
                context,
                "MIDI error",
                context.getString(R.string.nomidiconn)
            )
            return
        }
        else {
            if (mControl == PITCH_BEND){
                val channel = if (mChannel != MidandpadDB.DEFAULT_CHANNEL) mChannel.toUByte()
                    else MainActivity.mConfigParams.mDefaultChannel
                val command: UByte = MidiHelper.STATUS_PITCH_BEND or channel
                val msg = ubyteArrayOf(0U, PITCHCENTERU)
                MainActivity.mMidi.send(command, msg.toByteArray())
                // Temporarily remove only this listener to avoid recursive callbacks.
                removeOnChangeListener(this)
                value = PITCHCENTER.toFloat()
                addOnChangeListener(this)
            }
            else if (mRZ) {
                val channel = if (mChannel != MidandpadDB.DEFAULT_CHANNEL) mChannel.toUByte()
                    else MainActivity.mConfigParams.mDefaultChannel

                val command: UByte = MidiHelper.STATUS_CONTROL_CHANGE or channel
                val msg = ubyteArrayOf(mControl.toUByte(), mZeroPos.toUByte()
                )
                MainActivity.mMidi.send(command, msg.toByteArray())
                // Temporarily remove only this listener to avoid recursive callbacks.
                removeOnChangeListener(this)
                value = mZeroPos.toFloat()
                addOnChangeListener(this)
            }
            else if (mSnapZone > 0) {
                val snapTarget = if (mDefaultValue >= valueFrom && mDefaultValue <= valueTo) {
                    mDefaultValue
                } else {
                    (valueFrom + valueTo) / 2f
                }
                if (kotlin.math.abs(value - snapTarget) <= mSnapZone.toFloat()) {
                    val channel = if (mChannel != MidandpadDB.DEFAULT_CHANNEL) mChannel.toUByte()
                        else MainActivity.mConfigParams.mDefaultChannel
                    val command: UByte = MidiHelper.STATUS_CONTROL_CHANGE or channel
                    val msg = ubyteArrayOf(mControl.toUByte(), sliderToCcValue(snapTarget).toUByte())
                    MainActivity.mMidi.send(command, msg.toByteArray())
                    // Temporarily remove only this listener to avoid recursive callbacks.
                    removeOnChangeListener(this)
                    value = snapTarget
                    addOnChangeListener(this)
                }
            }

        }
    }

    fun editMe () {
        if (MainActivity.mConfigParams.mMode == ConfigParams.EDIT_MODE){
            (context as MainActivity).mBarConfigScreen.setBar(this)
            (context as MainActivity).changeView((context as MainActivity).mBarConfigScreen)
        }
    }


    fun setControl (control: Int) {
        if (control != PITCH_BEND){
            valueFrom = mBaseValueFrom
            valueTo = mBaseValueTo
            value = value.coerceIn(valueFrom, valueTo)
        }
        else {
            valueFrom = 0f
            valueTo = 0x3FFF.toFloat()
            value = PITCHCENTER.toFloat()
        }
        mControl = control
    }

    fun getControl (): Int{
        return mControl
    }
}
