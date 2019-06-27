package com.autoai.circlewave;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.autoai.circlewave.effects.Effect;
import com.autoai.circlewave.effects.EffectFactory;
import com.autoai.circlewave.util.WeakRunnable;
import com.autoai.circlewave.widgets.EffectSurfaceView;
import com.autoai.circlewave.widgets.RotateImageView;

public class MainActivity extends AppCompatActivity {

    private MediaManager mediaManager;
    private EffectSurfaceView surfaceView;
    private ImageView ivBg;
    private RotateImageView ivRotate;
    private Effect effect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_DENIED
         || PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }else {
            init();
        }
    }

    private void init() {
        setContentView(R.layout.activity_main);
        effect = EffectFactory.getEffect(this, EffectFactory.EffectType.WATER_WAVE,
                BitmapFactory.decodeResource(getResources(), R.mipmap.a));
        surfaceView = findViewById(R.id.surface_view);
        surfaceView.setEffect(effect);
        mediaManager = new MediaManager().create(this, R.raw.yicijiuhao, surfaceView);
    }

    public Effect chooseEffect(int type){
        return EffectFactory.getEffect(this, type,
                BitmapFactory.decodeResource(getResources(), R.mipmap.d));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean result = true;
        if(requestCode == 100) {
            for (int i = 0; i < grantResults.length; i++) {
                if(PermissionChecker.PERMISSION_GRANTED != grantResults[i]){
                    result = false;
                    break;
                }
            }
        }
        if(result){
            init();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaManager.release();
    }

    public void setBackground(){
        new Thread(new WeakRunnable<View>(ivBg) {
            @Override
            public void run(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    final Bitmap bitmap = effect.blurBg(BitmapFactory.decodeResource(view.getResources(), R.mipmap.bg));
                    view.post(new WeakRunnable<View>(view) {
                        @Override
                        public void run(View view1) {
                            view1.setBackground(new BitmapDrawable(bitmap));
                        }
                    });
                }
            }
        }).start();
    }

    public void setRotateView(){
        new Thread(new WeakRunnable<ImageView>(ivRotate) {

            @Override
            public void run(ImageView view) {
                final Bitmap circle = effect.clipCircle(BitmapFactory.decodeResource(view.getResources(), R.mipmap.bg));
                view.post(new WeakRunnable<ImageView>(view) {
                    @Override
                    public void run(ImageView imageView) {
                        imageView.setImageBitmap(circle);
                    }
                });
            }
        }).start();
    }

    public void waterWave(View view) {
        effect = EffectFactory.getEffect(this, EffectFactory.EffectType.WATER_WAVE,
                BitmapFactory.decodeResource(getResources(), R.mipmap.a));
        surfaceView.setEffect(effect);
    }

    public void lineWave(View view) {
        effect = EffectFactory.getEffect(this, EffectFactory.EffectType.LINE_WAVE,
                BitmapFactory.decodeResource(getResources(), R.mipmap.b));
        surfaceView.setEffect(effect);
    }

    public void planet(View view) {
        effect = EffectFactory.getEffect(this, EffectFactory.EffectType.PLANET,
                BitmapFactory.decodeResource(getResources(), R.mipmap.c));
        surfaceView.setEffect(effect);
    }

    public void DynamicScale(View view) {
        effect = EffectFactory.getEffect(this, EffectFactory.EffectType.DYNAMIC_SCALE,
                BitmapFactory.decodeResource(getResources(), R.mipmap.d));
        surfaceView.setEffect(effect);
    }

    public void exp_particle(View view) {
        effect = EffectFactory.getEffect(this, EffectFactory.EffectType.EXPLOSIVE_PARTICLE,
                BitmapFactory.decodeResource(getResources(), R.mipmap.e));
        surfaceView.setEffect(effect);
    }
}
