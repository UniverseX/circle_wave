package com.autoai.circlewave;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.root);

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
}
