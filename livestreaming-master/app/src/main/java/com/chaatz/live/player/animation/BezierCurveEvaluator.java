package com.chaatz.live.player.animation;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * Created by Ravic on 12/1/2016.
 * COMPS384F Page 24/36.
 *              c(0.2) = 0.512*(100,260) +
 *                       0.384*(160,85) +
 *                       0.096*(255,345) +
 *                       0.008*(404,260)
 *                     = (51.2, 133.12) +
 *                       (61.44, 32.64) +
 *                       (24.48, 33.12) +
 *                       (3.232, 2.08)
 *                     = (140.352, 200.96)
 */
public class BezierCurveEvaluator implements TypeEvaluator< PointF > {

    private PointF pointF1;
    private PointF pointF2;

    public BezierCurveEvaluator( PointF pointF1, PointF pointF2 ) {
        this.pointF1 = pointF1;
        this.pointF2 = pointF2;
    }

    @Override
    public PointF evaluate( float time, PointF startValue, PointF endValue ) {

        float timeLeft = 1.0f - time;
        PointF point = new PointF();

        point.x = timeLeft * timeLeft * timeLeft * ( startValue.x )
                + 3 * timeLeft * timeLeft * time * ( pointF1.x )
                + 3 * timeLeft * time * time * ( pointF2.x )
                + time * time * time * ( endValue.x );

        point.y = timeLeft * timeLeft * timeLeft * ( startValue.y )
                + 3 * timeLeft * timeLeft * time * ( pointF1.y )
                + 3 * timeLeft * time * time * ( pointF2.y )
                + time * time * time * ( endValue.y );
        return point;
    }
}

