package com.example.cpu02351_local.recorderview

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView

class HoldForActionButton @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_DELAY = 100 // ms
    }

    private var holdDelay = DEFAULT_DELAY
    private var startTime = -1L
    private var startX = -1
    private var startY = -1
    private var listeners = mutableListOf<ActionListener>()

    fun addActionListener(listener: ActionListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeActionListener(listener: ActionListener) {
        listeners.remove(listener)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.actionMasked ?: return super.onTouchEvent(event)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX.toInt()
                startY = event.rawY.toInt()
                startTime = System.currentTimeMillis()
                return true
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL -> {
                val curTime = System.currentTimeMillis()
                if (!isHolding && curTime - startTime > holdDelay) {
                    notifyStart()
                } else {
                    notifyMovement(startX, startY, event.rawX.toInt(), event.rawY.toInt())
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isHolding) {
                    notifyEnd()
                } else {
                    notifyClick()
                }
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private var isHolding = false

    private fun notifyClick() {
        listeners.forEach {
            it.onClick()
        }
    }

    private fun notifyStart() {
        isHolding = true
        startTimer()
        listeners.forEach {
            it.onActionStarted()
        }
    }

    private val mHandler = Handler()

    private val task = object : Runnable {
        override fun run() {
            notifyHold(System.currentTimeMillis() - startTime)
            mHandler.postDelayed(this, 500)
        }
    }

    private fun startTimer() {
        mHandler.post(task)
    }

    private fun notifyEnd() {
        resetState()
        stopTimer()
        listeners.forEach {
            it.onActionEnded()
        }
    }

    private fun stopTimer() {
        mHandler.removeCallbacks(task)
    }

    private fun notifyHold(totalTimeMilli: Long) {
        listeners.forEach {
            it.onHold(totalTimeMilli)
        }
    }

    private fun notifyMovement(startX: Int, startY: Int, currentX: Int, currentY: Int) {
        listeners.forEach {
            it.onMovement(startX, startY, currentX, currentY)
        }
    }

    private fun resetState() {
        isHolding = false
    }


    interface ActionListener {
        fun onActionStarted()
        fun onActionEnded()
        fun onHold(totalTimeMilli: Long)
        fun onMovement(startX: Int, startY: Int, currentX: Int, currentY: Int)
        fun onClick()
    }
}