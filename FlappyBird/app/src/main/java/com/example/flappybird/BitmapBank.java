package com.example.flappybird;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapBank {

    Bitmap background_game;
    Bitmap[] bird;

    public BitmapBank(Resources resources) {
        background_game = BitmapFactory.decodeResource(resources, R.drawable.background_game);
        background_game = scaleImage(background_game);

        bird = new Bitmap[4];
        bird[0] = BitmapFactory.decodeResource(resources, R.drawable.avatar);
        bird[1] = BitmapFactory.decodeResource(resources, R.drawable.avatar);
        bird[2] = BitmapFactory.decodeResource(resources, R.drawable.avatar);
        bird[3] = BitmapFactory.decodeResource(resources, R.drawable.avatar);
//        bird[1] = BitmapFactory.decodeResource(resources, R.drawable.bird_frame2);
//        bird[2] = BitmapFactory.decodeResource(resources, R.drawable.bird_frame3);
//        bird[3] = BitmapFactory.decodeResource(resources, R.drawable.bird_frame4);
    }

    public Bitmap getBird(int frame) {
        return bird[frame];
    }

    public int getBirdWidth(){
        return bird[0].getWidth();
    }

    public int getBirdHeight(){
        return bird[0].getHeight();
    }

    // return background bitmap
    public Bitmap getBackground_game() {
        return background_game;
    }

    // return background width
    public int getBackgroundWidth() {
        return background_game.getWidth();
    }

    // (partially visible method)
    public int getBackgroundHeight() {
        return background_game.getHeight();
    }

    public Bitmap scaleImage(Bitmap bitmap) {
        float widthHeightRatio = (float) getBackgroundWidth() / getBackgroundHeight();

        // Then call createScaledBitmap() to create a new bitmap, scaled from an existing bitmap, when possible
        int backgroundScaleWidth = (int) (widthHeightRatio * AppConstants.SCREEN_HEIGHT);

        return Bitmap.createScaledBitmap(bitmap, backgroundScaleWidth, AppConstants.SCREEN_HEIGHT, false);
    }
}





