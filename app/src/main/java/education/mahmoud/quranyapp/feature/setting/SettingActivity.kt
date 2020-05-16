package education.mahmoud.quranyapp.feature.setting

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.Repository
import kotlinx.android.synthetic.main.activity_setting.*
import org.koin.android.ext.android.inject
import java.util.*

class SettingActivity : AppCompatActivity() {

    var colorPosForBackground = 0

    val repository: Repository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        ButterKnife.bind(this)
        val colorSet: List<String> = ArrayList(listOf(getString(R.string.white), getString(R.string.yellow), getString(R.string.green)))
        cbNightMode.isChecked = repository.nightModeState

        if (cbNightMode.isChecked) {
            nightMode()
        } else {
            defaultMode()
        }

        cbNightMode.setOnCheckedChangeListener { compoundButton, state ->
            Log.d(TAG, "onCheckedChanged: $state")
            repository.nightModeState = state
            if (state) {
                nightMode()
            } else {
                defaultMode()
            }

        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, colorSet)
        spColorReqularMode.adapter = adapter
        // load from shared preference and set to spinner.
        colorPosForBackground = repository.backColorState
        spColorReqularMode.setSelection(colorPosForBackground)
        spColorReqularMode.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                Log.d(TAG, "onItemSelected: $i:: $l")
                val pos = spColorReqularMode.selectedItemPosition
                Log.d(TAG, "onItemSelected: pos $pos")
                colorPosForBackground = pos
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        title = getString(R.string.setting)

        btnSetColor.setOnClickListener {
            repository.backColorState = colorPosForBackground
            showMessage(getString(R.string.setting_updated))
        }
    }

    private fun defaultMode() {
        linearColor.visibility = View.VISIBLE
    }

    private fun nightMode() {
        linearColor.visibility = View.GONE
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "SettingActivity"
    }
}