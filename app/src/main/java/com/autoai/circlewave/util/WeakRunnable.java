package com.autoai.circlewave.util;

import java.lang.ref.WeakReference;

public abstract class WeakRunnable<T> implements Runnable {
    private WeakReference<T> weakRef;
    public WeakRunnable(T t){
        weakRef = new WeakReference<>(t);
    }
    @Override
    public void run() {
        if(weakRef.get() == null){
            return;
        }
        run(weakRef.get());
    }

    public abstract void run(T t);
}
