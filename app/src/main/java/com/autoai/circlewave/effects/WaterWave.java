package com.autoai.circlewave.effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;

import com.autoai.circlewave.util.BezierUtil;

public class WaterWave extends BaseEffect {

    private Paint mWavePaint;

    private Path mWavePath = new Path();

    private static final float WAVE_DIAMETER_OFFSET = 20f;
    private static final float WAVE_STROKE_WIDTH = 2f;
    private static final float WAVE_AMPLITUDE_RATIO = 0.4f;
    private static final int WAVE_POINTS = 15;
    private float mWaveDiameter;

    private PointF[] points = new PointF[WAVE_POINTS];
    private PointF[] origPoints = new PointF[WAVE_POINTS];

    public WaterWave(Context context, Bitmap bg) {
        super(context, bg);

        mWaveDiameter = mCircleDiameter + density * WAVE_DIAMETER_OFFSET;
        float mWaveStroke = density * WAVE_STROKE_WIDTH;

        mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG /*| Paint.FILTER_BITMAP_FLAG*/);
        mWavePaint.setStrokeWidth(mWaveStroke);
//        mWavePaint.setStyle(Paint.Style.STROKE);
        mWavePaint.setStrokeJoin(Paint.Join.ROUND);
        CornerPathEffect cornerPathEffect = new CornerPathEffect(130);
        mWavePaint.setPathEffect(cornerPathEffect);
        mWavePaint.setColor(mainBgColor);
        mWavePaint.setAlpha(0x7f);
    }

    @Override
    public void onDraw(Canvas canvas) throws Exception{
        //clear
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if(bg == null){
            return;
        }
        //draw bg
        canvas.drawBitmap(bg, null, surfaceRect, bgPaint);

        //draw wave
        drawWave(canvas, mBytes, mWavePaint);

        //draw circle
        canvas.drawBitmap(circle, mCircleMatrix, mCirclePaint);
        mCircleMatrix.postRotate(SPEED_ROTATE, surfaceRect.width() / 2f, surfaceRect.height() / 2f);
    }

    private void drawWave(Canvas canvas, byte[] bytes, Paint paint) {
        if(bytes == null){
            return;
        }
        float centerX = (surfaceRect.right + surfaceRect.left) / 2;
        float centerY = (surfaceRect.bottom + surfaceRect.top) / 2;
        float radius = mWaveDiameter / 2;
        int index = 0;
        int byteIndex = 0;

        for (float degree = 0; degree < 360f; degree += 360f/WAVE_POINTS) {
            float nx = (float) (Math.cos(Math.toRadians(degree)) * mWaveDiameter / 2f + centerX);
            float ny = (float) (Math.sin(Math.toRadians(degree)) * mWaveDiameter / 2f + centerY);

            float w_ratioX = 0;
            float w_ratioY = 0;
            if(byteIndex < bytes.length){
                byteIndex = (int) (degree / 360f * bytes.length);

                w_ratioX = (float) (Math.abs(bytes[index]) * WAVE_AMPLITUDE_RATIO * Math.cos(Math.toRadians(degree)));
                w_ratioY = (float) (Math.abs(bytes[index]) * WAVE_AMPLITUDE_RATIO * Math.sin(Math.toRadians(degree)));
            }

            if(origPoints[index] == null){
                PointF pointF = new PointF();
                pointF.x = nx + w_ratioX;
                pointF.y = ny + w_ratioY;
                origPoints[index] = pointF;
            }else {
                origPoints[index].x = nx + w_ratioX;
                origPoints[index].y = ny + w_ratioY;
            }
            if(points[index] == null) {
                PointF pointF = new PointF();
                pointF.x = nx + w_ratioX;
                pointF.y = ny + w_ratioY;
                points[index] = pointF;
            }else {
                points[index].x = nx + w_ratioX;
                points[index].y = ny + w_ratioY;
            }

            index ++;
        }
        mWavePath.reset();

        mWavePath.moveTo(points[0].x, points[0].y);
        for (int i = 1; i < points.length; i++) {
            PointF[] pb = BezierUtil.getCtrlPoint(points, i);
            mWavePath.cubicTo(pb[0].x, pb[0].y, pb[1].x, pb[1].y, points[i].x, points[i].y);
        }
        PointF[] pb = BezierUtil.getCtrlPoint(points, 0);
        mWavePath.cubicTo(pb[0].x, pb[0].y, pb[1].x, pb[1].y, points[0].x, points[0].y);
        canvas.drawPath(mWavePath, paint);
    }


}
