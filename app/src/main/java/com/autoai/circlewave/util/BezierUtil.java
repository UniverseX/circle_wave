package com.autoai.circlewave.util;

import android.graphics.PointF;

public class BezierUtil {
    /**
     * 计算控制点 如果计算 p2 , p3之间的控制点，则需要计算p1, p2, p3, p4四个点(三阶)
     */
    public static PointF[] getCtrlPoint(PointF[] ps, int i){

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

    /**
     *
     */

}
