package com.autoai.circlewave;

import android.graphics.PointF;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void test() {
        PointF[] cp = {new PointF(10, 100),
                new PointF(30, 20),
                new PointF(120, 20),
                new PointF(200, 100)};
        int number = 100;
        PointF[] curve = new PointF[number];
        ComputeBezier(cp, number, curve); //因为是数组，所以不用加星号。
        for (int i = 0; i < number; i++) {
            System.out.printf("curve[%d].x=%f,curve[%d].y=%f\n", i, curve[i].x, i, curve[i].y);
            System.out.println();
        }
    }


    PointF PointOnCubicBezier(PointF[] cp, double t) {

        double ax, bx, cx;
        double ay, by, cy;
        double tSquared, tCubed;
        PointF result = new PointF();
        /*計算多項式係數*/
        cx = 3.0 * (cp[1].x - cp[0].x);
        bx = 3.0 * (cp[2].x - cp[1].x) - cx;
        ax = cp[3].x - cp[0].x - cx - bx;
        cy = 3.0 * (cp[1].y - cp[0].y);
        by = 3.0 * (cp[2].y - cp[1].y) - cy;
        ay = cp[3].y - cp[0].y - cy - by;

        /*計算位於參數值t的曲線點*/
        tSquared = t * t;
        tCubed = tSquared * t;
        result.x = (float) ((ax * tCubed) + (bx * tSquared) + (cx * t) + cp[0].x);
        result.y = (float) ((ay * tCubed) + (by * tSquared) + (cy * t) + cp[0].y);

        return result;

    }


    void ComputeBezier(PointF[] cp, int numberOfPoints, PointF[] curve) {

        double dt = 1.0f / (numberOfPoints - 1);
        int i;

        for (i = 0; i < numberOfPoints; i++)
            curve[i] = PointOnCubicBezier(cp, i * dt);

    }
}