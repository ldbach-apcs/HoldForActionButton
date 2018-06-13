package com.example.cpu02351_local.recorderview

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import java.io.File
import java.util.*

class SoundRecorderButton @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val MAX_DURATION = 60000 // 1 minute
        private const val DELAY = 100
    }

    private var timeDisplay = "0 second(s)"
    private var timeSuffix = "second(s)"
    private var nameProvider: FileNameProvider = DefaultFileNameProvider()
    private var fileName: String = nameProvider.getName()
    private var pathPrefix =
            Environment.getExternalStorageDirectory().absolutePath +
            "/AwesomeChat/Audio/"
    private lateinit var mMediaRecorder: MediaRecorder


    fun setFileNameProvider(nameProvider: FileNameProvider) {
        if (nameProvider != this.nameProvider) {
            this.nameProvider = nameProvider
            fileName = nameProvider.getName()
        }
    }

    private var isRecording = false
    private var firstPress = -1L

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.actionMasked ?: return super.onTouchEvent(event)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                firstPress = System.currentTimeMillis()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isRecording && System.currentTimeMillis() - firstPress > DELAY) {
                    startRecording()
                } else {
                    updateTime()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isRecording) {
                    saveRecord()
                }
                resetState()
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun updateTime() {
        val secs = (System.currentTimeMillis() - firstPress) / 1000
        timeDisplay = "$secs $timeSuffix"
        Log.d("RECORDING", timeDisplay)
    }

    private fun startRecording() {
        isRecording = true
        mMediaRecorder = MediaRecorder()
        setupRecorder()
        mMediaRecorder.prepare()
        mMediaRecorder.start()
    }

    private lateinit var audioFile: File
    private fun setupRecorder() {
        mMediaRecorder.setMaxDuration(MAX_DURATION)
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        configureOutputPath()
        mMediaRecorder.setOutputFile(audioFile.absolutePath)
    }

    private fun configureOutputPath() {
        val storagePath = File(pathPrefix)
        if (!storagePath.exists()) {
            storagePath.mkdirs()
        }
        audioFile = File(pathPrefix + fileName)
    }

    private fun saveRecord() {
        mMediaRecorder.stop()
        mMediaRecorder.reset()
        mMediaRecorder.release()
    }

    private fun resetState() {
        isRecording = false
    }

    class DefaultFileNameProvider: FileNameProvider {
        override fun getName(): String {
            return UUID.randomUUID().toString() + ".mp3"
        }
    }

    interface FileNameProvider {
        fun getName(): String
    }
}