package education.mahmoud.quranyapp.feature.setting

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.databinding.ActivitySettingBinding
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.utils.viewBinding
import org.koin.android.ext.android.inject

class SettingActivity : AppCompatActivity() {

    var colorPosForBackground = 0

    val quranRepository: QuranRepository by inject()

    private val binding by viewBinding(ActivitySettingBinding::inflate)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            val colorSet: List<String> = ArrayList(
                listOf(
                    getString(R.string.white),
                    getString(R.string.yellow),
                    getString(R.string.green)
                )
            )
            cbNightMode.isChecked = quranRepository.nightModeState

            if (cbNightMode.isChecked) {
                nightMode()
            } else {
                defaultMode()
            }

            cbNightMode.setOnCheckedChangeListener { compoundButton, state ->
                Log.d(TAG, "onCheckedChanged: $state")
                quranRepository.nightModeState = state
                if (state) {
                    nightMode()
                } else {
                    defaultMode()
                }
            }
            val adapter = ArrayAdapter(
                this@SettingActivity,
                android.R.layout.simple_dropdown_item_1line,
                colorSet
            )
            spColorReqularMode.adapter = adapter
            // load from shared preference and set to spinner.
            colorPosForBackground = quranRepository.backColorState
            spColorReqularMode.setSelection(colorPosForBackground)
            spColorReqularMode.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View,
                    i: Int,
                    l: Long,
                ) {
                    Log.d(TAG, "onItemSelected: $i:: $l")
                    val pos = spColorReqularMode.selectedItemPosition
                    Log.d(TAG, "onItemSelected: pos $pos")
                    colorPosForBackground = pos
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
            title = getString(R.string.setting)

            btnSetColor.setOnClickListener {
                quranRepository.backColorState = colorPosForBackground
                showMessage(getString(R.string.setting_updated))
            }
        }
    }

    private fun defaultMode() {
        binding.linearColor.visibility = View.VISIBLE
    }

    private fun nightMode() {
        binding.linearColor.visibility = View.GONE
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "SettingActivity"
    }
}
