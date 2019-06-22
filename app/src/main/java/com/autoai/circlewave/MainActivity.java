package com.autoai.circlewave;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.autoai.circlewave.effects.EffectFactory;
import com.autoai.circlewave.widgets.EffectSurfaceView;

public class MainActivity extends AppCompatActivity {

    private MediaManager mediaManager;
    private EffectSurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_DENIED
         || PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }else {
            setContentView(R.layout.activity_main);
            surfaceView = (EffectSurfaceView) findViewById(R.id.surface_view);
            surfaceView.setEffect(EffectFactory.getEffect(this, EffectFactory.EffectType.LINE_WAVE));
            mediaManager = new MediaManager().create(this, R.raw.superheroes, surfaceView);
        }
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
            setContentView(R.layout.activity_main);
            mediaManager = new MediaManager().create(this, R.raw.superheroes, (EffectSurfaceView) findViewById(R.id.surface_view));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaManager.release();
    }
}
