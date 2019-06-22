package com.autoai.circlewave.effects;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

/**
 *
 */
public class DynamicScale extends BaseEffect {
    private static final String TAG = "DynamicScale";

    private static final float WAVE_DIAMETER_OFFSET = 35f;
    private static final int WAVE_DIVISIONS = 100;
    private final Paint mWavePaint;
    private final Paint mFadePaint;
    private float mWaveDiameter;
    private float[] mFFTPoints;
    private PointF[] mCirclePoints = new PointF[WAVE_DIVISIONS];

    public DynamicScale(Context context, Bitmap bg) {
        super(context, bg);
        mWaveDiameter = mCircleDiameter + density * WAVE_DIAMETER_OFFSET;
        float mWaveStroke = (float) (Math.PI * mWaveDiameter / WAVE_DIAMETER_OFFSET / 4f);

        mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG /*| Paint.FILTER_BITMAP_FLAG*/);
        mWavePaint.setStrokeWidth(mWaveStroke);
        mWavePaint.setColor(mainBgColor);

        mFadePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG /*| Paint.FILTER_BITMAP_FLAG*/);
        mFadePaint.setColor(Color.argb(238, 255, 255, 255)); // Adjust alpha to change how quickly the image fades
        mFadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    public void onDraw(Canvas canvas) throws Exception {
        //clear
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //draw bg
        canvas.drawBitmap(bg, null, surfaceRect, bgPaint);

        //draw circle
        canvas.drawBitmap(circle, mCircleMatrix, mCirclePaint);
        mCircleMatrix.postRotate(SPEED_ROTATE, surfaceRect.width() / 2f, surfaceRect.height() / 2f);

        if(mBytes == null){
            return;
        }
        float centerX = surfaceRect.width() / 2f;
        float centerY = surfaceRect.height() / 2f;
        //draw base wave circle
//        canvas.drawCircle(centerX, centerY, mWaveDiameter /2, mBaseWavePaint);

        //draw wave scale(音阶)
        int index = 0;
        for (float i = 0; i < 360f; i += 360f / WAVE_DIVISIONS) {
            float nx = (float) (Math.cos(Math.toRadians(i)) * mWaveDiameter / 2f + centerX);
            float ny = (float) (Math.sin(Math.toRadians(i)) * mWaveDiameter / 2f + centerY);

            if(mCirclePoints[index] == null) {
                mCirclePoints[index] = new PointF(nx, ny);
            }else {
                mCirclePoints[index].x = nx;
                mCirclePoints[index].y = ny;
            }
            index ++;
        }

        int division = mBytes.length / WAVE_DIVISIONS;
        for (int i = 0; i < WAVE_DIVISIONS; i++) {

            mFFTPoints[i * 4] = mCirclePoints[i].x;
            mFFTPoints[i * 4 + 1] = mCirclePoints[i].y;

            byte rfk = mBytes[division * i];//间隔倍数
            byte ifk = mBytes[division * i + 1];

            float magnitude = (rfk * rfk + ifk * ifk);
            float dbValue = (float) (10 * Math.log10(magnitude));

            if(dbValue <= 0){
                dbValue = density * 2;
            }

            float degree = i * 360f / WAVE_DIVISIONS;
            float dx = (float) (Math.cos(Math.toRadians(degree)) * (dbValue));
            float dy = (float) (Math.sin(Math.toRadians(degree)) * (dbValue));

            mFFTPoints[i * 4 + 2] = mCirclePoints[i].x + dx;
            mFFTPoints[i * 4 + 3] = mCirclePoints[i].y + dy;
        }

        canvas.drawLines(mFFTPoints, mWavePaint);
        // 渐变产生的阴影的效果
        canvas.drawPaint(mFadePaint);
    }

    @Override
    public void setByte(byte[] bytes) {
        super.setByte(bytes);
        if (mFFTPoints == null || mFFTPoints.length < bytes.length * 4) {
            mFFTPoints = new float[bytes.length * 4];
        }
    }

}
