package com.autoai.circlewave.bean;

import android.graphics.Canvas;

public interface Effect {
    void blurBg();

    void clipCircle();

    void draw(Canvas canvas) throws Exception;

    void setByte(byte[] bytes);

    void onSurfaceChanged(int width, int height);
}
