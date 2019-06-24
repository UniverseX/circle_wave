package com.autoai.circlewave.effects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

public interface Effect {

    void draw(Canvas canvas) throws Exception;

    void setByte(byte[] bytes);

    void setSurfaceRectF(RectF rectF);

    void setBitmap(Bitmap bitmap);

    Bitmap blurBg(Bitmap bg);

    Bitmap clipCircle(Bitmap bg);

    void invalidate();

    void copyFrom(Effect effect);
}
