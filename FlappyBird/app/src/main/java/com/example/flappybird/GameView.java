package com.example.flappybird;

import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    GameThread gameThread;

    public GameView(Context context) {
        super(context);
        InitView();
    }

    void InitView(){
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);

        gameThread = new GameThread(holder);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        // Start your game thread here
        if (!gameThread.isRunning()){
            gameThread = new GameThread(surfaceHolder);
            gameThread.start();
        } else {
            gameThread.start();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        if(gameThread.isRunning()) {
            gameThread.setRunning(false);
            boolean retry = true;
            while (retry) {
                try {
                    gameThread.join();
                    retry = false;
                } catch (InterruptedException e) {}
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        // TAP IS DETECTED
        // SE DETECTA EL TOQUE
        if (action == MotionEvent.ACTION_DOWN) {
            AppConstants.getGameEngine().gameState = 1;
            AppConstants.getGameEngine().bird.setVelocity(AppConstants.VELOCITY_WHEN_JUMPED);
        }
        return true;
    }
}