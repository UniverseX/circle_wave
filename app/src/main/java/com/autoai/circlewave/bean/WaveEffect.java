package com.autoai.circlewave.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.autoai.circlewave.R;
import com.autoai.circlewave.util.BitmapUtil;

public class WaveEffect implements Effect{
    private static final String TAG = "WaveEffect";
    private Context mContext;
    private RectF surfaceRect;
    private float density;

    private Bitmap bg;
    private Bitmap circle;

    private Matrix mMatrix;

    private Paint bgPaint = new Paint();
    private Paint mCirclePanit;
    private Paint mWavePaint;
    private PaintFlagsDrawFilter mCanvasAntiFilter;//canvas.setDrawFilter( mCanvasAntiFilter );
    private Path mWavePath = new Path();

    private static final float CIRCLE_DIAMETER = 255f;

    private static final float WAVE_DIAMETER_OFFSET = 35f;
    private static final float WAVE_STROKE_WIDTH = 2f;
    private static final float WAVE_WIDTH = 2F;
    private static final float WAVE_MAX_HEIGHT = 10F;
    private static final float WAVE_AMPLITUDE_RATIO = 0.15f;
    private float mWaveDiameter;
    private float mWaveStoke;
    private float mCircleDiameter;

    private byte[] mBytes;
    private PointF[] points = new PointF[20];

    private static volatile boolean hasBlurBg;
    private static volatile boolean hasClipCircle;

    public WaveEffect(Context context, Bitmap bg) {
        mContext = context;
        this.bg = bg;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        density = displayMetrics.density;
        mCircleDiameter = displayMetrics.widthPixels / 4f;
        mWaveDiameter = mCircleDiameter + density * WAVE_DIAMETER_OFFSET;
        mWaveStoke = density * WAVE_STROKE_WIDTH;


        mMatrix = new Matrix();
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
    public void blurBg() {
        if(!hasBlurBg){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                bg = BitmapUtil.rsBlur(mContext, BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.bg)
                        , 24, 1);
            }
            hasBlurBg = true;
        }
    }

    @Override
    public void clipCircle() {
        if(!hasClipCircle){
            //缩小
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bg, (int) (mCircleDiameter * bg.getWidth() / bg.getHeight()), (int) mCircleDiameter, false);
            Log.d(TAG, "clipCircle: ratio = "+ (1f *  bg.getWidth() / bg.getHeight()));
            RectF dst = new RectF(0, 0, mCircleDiameter, mCircleDiameter);
            //裁剪
//            circle = scaledBitmap;
            circle = BitmapUtil.createCircleBitmap(scaledBitmap, mCircleDiameter, dst);

            Rect src = new Rect(0, 0, circle.getWidth(), circle.getHeight());
            float circleLeft = surfaceRect.width() / 2f - (src.width() / 2f);
            float circleTop = surfaceRect.height() / 2f - (src.height() / 2f);
            mMatrix.setTranslate(circleLeft, circleTop);
            hasClipCircle = true;
        }
    }

    @Override
    public void draw(Canvas canvas) throws Exception{
        clipCircle();
        blurBg();

        //clear
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //draw bg
        canvas.drawBitmap(bg, null, surfaceRect, bgPaint);

        //draw bg
        canvas.drawBitmap(circle, mMatrix, mCirclePanit);

        //draw triangle

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
            PointF[] pb = getCtrlPoint(points, i);
            mWavePath.cubicTo(pb[0].x, pb[0].y, pb[1].x, pb[1].y, points[i].x, points[i].y);
        }
        PointF[] pb = getCtrlPoint(points, 0);
        mWavePath.cubicTo(pb[0].x, pb[0].y, pb[1].x, pb[1].y, points[0].x, points[0].y);

        canvas.drawPath(mWavePath, mWavePaint);

        mMatrix.postRotate(1f, surfaceRect.width() / 2f, surfaceRect.height() / 2f);

        Thread.sleep(10); // 这个就相当于帧频了，数值越小画面就越流畅
    }

    @Override
    public void setByte(byte[] bytes) {
        mBytes = bytes;
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        surfaceRect = new RectF(0, 0, width, height);
    }

    private PointF[] getCtrlPoint(PointF[] ps, int i){

        int prev = i - 1;
        if(prev < 0){
            prev = ps.length - 1;
        }
        int pprev = prev - 1;
        if(pprev < 0){
            pprev = 0;
        }

        int next = i + 1;
        if(next >= ps.length){
            next = 0;
        }
        float x0 = ps[pprev].x;
        float y0 = ps[pprev].y;
        float x1 = ps[prev].x;
        float y1 = ps[prev].y;
        float x2 = ps[i].x;
        float y2 = ps[i].y;
        float x3 = ps[next].x;
        float y3 = ps[next].y;

        float smooth_value = 0.6f;
        PointF[] ctrls = new PointF[2];
        PointF pA = new PointF();
        PointF pB = new PointF();
        float xc1 = (x0 + x1) /2.0f;
        float yc1 = (y0 + y1) /2.0f;
        float xc2 = (x1 + x2) /2.0f;
        float yc2 = (y1 + y2) /2.0f;
        float xc3 = (x2 + x3) /2.0f;
        float yc3 = (y2 + y3) /2.0f;
        float len1 = (float) Math.sqrt((x1-x0) * (x1-x0) + (y1-y0) * (y1-y0));
        float len2 = (float) Math.sqrt((x2-x1) * (x2-x1) + (y2-y1) * (y2-y1));
        float len3 = (float) Math.sqrt((x3-x2) * (x3-x2) + (y3-y2) * (y3-y2));
        float k1 = len1 / (len1 + len2);
        float k2 = len2 / (len2 + len3);
        float xm1 = xc1 + (xc2 - xc1) * k1;
        float ym1 = yc1 + (yc2 - yc1) * k1;
        float xm2 = xc2 + (xc3 - xc2) * k2;
        float ym2 = yc2 + (yc3 - yc2) * k2;
        pA.x = xm1 + (xc2 - xm1) * smooth_value + x1 - xm1;
        pA.y = ym1 + (yc2 - ym1) * smooth_value + y1 - ym1;
        pB.x = xm2 + (xc2 - xm2) * smooth_value + x2 - xm2;
        pB.y = ym2 + (yc2 - ym2) * smooth_value + y2 - ym2;


        ctrls[0] = pA;
        ctrls[1] = pB;
        return ctrls;
    }
}
