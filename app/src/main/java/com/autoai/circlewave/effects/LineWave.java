package com.autoai.circlewave.effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.DisplayMetrics;

import com.autoai.circlewave.util.EffectUtil;

/**
 * 跳动旋律
 */
public class LineWave extends BaseEffect{
    private static final String TAG = "WaveEffect";
    private Paint bgPaint = new Paint();
    private Paint mCirclePanit;
    private Paint mWavePaint;
    private PaintFlagsDrawFilter mCanvasAntiFilter;//canvas.setDrawFilter( mCanvasAntiFilter );
    private Path mWavePath = new Path();

    private static final float WAVE_DIAMETER_OFFSET = 35f;
    private static final float WAVE_STROKE_WIDTH = 2f;
    private static final float WAVE_WIDTH = 2F;
    private static final float WAVE_MAX_HEIGHT = 10F;
    private static final float WAVE_AMPLITUDE_RATIO = 0.15f;
    private float mWaveDiameter;
    private float mWaveStoke;

    private PointF[] points = new PointF[20];

    public LineWave(Context context, Bitmap bg) {
        super(context, bg);

        mWaveDiameter = mCircleDiameter + density * WAVE_DIAMETER_OFFSET;
        mWaveStoke = density * WAVE_STROKE_WIDTH;

        mCirclePanit = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG/* | Paint.FILTER_BITMAP_FLAG*/);
        mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG /*| Paint.FILTER_BITMAP_FLAG*/);
        mWavePaint.setStrokeWidth(mWaveStoke);
        mWavePaint.setStyle(Paint.Style.STROKE);
        mWavePaint.setStrokeJoin(Paint.Join.ROUND);
        CornerPathEffect cornerPathEffect = new CornerPathEffect(130);
        mWavePaint.setPathEffect(cornerPathEffect);
        mWavePaint.setColor(Color.rgb(0x5f, 0x5f, 0xff));
        mCanvasAntiFilter = new PaintFlagsDrawFilter(0,Paint.FILTER_BITMAP_FLAG);

    }

    @Override
    public void onDraw(Canvas canvas) throws Exception{
        //clear
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //draw bg
        canvas.drawBitmap(bg, null, surfaceRect, bgPaint);

        //draw circle
        canvas.drawBitmap(circle, mCircleMatrix, mCirclePanit);

        //draw wave
        double centerX = surfaceRect.width() / 2f;
        double centerY = surfaceRect.height() / 2f;
        int index = 0;
        int byteIndex = 0;

        for (float i = 0; i < 360f; i += 18f) {
            float nx = (float) (Math.cos(Math.toRadians(i)) * mWaveDiameter / 2f + centerX);
            float ny = (float) (Math.sin(Math.toRadians(i)) * mWaveDiameter / 2f + centerY);
//                    canvas.drawPoint((float)nx, (float)ny, mWavePaint);

            float w_ratioX = 0;
            float w_ratioY = 0;
            if(mBytes != null && byteIndex < mBytes.length){
                byteIndex = (int) (i / 360f * mBytes.length);
                w_ratioX = (float) (mBytes[index] * WAVE_AMPLITUDE_RATIO * Math.cos(Math.toRadians(i)));
                w_ratioY = (float) (mBytes[index] * WAVE_AMPLITUDE_RATIO * Math.sin(Math.toRadians(i)));
            }
            //TODO 定一个最低的值
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
            PointF[] pb = EffectUtil.getCtrlPoint(points, i);
            mWavePath.cubicTo(pb[0].x, pb[0].y, pb[1].x, pb[1].y, points[i].x, points[i].y);
        }
        PointF[] pb = EffectUtil.getCtrlPoint(points, 0);
        mWavePath.cubicTo(pb[0].x, pb[0].y, pb[1].x, pb[1].y, points[0].x, points[0].y);

        canvas.drawPath(mWavePath, mWavePaint);

        mCircleMatrix.postRotate(1f, surfaceRect.width() / 2f, surfaceRect.height() / 2f);

    }

}
