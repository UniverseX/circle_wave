package com.autoai.circlewave.effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.DisplayMetrics;

import com.autoai.circlewave.R;
import com.autoai.circlewave.util.EffectUtil;

public abstract class BaseEffect implements Effect {
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

    public BaseEffect(Context context, Bitmap bg) {
        mContext = context;
        this.bg = bg;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        density = displayMetrics.density;
        mCircleDiameter = displayMetrics.widthPixels / 4f;
        mCircleMatrix = new Matrix();
    }

    protected void blurBg(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            bg = EffectUtil.rsBlur(mContext, BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.bg)
                    , 24, 1);
        }
    }

    protected void clipCircle(){
        //缩小
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bg, (int) (mCircleDiameter * bg.getWidth() / bg.getHeight()), (int) mCircleDiameter, false);
        RectF dst = new RectF(0, 0, mCircleDiameter, mCircleDiameter);
        //裁剪
//            circle = scaledBitmap;
        circle = EffectUtil.createCircleBitmap(scaledBitmap, mCircleDiameter, dst);

        Rect src = new Rect(0, 0, circle.getWidth(), circle.getHeight());
        float circleLeft = surfaceRect.width() / 2f - (src.width() / 2f);
        float circleTop = surfaceRect.height() / 2f - (src.height() / 2f);
        mCircleMatrix.setTranslate(circleLeft, circleTop);
    }

    public abstract void onDraw(Canvas canvas) throws Exception;

    @Override
    public void draw(Canvas canvas) throws Exception {
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

    public void invalidate(){
        hasProcessBg = false;
        needDraw = false;
    }
}
