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

import java.util.ArrayList;
import java.util.List;

public class ExplosiveParticle extends BaseEffect {

    private Paint mWavePaint;
    private Paint mWaveCirclePaint;

    private Path mWavePath = new Path();

    private static final float WAVE_DIAMETER_OFFSET = 15f;
    private static final float WAVE_AMPLITUDE_RATIO = 0.25f;
    private static final float WAVE_POINTS_PER_SEGMENT = 8;
    private static final int WAVE_POINTS = 15;
    private float mWaveDiameter;
    private float mWaveStroke;

    private PointF[] points = new PointF[WAVE_POINTS];
    private List<PointF> particles = new ArrayList<>();


    public ExplosiveParticle(Context context, Bitmap bg) {
        super(context, bg);

        mWaveDiameter = mCircleDiameter + density * WAVE_DIAMETER_OFFSET;
        mWaveStroke = (float) (Math.PI * mWaveDiameter / WAVE_POINTS / 15f);

        mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG /*| Paint.FILTER_BITMAP_FLAG*/);
        mWavePaint.setStrokeWidth(mWaveStroke);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setColor(mainBgColor);
        mWavePaint.setStrokeJoin(Paint.Join.ROUND);
        CornerPathEffect cornerPathEffect = new CornerPathEffect(130);
        mWavePaint.setPathEffect(cornerPathEffect);

        mWaveCirclePaint = new Paint(mWavePaint);
        mWaveCirclePaint.setStyle(Paint.Style.STROKE);
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

    private float[] wavePoints;
    private void drawWave(Canvas canvas, byte[] bytes, Paint paint) {
        if(bytes == null){
            return;
        }
        float centerX = surfaceRect.width() / 2f;
        float centerY = surfaceRect.height() / 2f;
        float radius = mWaveDiameter / 2;
        float outside_radius = radius + 2 * density + mWaveStroke;
        int index = 0;
        int byteIndex = 0;

        canvas.drawCircle(centerX, centerY, radius, mWaveCirclePaint);

        mWavePath.reset();
        for (float degree = 0; degree < 360f; degree += 360f/WAVE_POINTS) {
            float nx = (float) (Math.cos(Math.toRadians(degree)) * outside_radius + centerX);
            float ny = (float) (Math.sin(Math.toRadians(degree)) * outside_radius + centerY);

            float w_ratioX = 0;
            float w_ratioY = 0;
            if(byteIndex < bytes.length){
                byteIndex = (int) (degree / 360f * bytes.length);

                w_ratioX = (float) (Math.abs(bytes[index]) * WAVE_AMPLITUDE_RATIO * Math.cos(Math.toRadians(degree)));
                w_ratioY = (float) (Math.abs(bytes[index]) * WAVE_AMPLITUDE_RATIO * Math.sin(Math.toRadians(degree)));
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

        particles.clear();
        PointF tmp = points[0];
        for (int i = 1; i < points.length; i++) {
            PointF[] pb = BezierUtil.getCtrlPoint(points, i);
            for (int j = 0; j < WAVE_POINTS_PER_SEGMENT; j++) {
                particles.add(BezierUtil.calCurvePoint(tmp, pb[0], pb[1], points[i], j / WAVE_POINTS_PER_SEGMENT));
            }
            tmp = points[i];
        }
        PointF[] pb = BezierUtil.getCtrlPoint(points, 0);
        for (int j = 0; j < WAVE_POINTS_PER_SEGMENT; j++) {
            particles.add(BezierUtil.calCurvePoint(tmp, pb[0], pb[1], points[0], j / WAVE_POINTS_PER_SEGMENT));
        }

        if(wavePoints == null){
            wavePoints = new float[particles.size() * 2];
        }
        for (int i = 0; i < particles.size(); i++) {
            wavePoints[i * 2] = particles.get(i).x;
            wavePoints[i * 2 + 1] = particles.get(i).y;
        }
        canvas.drawPoints(wavePoints, paint);
    }

}
