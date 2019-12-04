package brucej.splashview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

/**
 * 粒子扩散效果
 * 小球旋转后扩散，然后小球聚拢成为一个小球，小球位置水波纹扩散显示内容View;
 */
public class SplashView extends View {
    private String TAG = "SplashView--";

    public SplashView(Context context) {
        this(context, null);
    }

    public SplashView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private int[] ballColors = {Color.BLUE, Color.GREEN,
            Color.parseColor("#FF3892"),//pink
            Color.parseColor("#FF9600"),//orange
            Color.parseColor("#02D1AC"),//aqua
            Color.parseColor("#FFD200"),//yellow
    };
    private Paint ballPaint;
    private Paint pointPaint;
    private float pointStroke;
    private float centerLength = 200;
    private int ballRadius = 25;
    private float rotateOffset = 0;
    /**
     * STATE_1 小球旋转
     * STATE_2 小球停止旋转，进行扩散，聚拢
     * STATE_3 水波纹显示内容view
     * STATE_4 完全显示内容view,不再遮挡
     */
    private final byte STATE_1 = 0;
    private final byte STATE_2 = 1;
    private final byte STATE_3 = 2;
    private final byte STATE_4 = 3;
    private byte mState = STATE_1;

    public SplashView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ballPaint = new Paint();
        ballPaint.setStyle(Paint.Style.FILL);
        ballPaint.setStrokeWidth(4);
        ballPaint.setAntiAlias(true);
        //
        pointPaint = new Paint();
        pointPaint.setStyle(Paint.Style.STROKE);
        pointPaint.setAntiAlias(true);
        pointPaint.setColor(Color.WHITE);
        //
        initAnimotor();
    }

    private ValueAnimator state1Animator = ValueAnimator.ofFloat(0, 360);
    private ValueAnimator state2Animator = ValueAnimator.ofFloat(centerLength, 1.8f * centerLength, 0f);
    private ValueAnimator state3Animator = ValueAnimator.ofFloat();

    private void initAnimotor() {
        //
        state1Animator.setRepeatCount(-1);
        state1Animator.setDuration(1000);
        state1Animator.setInterpolator(new AccelerateInterpolator());
        state1Animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rotateOffset = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        //
        state2Animator.setDuration(1000);
        state2Animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                centerLength = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        state2Animator.setInterpolator(new DecelerateInterpolator());
        state2Animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                changeState(STATE_3);
            }
        });
        //
        state3Animator.setDuration(3000);
        state3Animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                pointStroke = f * 2;
                invalidate();
                Log.i(TAG, "diagonalLength/2=" + (diagonalLength / 2) + ",pointStroke=" + pointStroke);
            }
        });
        state3Animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                changeState(STATE_4);
            }
        });

    }

    private void changeState(byte state) {
        mState = state;
        if (mState == STATE_1) {
            state1Animator.start();
        } else if (mState == STATE_2) {
            state1Animator.cancel();
            state2Animator.start();
        } else if (mState == STATE_3) {
            //设置空心圆 半径范围;
            state3Animator.setFloatValues(diagonalLength / 2 - ballRadius, 0);
            state3Animator.start();
        } else if (mState == STATE_4) {
            //重置 属性值
            rotateOffset = 0;
            centerLength = 200;
            pointStroke = 0;
        }
    }

    public void startLoading() {
        changeState(STATE_1);
    }

    public void finishLoading() {
        changeState(STATE_2);
    }

    private float diagonalLength;//View范围的对角线长度
    private int centerX;
    private int centerY;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (diagonalLength == 0) {
            centerX = getMeasuredWidth() / 2;
            centerY = getMeasuredHeight() / 2;
            diagonalLength = (float) Math.pow(
                    Math.pow(getMeasuredWidth(), 2) + Math.pow(getMeasuredHeight(), 2),
                    0.5);
        }
        if (mState == STATE_1 || mState == STATE_2) {
            canvas.drawColor(Color.WHITE);
            for (int i = 0; i < ballColors.length; i++) {
                canvas.save();
                canvas.rotate((i * (360 / ballColors.length) + rotateOffset), centerX, centerY);
                ballPaint.setColor(ballColors[i]);
                canvas.drawCircle(centerX + centerLength, centerY, ballRadius, ballPaint);
                canvas.restore();
            }
        } else if (mState == STATE_3) {
            pointPaint.setStrokeWidth(pointStroke);
            canvas.drawCircle(centerX, centerY, diagonalLength / 2, pointPaint);
        } else {
            //不做处理
        }
    }
}
