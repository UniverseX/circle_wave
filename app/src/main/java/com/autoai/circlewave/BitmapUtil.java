package com.autoai.circlewave;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Matrix4f;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicColorMatrix;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class BitmapUtil {
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
}
