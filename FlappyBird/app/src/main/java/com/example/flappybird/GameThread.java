package com.example.flappybird;

import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameThread extends Thread {

    SurfaceHolder surfaceHolder;
    // Flag to detect whether the thread is running or not
    boolean isRunning;

    // Loop start time and loop duration
    long startTime, loopTime;

    // Delay in milliseconds between screen refreshes
    long DELAY = 33;

    public GameThread(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        isRunning = true;

    }

    @Override
    public void run() {
        while (isRunning) {
            startTime = SystemClock.uptimeMillis();
            Canvas canvas = surfaceHolder.lockCanvas(null);
            if(canvas != null) {
                synchronized (surfaceHolder){
                    AppConstants.getGameEngine().updateAndDrawableBackgroundImage(canvas);
                    AppConstants.getGameEngine().updateAndDrawBird(canvas);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

            loopTime = SystemClock.uptimeMillis() - startTime;
            if (loopTime < DELAY) {
                try {
                    Thread.sleep(DELAY - loopTime);
                } catch (InterruptedException e) {
                    Log.e("Interrupted", "Interrupted while sleeping");
                }
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean state) {
        isRunning = state;
    }
}
