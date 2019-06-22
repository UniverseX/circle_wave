package com.autoai.circlewave.effects;

import android.graphics.Canvas;

public abstract class BaseEffect implements Effect {
    protected abstract void blurBg();

    protected abstract void clipCircle();

    public abstract void onDraw(Canvas canvas) throws Exception;

    @Override
    public void draw(Canvas canvas) throws Exception {
        clipCircle();
        blurBg();
        onDraw(canvas);
    }
}
