package com.autoai.circlewave.util;

import android.graphics.PointF;

public class BezierUtil {
    /**
     * 计算控制点 如果计算 p2 , p3之间的控制点，则需要计算p1, p2, p3, p4四个点(三阶)
     * @param ps 圆上的点的数组
     * @param i 下一个点(p3)的数组索引
     * @return 返回 p2 , p3之间的控制点
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
     * @param p0 起始点
     * @param p1 控制点1
     * @param p2 控制点2
     * @param p3 终点
     * @param t 比例值
     * @return 三阶贝塞尔曲线 上的点
     */
    public static PointF calCurvePoint(PointF p0, PointF p1, PointF p2, PointF p3, float t) {
        //pt = p0*(1-t)^3 + 3*p1*(1-t)^2*t + 3*p2*(1-t)*t^2 + p3*t^3;
        PointF pt = new PointF();
        pt.x = (float) (p0.x*Math.pow(1-t, 3) + 3*p1.x*Math.pow(1-t,2)*t + 3*p2.x*(1-t)*Math.pow(t,2)+p3.x*Math.pow(t,3));
        pt.y = (float) (p0.y*Math.pow(1-t, 3) + 3*p1.y*Math.pow(1-t,2)*t + 3*p2.y*(1-t)*Math.pow(t,2)+p3.y*Math.pow(t,3));
        return pt;
    }

    /**
     * @param centerX 圆心坐标X
     * @param centerY 圆心坐标y
     * @param radius 半径
     * @param p_other 圆外的一点
     * @return p_other与圆心的连线，与圆的交点
     */
    public static PointF getCirclePoint(float centerX, float centerY, float radius, PointF p_other){
        double distance = Math.sqrt(Math.pow(p_other.x - centerX, 2) + Math.pow(p_other.y - centerY, 2));
        PointF cp = new PointF();
        cp.x = (float) (radius * (p_other.x - centerX) / distance + centerX);
        cp.y = (float) (radius * (p_other.y - centerY) / distance + centerY);
        return cp;
    }

}
