package com.autoai.circlewave.effects;

import android.graphics.Canvas;
import android.graphics.RectF;

public interface Effect {

    void draw(Canvas canvas) throws Exception;

    void setByte(byte[] bytes);

    void setSurfaceRectF(RectF rectF);
}
