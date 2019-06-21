package com.autoai.circlewave;

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
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.autoai.circlewave.util.BitmapUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private static final float CIRCLE_DIAMETER = 290f;
    private static final float CIRCLE_STROKE_WIDTH = 2f;
    private static final float WAVE_WIDTH = 2F;
    private static final float WAVE_MAX_HEIGHT = 10F;
    private static final float WAVE_AMPLITUDE_RATIO = 0.15f;
    private float mCircleDiameter;
    private float mCircleStoke;
    private float mWaveWidth;
    private float mWaveMaxHeght;
    private float initX;
    private float initY;
    private List<PointF> animPoint = new ArrayList<>();
    private Visualizer visualizer;
    private float[] mPoints;
    private static volatile byte[] mBytes;


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
        mCircleDiameter = displayMetrics.density * CIRCLE_DIAMETER;
        mCircleStoke = displayMetrics.density * CIRCLE_STROKE_WIDTH;
        mWaveWidth = displayMetrics.density * WAVE_WIDTH;
        mWaveMaxHeght = displayMetrics.density * WAVE_MAX_HEIGHT;

        media();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRun = true;
        mMatrix = new Matrix();
        mCirclePanit = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG/* | Paint.FILTER_BITMAP_FLAG*/);
        mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG /*| Paint.FILTER_BITMAP_FLAG*/);
        mWavePaint.setStrokeWidth(mCircleStoke);
        mWavePaint.setStyle(Paint.Style.STROKE);
        mWavePaint.setStrokeJoin(Paint.Join.ROUND);
        CornerPathEffect cornerPathEffect = new CornerPathEffect(130);
        mWavePaint.setPathEffect(cornerPathEffect);
        mWavePaint.setColor(Color.rgb(0x7f, 0x7f, 0x7f));
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

        randomInitX = mCircleLeft - (mCircleDiameter - src.width()) / 2f;
        initX = mCircleLeft - (mCircleDiameter - src.width()) / 2f;
        initY = mCircleTop - (mCircleDiameter - src.height()) / 2f;
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

                double centerX = mRect.width() / 2f;
                double centerY = mRect.height() / 2f;
                int index = 0;
                int byteIndex = 0;
                PointF[] points = new PointF[20];

                for (float i = 0; i < 360f; i += 18f) {
                    float nx = (float) (Math.cos(Math.toRadians(i)) * mCircleDiameter / 2f + centerX);
                    float ny = (float) (Math.sin(Math.toRadians(i)) * mCircleDiameter / 2f + centerY);
//                    mCanvas.drawPoint((float)nx, (float)ny, mWavePaint);

                    float w_ratioX = 0;
                    float w_ratioY = 0;
                    if(mBytes != null && byteIndex < mBytes.length){
                        byteIndex = (int) (i / 360f * mBytes.length);
                        w_ratioX = (float) (mBytes[index] * WAVE_AMPLITUDE_RATIO * Math.cos(Math.toRadians(i)));
                        w_ratioY = (float) (mBytes[index] * WAVE_AMPLITUDE_RATIO * Math.sin(Math.toRadians(i)));
                    }
                    //TODO 定一个最低的值
                    PointF pointF = new PointF();
                    pointF.x = nx + w_ratioX;
                    pointF.y = ny + w_ratioY;
                    points[index] = pointF;

                    index ++;
                }
                mWavePath.reset();
                mWavePath.moveTo(points[0].x, points[0].y);
                for (int i = 1; i < points.length; i++) {
                    Log.d(TAG, "run: points = " + points[i]);
                    PointF[] pb = getCtrlPoint(points, i);
                    mWavePath.cubicTo(pb[0].x, pb[0].y, pb[1].x, pb[1].y, points[i].x, points[i].y);
                }
                PointF[] pb = getCtrlPoint(points, 0);
                mWavePath.cubicTo(pb[0].x, pb[0].y, pb[1].x, pb[1].y, points[0].x, points[0].y);

                mCanvas.drawPath(mWavePath, mWavePaint);

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

    public void media(){
        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.superheroes);
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
//采样的最大值
                int captureSize = Visualizer.getCaptureSizeRange()[1];
                //采样的频率
                int captureRate = Visualizer.getMaxCaptureRate() * 3 / 4;
                visualizer.setCaptureSize(captureSize);
                visualizer.setDataCaptureListener(dataCaptureListener, captureRate, true, true);
                visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
                visualizer.setEnabled(true);
            }
        });
    }

    private Visualizer.OnDataCaptureListener dataCaptureListener = new Visualizer.OnDataCaptureListener() {
        @Override
        public void onWaveFormDataCapture(Visualizer visualizer, final byte[] waveform, int samplingRate) {
            //到waveform为波形图数据
//            mBytes = waveform;
        }

        @Override
        public void onFftDataCapture(Visualizer visualizer, final byte[] fft, int samplingRate) {
            //FFT数据，展示不同频率的振幅
            mBytes = fft;
        }
    };

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
