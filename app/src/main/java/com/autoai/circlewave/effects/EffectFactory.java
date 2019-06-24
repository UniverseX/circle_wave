package com.autoai.circlewave.effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.autoai.circlewave.R;

public class EffectFactory {
    public static interface EffectType {
        /**
         * 迷幻水波
         */
        int WATER_WAVE = 0;
        /**
         * 跳动旋律
         */
        int LINE_WAVE = WATER_WAVE + 1;
        /**
         * 孤独星球
         */
        int PLANET = LINE_WAVE + 1;
        /**
         * 动感音阶
         */
        int DYNAMIC_SCALE = PLANET + 1;
        /**
         * 爆炸粒子
         */
        int EXPLOSIVE_PARTICLE = DYNAMIC_SCALE + 1;
    }


    public static Effect getEffect(Context context, int type, Bitmap bitmap) {
        switch (type) {
            case EffectType.WATER_WAVE:
                return new WaterWave(context, bitmap);
            case EffectType.LINE_WAVE:
                return new LineWave(context, bitmap);
            case EffectType.PLANET:
                return new Planet(context, bitmap);
            case EffectType.DYNAMIC_SCALE:
                return new DynamicScale(context, bitmap);
            case EffectType.EXPLOSIVE_PARTICLE:
                return new ExplosiveParticle(context, bitmap);

        }
        return new LineWave(context, bitmap);
    }
}
