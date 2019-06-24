package com.autoai.circlewave;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.support.annotation.RawRes;

import com.autoai.circlewave.media.TunnelPlayerWorkaround;

public class MediaManager {
    private MediaPlayer mPlayer;
    private MediaPlayer mSilentPlayer;  /* to avoid tunnel player issue */
    private Visualizer mVisualizer;

    public MediaManager(){
    }

    /**
     * default MediaPlayer
     */
    public MediaManager create(Context context, @RawRes int res, Visualizer.OnDataCaptureListener dataCaptureListener){
        initTunnelPlayerWorkaround(context);
        init(context, res, dataCaptureListener);
        return this;
    }

    /**
     * other MediaPlayer
     */
    public void createVisualizer(MediaPlayer player, Visualizer.OnDataCaptureListener dataCaptureListener) {
        mPlayer = player;
        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(player.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);


        mVisualizer.setDataCaptureListener(dataCaptureListener,
                Visualizer.getMaxCaptureRate() / 2, true, true);

        // Enabled Visualizer and disable when we're done with the stream
        mVisualizer.setEnabled(true);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mediaPlayer){
                mVisualizer.setEnabled(false);
            }
        });
    }

    private void init(Context context, @RawRes int res, Visualizer.OnDataCaptureListener dataCaptureListener)
    {
        mPlayer = MediaPlayer.create(context, res);
        mPlayer.setLooping(true);
        mPlayer.start();
        createVisualizer(mPlayer, dataCaptureListener);

    }

    private void initTunnelPlayerWorkaround(Context context) {
        // Read "tunnel.decode" system property to determine
        // the workaround is needed
        if (TunnelPlayerWorkaround.isTunnelDecodeEnabled(context)) {
            mSilentPlayer = TunnelPlayerWorkaround.createSilentMediaPlayer(context);
        }
    }

    public void release()
    {
        if (mPlayer != null)
        {
            mVisualizer.release();
            mPlayer.release();
            mPlayer = null;
        }

        if (mSilentPlayer != null)
        {
            mSilentPlayer.release();
            mSilentPlayer = null;
        }
    }
}
