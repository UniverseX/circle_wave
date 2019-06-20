package com.autoai.circlewave;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WaveSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "WaveSurfaceView";

    private SurfaceHolder mHolder;
    private Thread drawThread;
    private Canvas mCanvas;
    private Paint mPanit;
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

    public WaveSurfaceView(Context context) {
        this(context, null);
    }

    public WaveSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRun = true;
        mMatrix = new Matrix();
        mPanit = new Paint();
        mPanit.setAntiAlias(true);
        mPanit.setFilterBitmap(true);
        mCanvasAntiFilter = new PaintFlagsDrawFilter(0,Paint.FILTER_BITMAP_FLAG);

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
//                mCanvas.drawBitmap(bg, null, mRect, bgPaint);

                //draw circle
                mCanvas.drawBitmap(circle, mMatrix, mPanit);

                //draw triangle


                //draw wave

                Thread.sleep(10); // 这个就相当于帧频了，数值越小画面就越流畅

                mMatrix.postRotate(0.1f, mRect.width() / 2f, mRect.height() / 2f);
//                mMatrix.postRotate(30, mCircleLeft, mCircleTop);
            } catch (Exception e) {
                Log.e(TAG, "run: ", e);
            } finally {
                if (mCanvas != null)
                    mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }
}
