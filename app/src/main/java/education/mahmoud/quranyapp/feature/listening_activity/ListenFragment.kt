package education.mahmoud.quranyapp.feature.listening_activity

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.jakewharton.rxrelay2.PublishRelay
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.DataLoadingBaseFragment
import education.mahmoud.quranyapp.datalayer.Repository
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.datalayer.local.room.SuraItem
import education.mahmoud.quranyapp.feature.home_Activity.HomeActivity
import education.mahmoud.quranyapp.utils.NumberHelper
import education.mahmoud.quranyapp.utils.Util
import education.mahmoud.quranyapp.utils.log
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_listen.*
import org.koin.android.ext.android.inject
import java.io.IOException
import java.text.MessageFormat
import java.util.*

class ListenFragment : DataLoadingBaseFragment(), OnDownloadListener {

    private var serviceIntent: Intent? = null
    val relay = PublishRelay.create<Boolean>()

    var mediaPlayer: MediaPlayer? = null

    var url = "http://cdn.alquran.cloud/media/audio/ayah/ar.alafasy/"
    var isPermissionAllowed = false
    var downloadID = 0
    var i = 1
    var startSura: SuraItem = SuraItem()
    var endSura: SuraItem = SuraItem()
    var downURL: String? = null
    var path: String? = null
    var filename: String? = null
    var index = 0
    var currentAyaAtAyasToListen = 0
    var fileSource: String? = null
    var ayahsToListen = listOf<AyahItem>()
    var actualStart = 0
    var actualEnd = 0
    var currentIteration = 0
    var endIteration = 0
    private val repository: Repository by inject()
    private var ayahsToDownLoad = listOf<AyahItem>()
    private var ayahsRepeatCount = 0
    private var ayahsSetCount = 0

    // </editor-fold>
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_listen, container, false)
    }

    override fun initViews(view: View) {
        super.initViews(view)
        isPermissionAllowed = repository.permissionState
        initSpinners()
    }

    override fun startObserving() {
        super.startObserving()

        relay.subscribe({
            mediaPlayer?.let { mediaPlayer ->
                tvProgressAudio.text = getString(R.string.time_progress, mediaPlayer.currentPosition / 1000, mediaPlayer.duration / 1000)
                sbPosition.progress = mediaPlayer.currentPosition
            }
        }, {
        }).addTo(bg)
    }

    private fun initSpinners() {
        val suraNames = repository.surasNames
        val startAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suraNames)
        spStartSura.adapter = startAdapter
        val endAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suraNames)
        spEndSura.adapter = endAdapter
    }

    override fun setClickListeners() {
        super.setClickListeners()
        spStartSura.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, index: Long) {
                try {
                    startSura = repository.getSuraByIndex(index + 1)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
            }
        }
        spEndSura.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, index: Long) {
                try {
                    endSura = repository.getSuraByIndex(index + 1)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        btnStartListening.setOnClickListener {
            startListening()
        }
    }

    // <editor-fold desc="downolad">
    private fun downloadAudio() { // compute index
        index = ayahsToDownLoad[currentIteration].ayahIndex
        // form  URL
        downURL = url + index
        // form path
        path = Util.getDirectoryPath() // get folder path
        // form file name
        filename = "$index.mp3"
        Log.d(TAG, "downloadAudio:  file name $filename")
        // start downloading
        PRDownloader.download(downURL, path, filename).build().start(this)
        // set text on screen downloaded / todownled
        // second is show name of current file to download
        tvDownCurrentFile.text = getString(R.string.now_down, filename)
        tvDownStatePercentage.text = getString(R.string.downState, currentIteration, endIteration)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.d(TAG, "setUserVisibleHint: ")
        if (isVisibleToUser && lnPlayView != null) {
            initSpinners()
            backToSelectionState()
        }
    }

    // <editor-fold desc="download ">
    override fun onDownloadComplete() {
        Log.d(TAG, "onDownloadComplete: ")
        // store storage path in db to use in media player
        val ayahItem = repository.getAyahByIndex(index) // first get ayah to edit it with storage path
        ayahItem.let {
            val storagePath = "$path/$filename"
            ayahItem.audioPath = storagePath // set path
            repository.updateAyahItem(ayahItem)
            // update currentIteration to indicate complete of download
            currentIteration++
            Log.d(TAG, "onDownloadComplete:  end $endIteration")
            Log.d(TAG, "onDownloadComplete:  current $currentIteration")
            if (currentIteration < endIteration) { // still files to download
                downloadAudio()
            } else { // here i finish download all ayas
                // start to display
                finishDownloadState()
                displayAyasState()
            }
        }
    }

    override fun onError(error: Error) {
        if (error.isConnectionError) {
            showMessage(getString(R.string.error_net))
        } else if (error.isServerError) {
            showMessage("Server error")
        } else {
            showMessage("Error $error")
        }
        lnDownState.visibility = View.GONE
        backToSelectionState()
    }

    // </editor-fold>
    private fun finishDownloadState() {
        showMessage(getString(R.string.finish))
        btnStartListening.visibility = View.VISIBLE
        lnDownState.visibility = View.GONE
    }

    private fun displayAyasState() {
        Log.d(TAG, "display Ayas State: ")
        currentAyaAtAyasToListen = 0
        // first reload ayahs from db
        ayahsToListen = repository.getAyahSInRange(actualStart + 1, actualEnd + 1)
        // repeation formation
        ayahsToListen = getAyahsEachOneRepreated(ayahsRepeatCount)
        ayahsToListen = getAllAyahsRepeated(ayahsSetCount)
        // control visibility
        lnSelectorAyahs.visibility = View.GONE
        lnPlayView.visibility = View.VISIBLE
        btnPlayPause.setBackgroundResource(R.drawable.ic_pause)

        // TODO: 6/30/2019  bind service with this.
        displayAyahs()

        val ayahsListen = AyahsListen()
        ayahsListen.ayahItemList = ayahsToListen

        /*  // delay work with service
           if (serviceIntent != null) {
              activity?.stopService(serviceIntent)
          }
          serviceIntent = ListenServie.createService(context, ayahsListen)*/

        dismissKeyboard()
    }

    private fun getAllAyahsRepeated(ayahsSetCount: Int): List<AyahItem> {
        val ayahItems = mutableListOf<AyahItem>()
        for (i in 0 until ayahsSetCount) {
            ayahItems.addAll(ayahsToListen)
        }
        Log.d(TAG, "getAllAyahsRepeated: " + ayahItems.size)
        return ayahItems
    }

    private fun getAyahsEachOneRepreated(ayahsRepeatCount: Int): List<AyahItem> {
        val ayahItems: MutableList<AyahItem> = ArrayList()
        for (ayahItem in ayahsToListen) {
            for (j in 0 until ayahsRepeatCount) {
                ayahItems.add(ayahItem)
            }
        }
        return ayahItems
    }

    private fun logAyahs() {
        for (ayahItem in ayahsToListen) {
            Log.d(TAG, "logAyahs: " + ayahItem.ayahIndex + ayahItem.text)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun displayAyahs() {
        Log.d(TAG, "displayAyahs: $currentAyaAtAyasToListen")
        val ayahItem = ayahsToListen[currentAyaAtAyasToListen]
        tvAyahToListen.text = MessageFormat.format("{0}   ﴿{1}﴾  ", ayahItem.text, NumberHelper.getArabicNumber(ayahItem.ayahInSurahIndex))
        playAudio()
    }


    fun onBtnPlayPauseClicked() {
        Log.d(TAG, "onBtnPlayPauseClicked: ")
        mediaPlayer?.let {
            val mp = it
            if (!mp.isPlaying) {
                mp.start()
                Log.d(TAG, "onBtnPlayPauseClicked: ")
                btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
            } else {
                btnPlayPause.setBackgroundResource(R.drawable.ic_play)
                mp.pause()
            }
        }
    }

    fun startListening() {
        ayahsToListen = ArrayList()
        //region check inputs
        if (startSura.name.isNotEmpty() && endSura.name.isNotEmpty()) {
            try {
                val start: Int = edStartSuraAyah.text.toString().toInt()
                if (start > startSura.numOfAyahs) {
                    edStartSuraAyah.error = getString(R.string.outofrange, startSura.numOfAyahs)
                    return
                }
                val end: Int = edEndSuraAyah.text.toString().toInt()
                if (end > endSura.numOfAyahs) {
                    edEndSuraAyah.error = getString(R.string.outofrange, endSura.numOfAyahs)
                    return
                }
                // compute actual start
                actualStart = repository.getAyahByInSurahIndex(startSura.index, start).ayahIndex - 1
                // compute actual end
                actualEnd = repository.getAyahByInSurahIndex(endSura.index, end).ayahIndex - 1
                // check actualstart & actualEnd
                if (actualEnd < actualStart) {
                    makeRangeError()
                    return
                }
                Log.d(TAG, "onViewClicked: actual $actualStart $actualEnd")
                ayahsSetCount = try {
                    edRepeatSet.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    1
                }
                ayahsRepeatCount = try {
                    edRepeatAyah.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    1
                }
                // get ayahs from db,
                // actual end is updated with one as query return result excluded one item
                ayahsToListen = repository.getAyahSInRange(actualStart + 1, actualEnd + 1)
                Log.d(TAG, "onViewClicked: start log after first select " + ayahsToListen.size)
                // logAyahs()
                ayahsToDownLoad = ayahsToListen.filter { it.audioPath == null }

                // close keyboard
                closeKeyboard()
                checkAyahsToDownloadIt()
            } catch (e: NumberFormatException) {
                makeRangeError()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            showMessage(getString(R.string.sura_select_error))
        }
        //endregion
    }

    private fun playAudio() {
        Log.d(TAG, "playAudio:  current $currentAyaAtAyasToListen")
        btnPlayPause.isEnabled = false
        try {
            mediaPlayer = MediaPlayer()
            fileSource = ayahsToListen[currentAyaAtAyasToListen].audioPath
            mediaPlayer?.setDataSource(fileSource)
            Log.d(TAG, "playAudio: file source $fileSource")
            mediaPlayer?.prepare()
            mediaPlayer?.setOnPreparedListener { mediaPlayer -> mediaPlayer.start() }
            sbPosition.max = mediaPlayer!!.duration

            mediaPlayer?.let {
                "update ".log(TAG)
                relay.accept(true)
            }

            sbPosition.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                    if (b) {
                        if (mediaPlayer != null) {
                            mediaPlayer?.seekTo(progress)
                            sbPosition.progress = progress
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
            mediaPlayer?.setOnCompletionListener {
                mediaPlayer?.release()
                mediaPlayer = null
                btnPlayPause.isEnabled = true
                currentAyaAtAyasToListen++
                if (currentAyaAtAyasToListen < ayahsToListen.size) {
                    Log.d(TAG, "@@  onCompletion: $currentAyaAtAyasToListen")
                    displayAyahs()
                } else {
                    actualStart = -1
                    actualEnd = -1
                    backToSelectionState()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            showMessage(getString(R.string.error))
            backToSelectionState()
        }
    }

    private fun closeKeyboard() {
        edEndSuraAyah.clearFocus()
        edStartSuraAyah.clearFocus()
        lnPlayView.requestFocus()
        //    Util.hideInputKeyboard(getContext());
    }

    private fun makeRangeError() {
        edStartSuraAyah.error = "Start must be before end "
        edEndSuraAyah.error = "End must be after start"
    }

    private fun checkAyahsToDownloadIt() {
        Log.d(TAG, "checkAyahsToDownloadIt: " + ayahsToDownLoad.size)
        currentIteration = 0
        if (ayahsToDownLoad.isNotEmpty()) {
            endIteration = ayahsToDownLoad.size
            downloadAyahs()
        } else {
            displayAyasState()
        }
    }

    fun downloadAyahs() {
        Log.d(TAG, "downloadAyahs: ")
        if (!isPermissionAllowed) {
            (activity as HomeActivity?)?.acquirePermission()
        } else {
            downloadState()
            downloadAudio()
        }

    }

    private fun downloadState() {
        showMessage(getString(R.string.downloading))
        btnStartListening.visibility = View.GONE
        lnDownState.visibility = View.VISIBLE
    }

    private fun backToSelectionState() {
        if (mediaPlayer != null) {
            mediaPlayer?.release()
        }
        // control visibility
        lnPlayView.visibility = View.GONE
        lnSelectorAyahs.visibility = View.VISIBLE
        btnStartListening.visibility = View.VISIBLE
        lnDownState.visibility = View.GONE
        // clear inputs
        edEndSuraAyah.text = null
        edStartSuraAyah.text = null
        edEndSuraAyah.error = null
        edStartSuraAyah.error = null
        edRepeatAyah.text = null
        edRepeatSet.text = null
    }

    companion object {
        private const val TAG = "ListenFragment"
    }
}