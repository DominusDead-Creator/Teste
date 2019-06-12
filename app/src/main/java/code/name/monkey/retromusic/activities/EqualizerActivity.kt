package code.name.monkey.retromusic.activities

import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.MaterialValueHelper
import code.name.monkey.appthemehelper.util.TintHelper
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.activities.base.AbsMusicServiceActivity
import code.name.monkey.retromusic.extensions.applyToolbar
import code.name.monkey.retromusic.helper.EqualizerHelper
import code.name.monkey.retromusic.util.ViewUtil
import kotlinx.android.synthetic.main.activity_equalizer.*
import kotlinx.android.synthetic.main.activity_equalizer.appBarLayout
import kotlinx.android.synthetic.main.activity_equalizer.toolbar
import kotlinx.android.synthetic.main.activity_playing_queue.*

/**
 * @author Hemanth S (h4h13).
 */

class EqualizerActivity : AbsMusicServiceActivity(), AdapterView.OnItemSelectedListener {

    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                if (seekBar === bassBoostStrength) {
                    bassBoost.isEnabled = progress > 0
                    EqualizerHelper.instance!!.bassBoostStrength = progress
                    EqualizerHelper.instance!!.isBassBoostEnabled = progress > 0
                } else if (seekBar === virtualizerStrength) {
                    virtualizer.isEnabled = progress > 0
                    EqualizerHelper.instance!!.isVirtualizerEnabled = progress > 0
                    EqualizerHelper.instance!!.virtualizerStrength = progress
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }
    }

    private var presetsNamesAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)

        setStatusbarColorAuto()
        setNavigationbarColorAuto()
        setTaskDescriptionColorAuto()
        setLightNavigationBar(true)

        setupToolbar()

        equalizerSwitch.isChecked = EqualizerHelper.instance!!.equalizer.enabled
        equalizerSwitch.setBackgroundColor(ThemeStore.accentColor(this))
        val widgetColor = MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(ThemeStore.accentColor(this)))
        equalizerSwitch.setTextColor(widgetColor)
        TintHelper.setTintAuto(equalizerSwitch, widgetColor, false)
        equalizerSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            when (buttonView.id) {
                R.id.equalizerSwitch -> {
                    EqualizerHelper.instance!!.equalizer.enabled = isChecked
                    TransitionManager.beginDelayedTransition(content)
                    content.visibility = if (isChecked) View.VISIBLE else View.GONE
                }
            }
        }

        presetsNamesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        presets.adapter = presetsNamesAdapter
        presets.onItemSelectedListener = this

        bassBoostStrength.progress = EqualizerHelper.instance!!.bassBoostStrength
        ViewUtil.setProgressDrawable(bassBoostStrength, ThemeStore.accentColor(this))
        bassBoostStrength.setOnSeekBarChangeListener(seekBarChangeListener)

        virtualizerStrength.progress = EqualizerHelper.instance!!.virtualizerStrength
        ViewUtil.setProgressDrawable(virtualizerStrength, ThemeStore.accentColor(this))
        virtualizerStrength.setOnSeekBarChangeListener(seekBarChangeListener)

        setupUI()
        addPresets()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar() {
        val primaryColor = ThemeStore.primaryColor(this)
        appBarLayout.setBackgroundColor(primaryColor)
        applyToolbar(toolbar)
    }

    private fun addPresets() {
        presetsNamesAdapter!!.clear()
        presetsNamesAdapter!!.add("Custom")
        for (j in 0 until EqualizerHelper.instance!!.equalizer.numberOfPresets) {
            presetsNamesAdapter!!
                    .add(EqualizerHelper.instance!!.equalizer.getPresetName(j.toShort()))
            presetsNamesAdapter!!.notifyDataSetChanged()
        }
        presets.setSelection(EqualizerHelper.instance!!.equalizer.currentPreset.toInt() + 1)
    }

    private fun setupUI() {
        frequencyBands.removeAllViews()
        val bands: Short
        try {
            // get number of supported bands
            bands = EqualizerHelper.instance!!.numberOfBands.toShort()

            // for each of the supported bands, we will set up a slider from -10dB to 10dB boost/attenuation,
            // as well as text labels to assist the user
            for (i in 0 until bands) {

                val view = LayoutInflater.from(this).inflate(R.layout.retro_seekbar, frequencyBands, false)
                val freqTextView = view.findViewById<TextView>(R.id.hurtz)
                freqTextView.text = String.format("%d Hz", EqualizerHelper.instance!!.getCenterFreq(i) / 1000)

                val minDbTextView = view.findViewById<TextView>(R.id.minus_db)
                minDbTextView.text = String.format("%d dB", EqualizerHelper.instance!!.bandLevelLow / 100)

                val maxDbTextView = view.findViewById<TextView>(R.id.plus_db)
                maxDbTextView.text = String.format("%d dB", EqualizerHelper.instance!!.bandLevelHigh / 100)

                val bar = view.findViewById<SeekBar>(R.id.seekbar)
                ViewUtil.setProgressDrawable(bar, ThemeStore.accentColor(this))
                bar.max = EqualizerHelper.instance!!.bandLevelHigh - EqualizerHelper.instance!!
                        .bandLevelLow
                bar.progress = EqualizerHelper.instance!!.getBandLevel(i) - EqualizerHelper.instance!!
                        .bandLevelLow
                bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        EqualizerHelper.instance!!.setBandLevel(i,
                                progress + EqualizerHelper.instance!!.bandLevelLow)
                        if (fromUser) {
                            presets.setSelection(0)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })

                frequencyBands.addView(view)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (position == 0) {
            return
        }
        EqualizerHelper.instance!!.equalizer.usePreset((position - 1).toShort())
        setupUI()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }
}
