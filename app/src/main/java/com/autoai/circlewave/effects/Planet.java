package com.autoai.circlewave.effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.Log;

import java.util.Arrays;

public class Planet extends BaseEffect {
    private static final String TAG = "Planet";
    private Paint mWavePaint;
    private Paint mWavePaint1;
    private Paint mWavePaint2;
    private Paint mWavePaint3;

    private static final float WAVE_DIAMETER_OFFSET_INIT = 30f;
    private static final float WAVE_DIAMETER_OFFSET = 30f;
    private static final float WAVE_OFFSET_ALL = WAVE_DIAMETER_OFFSET * 4;
    private static final float WAVE_STROKE_WIDTH = 2f;
    private float mWaveDiameter;
    private float mWaveDiameter1;
    private float mWaveDiameter2;
    private float mWaveDiameter3;
    private static float sWaveDiameter_init;
    private static float sWaveDiameter_end;

    public Planet(Context context, Bitmap bg) {
        super(context, bg);

        sWaveDiameter_init = mCircleDiameter + density * WAVE_DIAMETER_OFFSET_INIT;
        sWaveDiameter_end = sWaveDiameter_init + density * WAVE_OFFSET_ALL;
        mWaveDiameter = sWaveDiameter_init;
        mWaveDiameter1 = sWaveDiameter_init;
        mWaveDiameter2 = sWaveDiameter_init;
        mWaveDiameter3 = sWaveDiameter_init;

        float mWaveStroke = density * WAVE_STROKE_WIDTH;

        mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG /*| Paint.FILTER_BITMAP_FLAG*/);
        mWavePaint.setStrokeWidth(mWaveStroke);
        mWavePaint.setStyle(Paint.Style.STROKE);
        mWavePaint.setColor(mainBgColor);
        mWavePaint.setAlpha(0xff);

        mWavePaint1 = new Paint(mWavePaint);
        mWavePaint1.setAlpha(0xbb);

        mWavePaint2 = new Paint(mWavePaint);
        mWavePaint2.setAlpha(0x88);

        mWavePaint3 = new Paint(mWavePaint);
        mWavePaint3.setAlpha(0x44);

    }

    @Override
    public void onDraw(Canvas canvas) throws Exception {
        //clear
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (bg == null) {
            return;
        }
        //draw bg
        canvas.drawBitmap(bg, null, surfaceRect, bgPaint);

        //draw wave
        drawWave(canvas);

        //draw circle
        canvas.drawBitmap(circle, mCircleMatrix, mCirclePaint);
        mCircleMatrix.postRotate(SPEED_ROTATE, surfaceRect.width() / 2f, surfaceRect.height() / 2f);
    }

    private PointF p0 = new PointF();
    private PointF p1 = new PointF();
    private PointF p2 = new PointF();
    private PointF p3 = new PointF();
    private Path path = new Path();
    private Matrix matrix = new Matrix();
    private float[] values = new float[9];
    private float currAlpha = 0xff;

    private void drawWave(Canvas canvas) {
        float centerX = (surfaceRect.right + surfaceRect.left) / 2;
        float centerY = (surfaceRect.bottom + surfaceRect.top) / 2;


        if (mWaveDiameter >= sWaveDiameter_end) {
            mWaveDiameter = sWaveDiameter_init;
        }

        canvas.drawCircle(centerX, centerY, mWaveDiameter / 2, mWavePaint);
        mWaveDiameter += 2f;

        Log.d(TAG, "drawWave: m = " + mWaveDiameter);
        if ((mWaveDiameter < (sWaveDiameter_init + density * WAVE_DIAMETER_OFFSET)
                && mWaveDiameter1 == sWaveDiameter_init) || mWaveDiameter1 > sWaveDiameter_end) {
            mWaveDiameter1 = sWaveDiameter_init;
        } else {
            canvas.drawCircle(centerX, centerY, mWaveDiameter1 / 2, mWavePaint);
            mWaveDiameter1 += 2f;
        }

        Log.d(TAG, "drawWave: m1 = " + mWaveDiameter1);

        if ((mWaveDiameter1 < (sWaveDiameter_init + density * WAVE_DIAMETER_OFFSET)
                && mWaveDiameter2 == sWaveDiameter_init) || mWaveDiameter2 > sWaveDiameter_end) {
            mWaveDiameter2 = sWaveDiameter_init;
        } else {
            canvas.drawCircle(centerX, centerY, mWaveDiameter2 / 2, mWavePaint);
            mWaveDiameter2 += 2f;
        }

        Log.d(TAG, "drawWave: m2 = " + mWaveDiameter2);

        if ((mWaveDiameter2 < (sWaveDiameter_init + density * WAVE_DIAMETER_OFFSET)
                && mWaveDiameter3 == sWaveDiameter_init)|| mWaveDiameter3 > sWaveDiameter_end) {
            mWaveDiameter3 = sWaveDiameter_init;
        } else {
            canvas.drawCircle(centerX, centerY, mWaveDiameter3 / 2, mWavePaint);
            mWaveDiameter3 += 2f;
        }

        Log.d(TAG, "drawWave: m3 = " + mWaveDiameter3);

        Log.d(TAG, "drawWave: -------------------------");
    }
}
