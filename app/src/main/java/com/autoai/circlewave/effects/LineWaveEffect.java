package com.autoai.circlewave.effects;

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
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.autoai.circlewave.R;
import com.autoai.circlewave.util.EffectUtil;

public class LineWaveEffect extends BaseEffect{
    private static final String TAG = "WaveEffect";
    private Context mContext;
    private RectF surfaceRect;
    private float density;

    private Bitmap circle;

    private Matrix mMatrix;

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
    private float mCircleDiameter;

    private byte[] mBytes;
    private PointF[] points = new PointF[20];

    public LineWaveEffect(Context context, Bitmap bg) {
        super(context, bg);
        mContext = context;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            bg = EffectUtil.rsBlur(mContext, BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.bg)
                    , 24, 1);
        }
    }

    @Override
    public void clipCircle() {
        //缩小
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bg, (int) (mCircleDiameter * bg.getWidth() / bg.getHeight()), (int) mCircleDiameter, false);
        Log.d(TAG, "clipCircle: ratio = "+ (1f *  bg.getWidth() / bg.getHeight()));
        RectF dst = new RectF(0, 0, mCircleDiameter, mCircleDiameter);
        //裁剪
//            circle = scaledBitmap;
        circle = EffectUtil.createCircleBitmap(scaledBitmap, mCircleDiameter, dst);

        Rect src = new Rect(0, 0, circle.getWidth(), circle.getHeight());
        float circleLeft = surfaceRect.width() / 2f - (src.width() / 2f);
        float circleTop = surfaceRect.height() / 2f - (src.height() / 2f);
        mMatrix.setTranslate(circleLeft, circleTop);
    }

    @Override
    public void onDraw(Canvas canvas) throws Exception{
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
            PointF[] pb = EffectUtil.getCtrlPoint(points, i);
            mWavePath.cubicTo(pb[0].x, pb[0].y, pb[1].x, pb[1].y, points[i].x, points[i].y);
        }
        PointF[] pb = EffectUtil.getCtrlPoint(points, 0);
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
    public void setSurfaceRectF(RectF rectF) {
        surfaceRect = rectF;
    }


}
