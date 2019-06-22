package com.autoai.circlewave.widgets;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.autoai.circlewave.R;
import com.autoai.circlewave.effects.Effect;
import com.autoai.circlewave.effects.LineWaveEffect;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class WaveSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "WaveSurfaceView";

    private SurfaceHolder mHolder;
    private Thread drawThread;
    private volatile boolean isRun = true;
    private Canvas mCanvas;
    private Effect effect;
    private Visualizer visualizer;

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
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//deprecated
//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        this.setKeepScreenOn(true);
        effect = new LineWaveEffect(context, BitmapFactory.decodeResource(context.getResources(), R.mipmap.bg));
        media();
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
        effect.setSurfaceRectF(new RectF(0, 0, width, height));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRun = false;
    }

    public void setEffect(Effect effect) {
        this.effect = effect;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void run() {
        while (isRun) {
            try {
                mCanvas = mHolder.lockCanvas();
                if(mCanvas == null){
                    break;
                }
                effect.draw(mCanvas);
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
            }
        });
        visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
//采样的最大值
        int captureSize = Visualizer.getCaptureSizeRange()[1];
        //采样的频率
        int captureRate = Visualizer.getMaxCaptureRate() * 3 / 4;
        visualizer.setCaptureSize(captureSize);
        visualizer.setDataCaptureListener(dataCaptureListener, captureRate, true, true);
        visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
        visualizer.setEnabled(true);
        visualizer.setEnabled(true);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mediaPlayer){
                visualizer.setEnabled(false);
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
            if(effect != null){
                effect.setByte(fft);
            }
        }
    };


}
