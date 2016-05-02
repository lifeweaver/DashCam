package com.stardecimal.dashcam.app

import android.os.Handler
import android.widget.EditText

/**
 * Created by LifeWeaver on 12/15/2015.
 */
public class Timer implements Runnable {
    def videoTime
    def timerHandler
    def currentTime = 0

    public Timer(EditText editText, Handler handler) {
        videoTime = editText
        timerHandler = handler
    }

    @Override
    public void run() {
        currentTime += 1
        // TODO: Add time to video so it can be seen when watching it.
        videoTime.text = "${(currentTime / 60).intValue().toString().padLeft(2, '0')}:${(currentTime % 60).toString().padLeft(2, '0')}"
        timerHandler.postDelayed(this, 1000)
    }

    public void reset() {
        currentTime = 0
        videoTime.text = "00:00"
    }
}