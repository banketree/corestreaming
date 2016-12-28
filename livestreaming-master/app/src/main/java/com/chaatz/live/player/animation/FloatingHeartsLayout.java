package com.chaatz.live.player.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.chaatz.live.R;

import java.util.Random;

/**
 * Created by Ravic on 12/1/2016.
 * Custom made hearts animations like Periscope for Chaatz-Live. (Bezier curve approach)
 */
public class FloatingHeartsLayout extends RelativeLayout {

    private Interpolator mLine = new LinearInterpolator();
    private Interpolator mAccelerator = new AccelerateInterpolator();
    private Interpolator mDecelerator = new DecelerateInterpolator();
    private Interpolator mBooster = new AccelerateDecelerateInterpolator();
    private Interpolator[] mInterpolatorArray;

    private int mWidth = 0;
    private int mHeight = 0;
    private int iconWidth = 0;
    private int iconHeight = 0;
    private LayoutParams mLayoutParams;
    private Drawable[] mDrawablesArray;
    private Random random = new Random();

    public FloatingHeartsLayout( Context context ) {
        super( context );
        init();
    }

    public FloatingHeartsLayout( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init();
    }

    public FloatingHeartsLayout( Context context, AttributeSet attrs, int defStyleAttr ) {
        super( context, attrs, defStyleAttr );
        init();
    }

    @TargetApi( Build.VERSION_CODES.LOLLIPOP )
    public FloatingHeartsLayout( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes ) {
        super( context, attrs, defStyleAttr, defStyleRes );
        init();
    }

    public void init() {
        // Initialize range of hearts images.
        mDrawablesArray = new Drawable[ 3 ];
        Drawable red = getResources().getDrawable( R.drawable.pl_red );
        Drawable blue = getResources().getDrawable( R.drawable.pl_blue );
        Drawable yellow = getResources().getDrawable( R.drawable.pl_yellow );
        mDrawablesArray[ 0 ] = red;
        mDrawablesArray[ 1 ] = blue;
        mDrawablesArray[ 2 ] = yellow;

        // Take dimension samples of hearts image.
        iconHeight = red.getIntrinsicHeight();
        iconWidth = blue.getIntrinsicWidth();

        mLayoutParams = new LayoutParams( iconWidth, iconHeight );
        mLayoutParams.addRule( CENTER_HORIZONTAL, TRUE );
        mLayoutParams.addRule( ALIGN_PARENT_BOTTOM, TRUE );

        // Initialize interpolators.
        mInterpolatorArray = new Interpolator[ 4 ];
        mInterpolatorArray[ 0 ] = mLine;
        mInterpolatorArray[ 1 ] = mAccelerator;
        mInterpolatorArray[ 2 ] = mDecelerator;
        mInterpolatorArray[ 3 ] = mBooster;
    }

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
        super.onMeasure( widthMeasureSpec, heightMeasureSpec );
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    public void addHeart() {
        ImageView imageView = new ImageView( getContext() );

        //Generate heart images randomly
        imageView.setImageDrawable( mDrawablesArray[ random.nextInt( 3 ) ] );
        imageView.setLayoutParams( mLayoutParams );

        addView( imageView );

        Animator set = getAnimator( imageView );
        set.addListener( new AnimationEndListener( imageView ) );
        set.start();
    }

    private Animator getAnimator( View target ) {
        AnimatorSet set = getEnterAnimator( target );

        ValueAnimator bezierValueAnimator = getBezierValueAnimator( target );

        AnimatorSet finalSet = new AnimatorSet();
        finalSet.playSequentially( set );
        finalSet.playSequentially( set, bezierValueAnimator );
        finalSet.setInterpolator( mInterpolatorArray[ random.nextInt( 4 ) ] );
        finalSet.setTarget( target );
        return finalSet;
    }

    private AnimatorSet getEnterAnimator( final View target ) {

        ObjectAnimator alpha = ObjectAnimator.ofFloat( target, View.ALPHA, 0.2f, 1f );
        ObjectAnimator scaleX = ObjectAnimator.ofFloat( target, View.SCALE_X, 0.2f, 1f );
        ObjectAnimator scaleY = ObjectAnimator.ofFloat( target, View.SCALE_Y, 0.2f, 1f );
        AnimatorSet enter = new AnimatorSet();
        enter.setDuration( 500 );
        enter.setInterpolator( new LinearInterpolator() );
        enter.playTogether( alpha, scaleX, scaleY );
        enter.setTarget( target );
        return enter;
    }

    private ValueAnimator getBezierValueAnimator( View target ) {

        BezierCurveEvaluator evaluator = new BezierCurveEvaluator( getPointF( 2 ), getPointF( 1 ) );

        ValueAnimator animator = ValueAnimator.ofObject( evaluator, new PointF( ( mWidth - iconWidth ) / 2, mHeight - iconHeight ), new PointF( random.nextInt( getWidth() ), 0 ) );
        animator.addUpdateListener( new BezierCurveListener( target ) );
        animator.setTarget( target );
        animator.setDuration( 3000 );
        return animator;
    }

    private PointF getPointF( int scale ) {
        PointF pointF = new PointF();
        pointF.x = random.nextInt( ( mWidth - 100 ) );
        pointF.y = random.nextInt( ( mHeight - 100 ) ) / scale;
        return pointF;
    }

    private class BezierCurveListener implements ValueAnimator.AnimatorUpdateListener {
        private View anchor;

        public BezierCurveListener( View anchor ) {
            this.anchor = anchor;
        }

        @Override
        public void onAnimationUpdate( ValueAnimator animation ) {
            PointF pointF = ( PointF ) animation.getAnimatedValue();
            anchor.setX( pointF.x );
            anchor.setY( pointF.y );
            anchor.setAlpha( 1 - animation.getAnimatedFraction() );
        }
    }


    private class AnimationEndListener extends AnimatorListenerAdapter {
        private View anchor;

        public AnimationEndListener( View anchor ) {
            this.anchor = anchor;
        }

        @Override
        public void onAnimationEnd( Animator animation ) {
            super.onAnimationEnd( animation );
            removeView( ( anchor ) );
        }
    }
}
