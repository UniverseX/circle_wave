package com.autoai.circlewave.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Matrix4f;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class EffectUtil {
    private static final String TAG = "BitmapUtil";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Bitmap rsBlur(Context context, Bitmap source, int radius, float scale){

        Log.i(TAG,"origin size:"+source.getWidth()+"*"+source.getHeight());
        int width = Math.round(source.getWidth() * scale);
        int height = Math.round(source.getHeight() * scale);

        Bitmap inputBmp = Bitmap.createScaledBitmap(source,width,height,false);

        RenderScript renderScript =  RenderScript.create(context);

        Log.i(TAG,"scale size:"+inputBmp.getWidth()+"*"+inputBmp.getHeight());

        // Allocate memory for Renderscript to work with

        final Allocation input = Allocation.createFromBitmap(renderScript,inputBmp);
        final Allocation output = Allocation.createTyped(renderScript,input.getType());

        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        scriptIntrinsicBlur.setInput(input);

        // Set the blur radius
        if (!(radius <= 0 || radius > 25)) {
            scriptIntrinsicBlur.setRadius(radius);
        }

        // Start the ScriptIntrinisicBlur
        scriptIntrinsicBlur.forEach(output);

        // Copy the output to the blurred bitmap
        output.copyTo(inputBmp);


        renderScript.destroy();
        return inputBmp;
    }

    private static final Matrix4f BRIGHTNESS_ADJUSTMENT_FACTOR_MATRIX = new Matrix4f(new float[]{
            0.0f, 0f, 0.0f, 0.0f,
            0.0f, 0f, 0.0f, 0.0f,
            0.0f, 0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f,
    });


    /**清除背景颜色
     * @param mBitmap
     * @param mColor 背景颜色值 eg：Color.WHITE
     *
     * @return
     */
    public static Bitmap getAlphaBitmap(Bitmap mBitmap, int mColor)
    {
        Bitmap mAlphaBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        int mBitmapWidth = mAlphaBitmap.getWidth();
        int mBitmapHeight = mAlphaBitmap.getHeight();

        for (int i = 0; i < mBitmapHeight; i++)
        {
            for (int j = 0; j < mBitmapWidth; j++)
            {
                int color = mBitmap.getPixel(j, i);
                if (color != mColor)
                {
                    mAlphaBitmap.setPixel(j, i, color);
                }
            }
        }

        return mAlphaBitmap;
    }

    public static Bitmap createCircleBitmap(Bitmap resource, float circleDiameter, RectF dstRect)
    {
        //获取图片的宽度
        int width = resource.getWidth();
        Paint paint = new Paint();
        //设置抗锯齿
        paint.setAntiAlias(true);

        //创建一个与原bitmap一样宽度的正方形bitmap
        Bitmap circleBitmap = Bitmap.createBitmap((int)circleDiameter, (int) circleDiameter, Bitmap.Config.ARGB_8888);
        //以该bitmap为低创建一块画布
        Canvas canvas = new Canvas(circleBitmap);
        //以（width/2, width/2）为圆心，width/2为半径画一个圆
        canvas.drawCircle(circleDiameter/2, circleDiameter/2, circleDiameter/2, paint);

        //设置画笔为取交集模式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //裁剪图片
        canvas.drawBitmap(resource, null, dstRect, paint);

        return circleBitmap;
    }

    public static PointF[] getCtrlPoint(PointF[] ps, int i){

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
