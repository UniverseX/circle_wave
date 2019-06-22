package com.autoai.circlewave;

import android.graphics.PointF;

import org.junit.Test;

public class BezierTest {
    @Test
    public void test(){

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
}
