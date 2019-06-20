package com.autoai.circlewave;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.autoai.circlewave.util.BitmapUtil;

import java.util.Random;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class WaveSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "WaveSurfaceView";

    private SurfaceHolder mHolder;
    private Thread drawThread;
    private Canvas mCanvas;
    private Paint mCirclePanit;
    private Paint mWavePaint;
    private Path mWavePath = new Path();
    private final Paint bgPaint = new Paint();
    private PaintFlagsDrawFilter mCanvasAntiFilter;//canvas.setDrawFilter( mCanvasAntiFilter );
    private RectF mRect;
    private Bitmap bg;
    private Bitmap circle;
    private Matrix mMatrix;
    private static boolean isBgSeted = false;
    private volatile boolean isRun = true;
    private float mCircleLeft;
    private float mCircleTop;
    private float randomInitX;
    private Random random;
    private static final float CIRCLE_DIAMETER = 270f;
    private static float sCircleDiameter;
    private float initX;
    private float initY;

    public WaveSurfaceView(Context context) {
        this(context, null);
    }

    public WaveSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.RGBA_8888);
        bg = BitmapUtil.rsBlur(getContext(), BitmapFactory.decodeResource(getResources(), R.mipmap.bg)
                , 0, 1);
        circle = BitmapFactory.decodeResource(getResources(), R.mipmap.circle);
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//deprecated
//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        this.setKeepScreenOn(true);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        sCircleDiameter = displayMetrics.density * CIRCLE_DIAMETER;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRun = true;
        mMatrix = new Matrix();
        mCirclePanit = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
        mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
        mWavePaint.setStrokeWidth(20);
        mWavePaint.setColor(Color.MAGENTA);
        mCanvasAntiFilter = new PaintFlagsDrawFilter(0,Paint.FILTER_BITMAP_FLAG);

//        setZOrderOnTop(true);
        drawThread = new Thread(this);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        mRect = new RectF(0, 0, width, height);
        Rect src = new Rect(0, 0, circle.getWidth(), circle.getHeight());
        mCircleLeft = mRect.width() / 2f - (src.width() / 2f);
        mCircleTop = mRect.height() / 2f - (src.height() / 2f);
        mMatrix.setTranslate(mCircleLeft, mCircleTop);

        randomInitX = mCircleLeft - (sCircleDiameter - src.width()) / 2f;
        initX = mCircleLeft - (sCircleDiameter - src.width()) / 2f;
        initY = mCircleTop - (sCircleDiameter - src.height()) / 2f;
        mWavePath.moveTo(initX, initY);
        random = new Random();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRun = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void run() {
        while (isRun) {
            try {
                assert (mRect != null);


                mCanvas = mHolder.lockCanvas();
                if(mCanvas == null){
                    break;
                }
                //clear
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                //draw bg
                mCanvas.drawBitmap(bg, null, mRect, bgPaint);

                //draw circle bg
                mCanvas.drawBitmap(circle, mMatrix, mCirclePanit);

                //draw triangle

                //draw wave
                double x =initX, y = initY;
                mWavePath.reset();
                mWavePath.moveTo(initX, initY);
                double centerX = mRect.width() / 2f;
                double centerY = mRect.height() / 2f;

                for (int i = 0; i < 360; i+=2) {
                    double nx = Math.cos(Math.toRadians(i)) * sCircleDiameter / 2f + centerX;
                    double ny = Math.sin(Math.toRadians(i)) * sCircleDiameter / 2f + centerY;
//                    mWavePath.quadTo((float) x, (float)y, (float)nx, (float)ny);
//                    x = nx;
//                    y = ny;
                    mCanvas.drawPoint((float)nx, (float)ny, mWavePaint);
                }

//                wave.cubicTo();
//                mCanvas.drawPath(mWavePath, mWavePaint);

                Thread.sleep(10); // 这个就相当于帧频了，数值越小画面就越流畅

                mMatrix.postRotate(1f, mRect.width() / 2f, mRect.height() / 2f);
            } catch (Exception e) {
                Log.e(TAG, "run: ", e);
            } finally {
                if (mCanvas != null)
                    mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }
}
