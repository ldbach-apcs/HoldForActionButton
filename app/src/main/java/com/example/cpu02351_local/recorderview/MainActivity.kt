package com.example.cpu02351_local.recorderview

import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.TextView
import android.widget.Toast
import java.io.File

class MainActivity : AppCompatActivity(), HoldForActionButton.ActionListener {
    override fun onHold(totalTimeMilli: Long) {
        updateProgress(totalTimeMilli)
    }

    override fun onActionStarted() {
        startRecorder()
    }

    override fun onActionEnded() {
        stopRecorder()
    }

    override fun onMovement(startX: Int, startY: Int, currentX: Int, currentY: Int) {
        val difX = currentX - startX
        val difY = currentY - startY
        handleMovement(difX, difY)
    }

    override fun onClick() {
        presentHint()
    }

    override fun onStart() {
        super.onStart()
        holdForAction.addActionListener(this)
    }

    override fun onStop() {
        super.onStop()
        holdForAction.removeActionListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaRecorder.release()
    }

    private lateinit var progressDisplayer : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        holdForAction = findViewById(R.id.holdForActionButton)
        progressDisplayer = findViewById(R.id.progressDisplayer)
    }

    private lateinit var holdForAction: HoldForActionButton
    private var fileNum = 1
    private fun getFileName(): String = "Hello_world_" + (fileNum++).toString()
    private var mMediaRecorder = MediaRecorder()
    private val maxDuration = 60000
    private lateinit var mAudioFile: File
    private var pathPrefix =
            Environment.getExternalStorageDirectory().absolutePath +
                    "/AwesomeChat3/Audio/"

    private fun presentHint() {
        Toast.makeText(this, "Press and hold the button to record audio", Toast.LENGTH_SHORT).show()
    }

    private fun initRecorder() {
        mMediaRecorder.setMaxDuration(maxDuration)
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        configureOutputPath()
        mMediaRecorder.setOutputFile(mAudioFile.absolutePath)
        mMediaRecorder.prepare()
    }

    private fun configureOutputPath() {
        val storagePath = File(pathPrefix)
        if (!storagePath.exists()) {
            storagePath.mkdirs()
        }
        mAudioFile = File(pathPrefix + getFileName())
    }

    private fun startRecorder() {
        initRecorder()
        mMediaRecorder.start()
        progressDisplayer.visibility = VISIBLE
    }

    private fun updateProgress(totalTimeMilli: Long) {
        val progress = (totalTimeMilli / 1000).toString() + " seconds"
        progressDisplayer.text = progress
    }

    private var cancelRecording = false
    private fun handleMovement(difX: Int, difY: Int) {
        holdForAction.translationY = difY.toFloat()
        holdForAction.translationX = difX.toFloat()
        cancelRecording = difY < -20
    }

    private fun stopRecorder() {
        holdForAction.animate()
                .apply {
                    holdForAction.translationX = 0f
                    holdForAction.translationY = 0f
                }
                .start()

        if (cancelRecording) {
            Toast.makeText(this, "Recording cancelled", Toast.LENGTH_SHORT).show()
            if (mAudioFile.exists())
                mAudioFile.delete()
        } else {
            Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show()
        }
        progressDisplayer.visibility = INVISIBLE
        mMediaRecorder.stop()
    }
}
