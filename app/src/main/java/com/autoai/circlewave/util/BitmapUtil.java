package com.autoai.circlewave.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Matrix4f;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BitmapUtil {
    private static final String TAG = "BitmapUtil";

    /**
     * @param inputBmp 输入bitmap
     * @param radius   模糊半径
     * @return 模糊后的bitmap
     */
    public static Bitmap rsBlur(Context context, @NonNull Bitmap inputBmp, int radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            RenderScript renderScript = RenderScript.create(context);

            // Allocate memory for Renderscript to work with
            final Allocation input = Allocation.createFromBitmap(renderScript, inputBmp);
            final Allocation output = Allocation.createTyped(renderScript, input.getType());

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
        }else {
            Log.w(TAG, "rsBlur failed: your sdk version must bigger than JELLY_BEAN_MR1(17)" );
        }
        return inputBmp;
    }

    public static Bitmap circleProcess(@NonNull Bitmap resource, float circleDiameter) {
        Bitmap src = scaleBitmap(resource, circleDiameter / resource.getWidth(), circleDiameter / resource.getHeight(), false);
        //获取图片的宽度
        Paint paint = new Paint();
        //设置抗锯齿
        paint.setAntiAlias(true);
        paint.setDither(true);

        //创建一个与原bitmap一样宽度的正方形bitmap
        Bitmap circleBitmap = Bitmap.createBitmap((int) circleDiameter, (int) circleDiameter, Bitmap.Config.ARGB_8888);
        //以该bitmap为低创建一块画布
        Canvas canvas = new Canvas(circleBitmap);
        //以（width/2, width/2）为圆心，width/2为半径画一个圆
        canvas.drawCircle(circleDiameter / 2, circleDiameter / 2, circleDiameter / 2, paint);

        //设置画笔为取交集模式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //裁剪图片
        canvas.drawBitmap(src, null, new Rect(0, 0, (int) circleDiameter, (int) circleDiameter), paint);

        return circleBitmap;
    }


    public static Bitmap bgProcess(Context context, @NonNull Bitmap src, RectF rectF, boolean needBlurFirst) {

        int radius;
        float sx = src.getWidth() / rectF.width();
        float sy = src.getHeight() / rectF.height();
        if(sx > 1) sx = 1 / sx;
        if(sy > 1) sy = 1 / sy;
        radius = (int) Math.max(24 * sx, 24 * sy);

        Log.d(TAG, "bgProcess: radius = " + radius);
        //先模糊
        Bitmap rsBlur = src;
        if(needBlurFirst) rsBlur = rsBlur(context, src, radius);

        Bitmap scaledBg;
        //如果比背景VIEW小，先放大
        if (src.getWidth() < rectF.width()) {
            float x_scale = rectF.width() / src.getWidth();
            if(src.getHeight() < rectF.height()) {
                float y_scale = src.getHeight() / rectF.height();
                scaledBg = scaleBitmap(rsBlur, x_scale, y_scale, false);
            }else if(src.getHeight() == rectF.height()){
                scaledBg = scaleBitmap(rsBlur, x_scale, 1, false);
            }else {
                float y_scale = rectF.height() / src.getHeight();
                scaledBg = scaleBitmap(rsBlur, x_scale, y_scale, false);
            }
        } else if (src.getWidth() > rectF.width()){
            //如果比背景VIEW大，缩小
            float x_scale = rectF.width() / src.getWidth();
            if(src.getHeight() > rectF.height()){
                return bgProcess(context, scaleBitmap(rsBlur, x_scale, 1, false),
                        rectF, true);
            }else {
                return bgProcess(context, scaleBitmap(rsBlur, x_scale, 1, false),
                        rectF, false);
            }

        } else {
            if(src.getHeight() < rectF.height()) {
                float y_scale = src.getHeight() / rectF.height();
                scaledBg = scaleBitmap(rsBlur, 1, y_scale, false);
            }else if(src.getHeight() == rectF.height()){
                scaledBg = rsBlur;
            }else {
                float y_scale = rectF.height() / src.getHeight();
                scaledBg = scaleBitmap(rsBlur, 1, y_scale, false);
            }
        }

        //在模糊一次
        return rsBlur(context, scaledBg, radius);
    }

    private static Matrix scaleMatrix = new Matrix();

    public static Bitmap scaleBitmap(@NonNull Bitmap src, float x_scale, float y_scale, boolean filter) {
        scaleMatrix.reset();
        scaleMatrix.postScale(x_scale, y_scale);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), scaleMatrix, filter);
    }

}
