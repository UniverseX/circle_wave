package com.autoai.circlewave.effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import com.autoai.circlewave.util.BezierUtil;
import com.autoai.circlewave.util.EffectUtil;

/**
 * 跳动旋律
 */
public class LineWave extends BaseEffect{
    private static final String TAG = "WaveEffect";
    private Paint mWavePaint1;
    private Paint mWavePaint2;
    private Paint mWavePaint3;
    private byte[] lastBytes;
    private byte[] lastBytes2;

    private PaintFlagsDrawFilter mCanvasAntiFilter;//canvas.setDrawFilter( mCanvasAntiFilter );
    private Path mWavePath = new Path();

    private static final float WAVE_DIAMETER_OFFSET = 35f;
    private static final float WAVE_STROKE_WIDTH = 2f;
    private static final float WAVE_AMPLITUDE_RATIO = 0.3f;
    private static final int WAVE_POINTS = 15;
    private float mWaveDiameter;

    private PointF[] points = new PointF[WAVE_POINTS];

    public LineWave(Context context, Bitmap bg) {
        super(context, bg);

        mWaveDiameter = mCircleDiameter + density * WAVE_DIAMETER_OFFSET;
        float mWaveStroke = density * WAVE_STROKE_WIDTH;

        mCanvasAntiFilter = new PaintFlagsDrawFilter(0,Paint.FILTER_BITMAP_FLAG);

        mWavePaint1 = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG /*| Paint.FILTER_BITMAP_FLAG*/);
        mWavePaint1.setStrokeWidth(mWaveStroke);
        mWavePaint1.setStyle(Paint.Style.STROKE);
        mWavePaint1.setStrokeJoin(Paint.Join.ROUND);
        CornerPathEffect cornerPathEffect = new CornerPathEffect(130);
        mWavePaint1.setPathEffect(cornerPathEffect);
        mWavePaint1.setColor(mainBgColor);

        mWavePaint2 = new Paint(mWavePaint1);
        mWavePaint3 = new Paint(mWavePaint1);
    }

    private long count = 0;

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
        //TODO 留影效果
//        if(count % 30 == 0) {
//            drawWave(canvas, mBytes, mWavePaint1);
//            drawWave(canvas, lastBytes, mWavePaint2);
//            drawWave(canvas, lastBytes2, mWavePaint3);
//        }else {
//            drawWave(canvas, mBytes, mWavePaint1);
//            if(count % 30 == 10)
//                lastBytes = mBytes;
//            if(count % 30 == 20)
//                lastBytes2 = mBytes;
//        }
//        count++;
        drawWave(canvas, mBytes, mWavePaint1);

        //draw circle
        canvas.drawBitmap(circle, mCircleMatrix, mCirclePaint);
        mCircleMatrix.postRotate(SPEED_ROTATE, surfaceRect.width() / 2f, surfaceRect.height() / 2f);
    }

    private void drawWave(Canvas canvas, byte[] bytes, Paint paint) {
        if(bytes == null){
            return;
        }
        double centerX = surfaceRect.width() / 2f;
        double centerY = surfaceRect.height() / 2f;
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

    @Override
    public void setSurfaceRectF(RectF rectF) {
        super.setSurfaceRectF(rectF);

        setShader(mWavePaint2, rectF.width()/2 , rectF.height()/2, mWaveDiameter/2);
        setShader(mWavePaint3, rectF.width()/2 , rectF.height()/2, mWaveDiameter/2);
    }

    public void setShader(Paint paint, float cx, float cy, float radius){
        int[] colors = new int[3];
        float[] positions = new float[3];

        // 第1个点
        colors[0] = 0x00ffffff;
        positions[0] = 0;

        // 第2个点
        colors[1] = 0x77ffffff;
        positions[1] = 0.5f;

        // 第3个点
        colors[2] = 0x7fffffff;
        positions[2] = 1;

        RadialGradient shader = new RadialGradient(
                cx, cy,
                radius,
                colors,
                positions,
                Shader.TileMode.MIRROR);
        paint.setShader(shader);
    }
}
