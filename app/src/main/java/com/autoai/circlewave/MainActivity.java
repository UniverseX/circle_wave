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

import com.autoai.circlewave.util.BitmapUtil;
import com.autoai.circlewave.util.WeakRunnable;

public class MainActivity extends AppCompatActivity {

    private View root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_DENIED
         || PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }else {
            setContentView(R.layout.activity_main);
            root = findViewById(R.id.root);
        }

//        new Thread(new WeakRunnable<View>(root) {
//            @Override
//            public void run(View view) {
//                Bitmap bg_src = BitmapFactory.decodeResource(view.getResources(), R.mipmap.bg);
//                final Bitmap bg = BitmapUtil.rsBlur(root.getContext(), bg_src, 0, 1);
//                view.post(new WeakRunnable<View>(view) {
//                    @Override
//                    public void run(View view1) {
//                        view1.setBackground(new BitmapDrawable(bg));
//                    }
//                });
//            }
//        }).start();
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
            root = findViewById(R.id.root);
        }

    }
}
