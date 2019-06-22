package com.autoai.circlewave.effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

public abstract class BaseEffect implements Effect {
    protected Bitmap bg;
    protected boolean hasProcessBg;
    private boolean needDraw;

    public BaseEffect(Context context, Bitmap bg) {
        this.bg = bg;
    }

    protected abstract void blurBg();

    protected abstract void clipCircle();

    public abstract void onDraw(Canvas canvas) throws Exception;

    @Override
    public void draw(Canvas canvas) throws Exception {
        if(!hasProcessBg) {
            clipCircle();
            blurBg();
            hasProcessBg = true;
            needDraw = true;
        }
        if(needDraw) {
            onDraw(canvas);
        }
    }

    public void setBitmap(Bitmap bg){
        this.bg = bg;
        hasProcessBg = false;
        needDraw = false;
    }
}
