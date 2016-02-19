package com.busylee.treegame

import android.os.Handler
import android.os.HandlerThread
import java.util.*

/**
 * Created by busylee on 18.02.16.
 */
class ScoreMaster(val mScoreListener: IScoreListener): Observer {

    companion object {
//        val THREAD_NAME = "ScoreMasterTimer"
        val TIMER_INTERVAL = 1000L //1000 ms
    }

    var mTimeLeft: Int = 0
    var mTimer = false

//    var mHandlerThread = HandlerThread(THREAD_NAME)
    var mHandler = Handler()

    private val mTimerRunnable = object : Runnable {
        override fun run() {
            if (mTimer) {
                if (decreaseTime()) {
                    mHandler.postDelayed(this, TIMER_INTERVAL)
                } else {
                    onGameLose()
                }
            }
        }
    }

    fun initTimer(timeLeft: Int) {
        mTimeLeft = timeLeft
    }

    fun startTimer() {
        mTimer = true
        mHandler.postDelayed(mTimerRunnable, TIMER_INTERVAL)
    }

    fun stopTimer() {
        mTimer = false
        mHandler.removeCallbacks(mTimerRunnable)
    }

    fun resetTimer() {
        stopTimer()
    }

    override fun update(observable: Observable?, obj: Any?) {
        if(!decreaseTime()) {
            mScoreListener.onGameLose()
        }
    }

    internal fun decreaseTime(): Boolean {
        if(mTimeLeft > 0) {
            mTimeLeft--
        }
        mScoreListener.onTimeLeftChange(mTimeLeft)
        return mTimeLeft > 0
    }

    internal fun onGameLose() {
        stopTimer()
        mScoreListener.onGameLose()
    }

    interface IScoreListener {
        fun onTimeLeftChange(timeLeft: Int)
        fun onGameLose()
    }
}