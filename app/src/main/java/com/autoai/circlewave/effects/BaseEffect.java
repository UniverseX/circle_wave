package com.autoai.circlewave.effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;

import com.autoai.circlewave.util.EffectUtil;

public abstract class BaseEffect implements Effect {
    public static final float SPEED_ROTATE = 0.3F;//per 10 ms
    protected Context mContext;
    protected float density;
    protected Bitmap bg;
    protected Bitmap circle;

    private boolean hasProcessBg;
    private boolean needDraw;

    protected float mCircleDiameter;
    protected Matrix mCircleMatrix;

    protected RectF surfaceRect;
    protected byte[] mBytes;

    protected Paint bgPaint = new Paint();
    protected Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    protected int mainBgColor = Color.rgb(0x5f, 0x5f, 0x5f);

    public BaseEffect(Context context, Bitmap bg) {
        mContext = context;
        this.bg = bg;
        Palette palette = Palette.generate(bg);

        Palette.Swatch swatch = palette.getLightMutedSwatch();
        if(swatch != null){
            mainBgColor = swatch.getRgb();
        }

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        density = displayMetrics.density;
        mCircleDiameter = displayMetrics.heightPixels / 2f;
        mCircleMatrix = new Matrix();
    }

    public void blurBg(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            bg = blurBg(bg);
        }
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public Bitmap blurBg(Bitmap bg){
        return EffectUtil.rsBlur(mContext, bg, 24, 1);
    }

    public void clipCircle(){
        circle = clipCircle(bg);

        Rect src = new Rect(0, 0, circle.getWidth(), circle.getHeight());
        float circleLeft = surfaceRect.width() / 2f - (src.width() / 2f);
        float circleTop = surfaceRect.height() / 2f - (src.height() / 2f);
        mCircleMatrix.setTranslate(circleLeft, circleTop);
    }

    @Override
    public Bitmap clipCircle(Bitmap bg){
        //缩小
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bg, (int) (mCircleDiameter * bg.getWidth() / bg.getHeight()), (int) mCircleDiameter, false);
        RectF dst = new RectF(0, 0, mCircleDiameter, mCircleDiameter);
        //裁剪
        return EffectUtil.createCircleBitmap(scaledBitmap, mCircleDiameter, dst);
    }

    public abstract void onDraw(Canvas canvas) throws Exception;

    @Override
    final public void draw(Canvas canvas) throws Exception {
        if(!hasProcessBg) {
            clipCircle();
            blurBg();
            hasProcessBg = true;
            needDraw = true;
        }
        if(needDraw) {
            onDraw(canvas);
        }
    }


    @Override
    public void setByte(byte[] bytes) {
        mBytes = bytes;
    }

    @Override
    public void setSurfaceRectF(RectF rectF) {
        surfaceRect = rectF;
    }

    @Override
    public void setBitmap(Bitmap bg){
        this.bg = bg;
        invalidate();
    }

    private void invalidate(){
        hasProcessBg = false;
        needDraw = false;
    }
}
