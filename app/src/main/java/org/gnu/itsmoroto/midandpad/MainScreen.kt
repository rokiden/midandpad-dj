package org.gnu.itsmoroto.midandpad

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout

class MainScreen (context: Context): ConstraintLayout(context) {


    private val mEditButton:ImageButton
    private val mMidiButton:ImageButton
    private val mSaveButton:ImageButton
    private val mSaveAsButton:ImageButton
    private val mRunButton:ImageButton
    private val mExploreButton:ImageButton
    private val mCalibrationButton: ImageButton
    private val mMidiLogoIcon: ImageView

    private val mHaveMIDIcheck: CheckBox
    private val mHaveClockCheck:CheckBox

    private val mCurrPresetLabel: TextView

    companion object{
        public const val CONTROLSCOUNT = 10
        private lateinit var mEventButtons: Array<EventButton>
        private lateinit var mControlBars: Array<CCBar>
        fun clockTick (){
            for (b in mEventButtons) {
                b.tick()
            }
        }
    }

    init {
        inflate(getContext(), R.layout.activity_main, this)
        mEditButton = findViewById(R.id.buttonedit)
        mEditButton.setOnClickListener { _: View ->
            onEditClick()
        }

        mMidiButton= findViewById(R.id.buttonmidi)
        mMidiButton.setOnClickListener { _: View ->
            onMidiClick()
        }

        mSaveButton = findViewById(R.id.buttonsave)
        mSaveButton.setOnClickListener {_: View->
            onSaveClick ()
        }
        mSaveAsButton = findViewById(R.id.buttonsaveas)
        mSaveAsButton.setOnClickListener {_: View->
            onSaveAsClick ()
        }
        mRunButton = findViewById(R.id.buttonrun)
        mRunButton.setOnClickListener { _: View ->
            onRunClick()
        }

        mExploreButton = findViewById(R.id.buttonexplore)
        mExploreButton.setOnClickListener { _: View->
            onExploreClick ()
        }

        mCalibrationButton = findViewById(R.id.buttoncalib)
        mCalibrationButton.setOnClickListener {_: View->
            onCalibrationClick ()
        }
        mMidiLogoIcon = findViewById(R.id.midilogo)
        mMidiLogoIcon.setOnClickListener { onMidiLogoClick() }


        mHaveMIDIcheck = findViewById(R.id.havemidi)
        mHaveMIDIcheck.isChecked = false
        mHaveClockCheck = findViewById(R.id.haveclock)
        mHaveClockCheck.isChecked = false

        mCurrPresetLabel = findViewById(R.id.labelpreset)

        val deck1 = findViewById<ConstraintLayout>(R.id.deck1)
        val deck2 = findViewById<ConstraintLayout>(R.id.deck2)
        val mixer = findViewById<ConstraintLayout>(R.id.mixer)

        mEventButtons = arrayOf(
            // Deck 1
            deck1.findViewById(R.id.btn_loop_in) as EventButton,
            deck1.findViewById(R.id.btn_loop_out) as EventButton,
            deck1.findViewById(R.id.btn_loop_auto) as EventButton,
            deck1.findViewById(R.id.btn_jump_back_16) as EventButton,
            deck1.findViewById(R.id.btn_jump_back_4) as EventButton,
            deck1.findViewById(R.id.btn_jump_forward_4) as EventButton,
            deck1.findViewById(R.id.btn_jump_forward_16) as EventButton,
            deck1.findViewById(R.id.btn_hotcue_1) as EventButton,
            deck1.findViewById(R.id.btn_hotcue_2) as EventButton,
            deck1.findViewById(R.id.btn_hotcue_3) as EventButton,
            deck1.findViewById(R.id.btn_hotcue_4) as EventButton,
            deck1.findViewById(R.id.btn_play_pause) as EventButton,
            deck1.findViewById(R.id.btn_cue) as EventButton,
            deck1.findViewById(R.id.btn_sync) as EventButton,
            // Deck 2
            deck2.findViewById(R.id.btn_loop_in) as EventButton,
            deck2.findViewById(R.id.btn_loop_out) as EventButton,
            deck2.findViewById(R.id.btn_loop_auto) as EventButton,
            deck2.findViewById(R.id.btn_jump_back_16) as EventButton,
            deck2.findViewById(R.id.btn_jump_back_4) as EventButton,
            deck2.findViewById(R.id.btn_jump_forward_4) as EventButton,
            deck2.findViewById(R.id.btn_jump_forward_16) as EventButton,
            deck2.findViewById(R.id.btn_hotcue_1) as EventButton,
            deck2.findViewById(R.id.btn_hotcue_2) as EventButton,
            deck2.findViewById(R.id.btn_hotcue_3) as EventButton,
            deck2.findViewById(R.id.btn_hotcue_4) as EventButton,
            deck2.findViewById(R.id.btn_play_pause) as EventButton,
            deck2.findViewById(R.id.btn_cue) as EventButton,
            deck2.findViewById(R.id.btn_sync) as EventButton,
            // Mixer
            mixer.findViewById(R.id.btn_cue_1) as EventButton,
            mixer.findViewById(R.id.btn_cue_m) as EventButton,
            mixer.findViewById(R.id.btn_cue_2) as EventButton
        )
        mEditButton.setBackgroundResource(android.R.drawable.btn_default)
        mEventButtons.forEachIndexed { i, b -> b.mNumber = i + 1 }
        mControlBars = arrayOf(
            // Deck 1
            deck1.findViewById(R.id.slider_eq1) as CCBar,
            deck1.findViewById(R.id.slider_eq2) as CCBar,
            deck1.findViewById(R.id.slider_eq3) as CCBar,
            deck1.findViewById(R.id.slider_eq4) as CCBar,
            // Deck 2
            deck2.findViewById(R.id.slider_eq1) as CCBar,
            deck2.findViewById(R.id.slider_eq2) as CCBar,
            deck2.findViewById(R.id.slider_eq3) as CCBar,
            deck2.findViewById(R.id.slider_eq4) as CCBar,
            // Mixer
            mixer.findViewById(R.id.slider_deck_1) as CCBar,
            mixer.findViewById(R.id.slider_deck_2) as CCBar
        )

        // Wire layout labels for EQ/CFX bars in each deck
        val deckViews = listOf(deck1, deck2)
        val eqLabelIds = listOf(R.id.label_eq1, R.id.label_eq2, R.id.label_eq3, R.id.label_eq4)
        deckViews.forEachIndexed { deckIdx, deckView ->
            eqLabelIds.forEachIndexed { eqIdx, labelId ->
                mControlBars[deckIdx * 4 + eqIdx].setLabelWidget(deckView.findViewById(labelId))
            }
        }
        // Mixer bars use dummy labels (not shown in layout)
        mControlBars[8].setLabelWidget(TextView(context))
        mControlBars[9].setLabelWidget(TextView(context))
        mControlBars.forEachIndexed { i, b -> b.mNumber = i + 1 }

        configControls()
    }

    fun configControls (){
        MainActivity.mConfigParams.configButtons(mEventButtons)
        MainActivity.mConfigParams.configBars (mControlBars)
    }
    private fun onMidiClick() {

        /*if ((context as MainActivity).m_midiconfig == null)
            context.m_midiconfig = MidiConfig (this, context)*/
        (context as MainActivity).changeView((context as MainActivity).mMidiConfig)
    }

    private fun onMidiLogoClick() {
        val mainActivity = context as MainActivity
        if (!mainActivity.connectAndroidUsbPeripheralMidi()) {
            showErrorDialog(
                context,
                resources.getString(R.string.snomidiintitle),
                resources.getString(R.string.nomidiconn)
            )
        }
    }

    private fun onEditClick (){
        mEditButton.setBackgroundResource(com.google.android.material.R.color.design_default_color_secondary)
        mRunButton.setBackgroundResource(android.R.drawable.btn_default)
        MainActivity.mConfigParams.mMode = ConfigParams.EDIT_MODE
    }

    private fun onRunClick (){
        mRunButton.setBackgroundResource(com.google.android.material.R.color.design_default_color_secondary)
        mEditButton.setBackgroundResource(android.R.drawable.btn_default)
        MainActivity.mConfigParams.mMode = ConfigParams.RUN_MODE
    }

    private fun onExploreClick (){
        val exploreScreen = (context as MainActivity).mExploreScreen
        exploreScreen.updateData()
        (context as MainActivity).changeView(exploreScreen)
    }

    fun onSaveClick (){
        if (MainActivity.mConfigParams.mCurrPreset == 1L){
                /*Toast.makeText(context, resources.getString(R.string.ssavefactory),
                    ConfigParams.TOASTLENGTH).show()*/
            showErrorDialog(context, "Error", resources.getString(R.string.ssavefactory))
                return;
        }
        MainActivity.mConfigParams.savePreset(mEventButtons, mControlBars)
        Toast.makeText(context, resources.getString(R.string.spresetsaved)
            .replace(ReplaceLabels.PRESETNAMELABEL, MainActivity.mConfigParams.mCurrPresetName),
            ConfigParams.TOASTLENGTH).show()
    }

    private fun onSaveAsClick (){
        val dlgTitle = context.resources.getString(R.string.srenamepreset)
        //val textlayout: TextInputLayout = TextInputLayout(context)
        val textinput = EditText (context)
        //textlayout.addView(textinput)
        AlertDialog.Builder (context, androidx.appcompat.R.style.AlertDialog_AppCompat)
            .setTitle(dlgTitle)
            //.setView(textlayout)
            .setView(textinput)
            .setPositiveButton(R.string.sok){
                    dialog: DialogInterface, _->
                val newname: String = textinput.text.toString().trim()
                if (newname == ""){
                    return@setPositiveButton
                }
                if (MainActivity.mConfigParams.checkPresetName(newname)){
                    val msg = context.resources.getString(R.string.spresetnameexists)
                        .replace(ReplaceLabels.PRESETNAMELABEL, newname)
                    showErrorDialog(context, resources.getString(R.string.sduplicatedname),
                        msg)
                    dialog.dismiss()
                    return@setPositiveButton
                }
                MainActivity.mConfigParams.savePresetAs(newname, mEventButtons, mControlBars)
                (context as MainActivity).mExploreScreen.updateData()
                dialog.dismiss()
                Toast.makeText(context, resources.getString(R.string.spresetsaved)
                    .replace(ReplaceLabels.PRESETNAMELABEL, newname),
                    ConfigParams.TOASTLENGTH).show()
            }
            .setNegativeButton(R.string.scancel){_,_->
                return@setNegativeButton
            }
            .show()
    }
    private fun onCalibrationClick (){
        (context as MainActivity).changeView((context as MainActivity).mTouchCalibration)
    }

    fun haveMIDI (status: Boolean){
        mHaveMIDIcheck.isChecked = status
    }

    fun haveClock (status: Boolean){
        mHaveClockCheck.isChecked = status
    }

    fun setPresetName (name: String){
        mCurrPresetLabel.text = name
    }

}
