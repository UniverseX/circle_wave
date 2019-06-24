package com.autoai.circlewave.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.autoai.circlewave.effects.Effect;

public class EffectSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable, Visualizer.OnDataCaptureListener {
    private static final String TAG = "WaveSurfaceView";

    private SurfaceHolder mHolder;
    private Thread drawThread;
    private volatile boolean isRun = true;
    private Canvas mCanvas;
    private Effect effect;
    private Visualizer visualizer;

    public EffectSurfaceView(Context context) {
        this(context, null);
    }

    public EffectSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EffectSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.RGBA_8888);
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//deprecated
//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        this.setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRun = true;
//        setZOrderOnTop(true);
        drawThread = new Thread(this);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(effect != null)
            effect.setSurfaceRectF(new RectF(0, 0, width, height));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRun = false;
    }

    public boolean isNeedInvalidate = false;
    public void setEffect(Effect effect) {
        effect.copyFrom(this.effect);
        this.effect = effect;
        isNeedInvalidate = true;
    }

    @Override
    public void run() {
        while (isRun) {
            try {
                if(isNeedInvalidate){
                    effect.invalidate();
                    isNeedInvalidate = false;
                }
                mCanvas = mHolder.lockCanvas();
                if(mCanvas == null){
                    break;
                }
                if(effect != null)
                    effect.draw(mCanvas);

                Thread.sleep(10); // 这个就相当于帧频了，数值越小画面就越流畅
            } catch (Exception e) {
                Log.e(TAG, "run: ", e);
            } finally {
                if (mCanvas != null)
                    mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {

    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        if(effect != null){
            effect.setByte(fft);
        }
    }
}
