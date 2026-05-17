package org.gnu.itsmoroto.midandpad

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import com.google.android.material.slider.Slider

class CCBar : VerticalSlider, Slider.OnChangeListener, Slider.OnSliderTouchListener {
    private lateinit var mLabel: TextView
    constructor(context: Context?): super (context!!){
        setOnClick()
    }
    constructor(context: Context?, attributeSet: AttributeSet): super (context!!, attributeSet){
        setOnClick ()
    }



    var mNumber: Int = 0
    var mName: String = ""
    var mChannel: Int = MidandpadDB.DEFAULT_CHANNEL
    private var mControl: Int = 0 //Special controls, like pitch bend are negatives.
    var mRZ: Boolean = false
    var mZeroPos: Int = 0
    var mDefaultValue: Float = 64f
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

    companion object {
        const val DO_NOTHING = -2
        const val PITCH_BEND = -1 //zero on msb 64 and lsb 0 (msb and lsb are 7bit bytes)
        const val PITCHCENTER = 0x2000
        const val PITCHCENTERU: UByte = 0x40U
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
                val msg = ubyteArrayOf(mControl.toUByte(), value.toInt().toUByte()
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
            showErrorDialog(context, "MIDI error", context.getString(R.string.nomidiconn))
            return
        }
        else {
            if (mControl == PITCH_BEND){
                val channel = if (mChannel != MidandpadDB.DEFAULT_CHANNEL) mChannel.toUByte()
                    else MainActivity.mConfigParams.mDefaultChannel
                val command: UByte = MidiHelper.STATUS_PITCH_BEND or channel
                val msg = ubyteArrayOf(0U, PITCHCENTERU)
                MainActivity.mMidi.send(command, msg.toByteArray())
                // Remove and re-add listener to avoid triggering onValueChange when resetting
                clearOnChangeListeners()
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
                // Remove and re-add listener to avoid triggering onValueChange when resetting
                clearOnChangeListeners()
                value = mZeroPos.toFloat()
                addOnChangeListener(this)
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
            valueTo = 0x7F.toFloat()
        }
        else {
            valueTo = 0x3FFF.toFloat()
            value = PITCHCENTER.toFloat()
        }
        mControl = control
    }

    fun getControl (): Int{
        return mControl
    }
}
