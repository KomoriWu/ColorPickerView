package com.komoriwu.colorpickerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class RotateView extends View {
    public static final String TAG = RotateView.class.getName();
    /**
     * 原心坐标x
     */
    float o_x;

    /**
     * 原心坐标y
     */
    float o_y;

    float width;

    float height;

    // view的真实宽度与高度:因为是旋转，所以这个view是正方形，它的值是图片的对角线长度
    double maxwidth;

    //取色盘画笔
    private Paint mPaint;
    //当前颜色值的小球
    private Paint mCenterPaint;
    private int[] mColors;
    private float mRadius;
    private static final int CENTER_RADIUS = 30;
    private static final float PI = 3.1415926f;

    public RotateView(Context context) {
        this(context, null);
    }

    public RotateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.
                ColorPickerView, defStyleAttr, 0);
        int indexCount = a.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.ColorPickerView_radius:
                    mRadius = a.getDimensionPixelSize(attr, 0);
                    break;
            }
        }
        a.recycle();

        init();
    }

    @SuppressLint("HandlerLeak")
    private void init() {
        //初始化取色盘
        mColors = new int[]{0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xff00e8ff, 0xFF00FFFF, 0xFF00FF00,
                0xFFFFFFFF, 0xffe5ff00, 0xFFFFFF00, 0xFFffcc00, 0xFFFF0000};
        Shader s = new SweepGradient(0, 0, mColors, null);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(s);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mRadius/2.5f);

        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setColor(0xFF000000);
        mCenterPaint.setStrokeWidth(5);

        initSize();
    }

    private void initSize() {
        width = mRadius * 2;
        height = mRadius * 2;

        maxwidth = Math.sqrt(width * width + height * height);
        o_x = o_y = (float) (maxwidth / 2);//确定圆心坐标
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.rotate(deta_degree);
        canvas.drawCircle(0, 0, mRadius, mPaint);
        canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);

        super.onDraw(canvas);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension((int) maxwidth, (int) maxwidth);
    }

    /**
     * 通过此方法来控制旋转度数，如果超过360，让它求余，防止，该值过大造成越界
     *
     * @param added
     */
    private void addDegree(float added) {
        deta_degree += added;
        if (deta_degree > 360 || deta_degree < -360) {
            deta_degree = deta_degree % 360;
        }

    }

    /**
     * 手指触屏的初始x的坐标
     */
    float down_x;

    /**
     * 手指触屏的初始y的坐标
     */
    float down_y;

    /**
     * 移动时的x的坐标
     */
    float target_x;

    /**
     * 移动时的y的坐标
     */
    float target_y;


    /**
     * 当前的弧度(以该 view 的中心为圆点)
     */
    float current_degree;

    /**
     * 当前圆盘所转的弧度(以该 view 的中心为圆点)
     */
    float deta_degree;
    float x = 500, y = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                down_x = event.getX();
                down_y = event.getY();
                current_degree = detaDegree(o_x, o_y, down_x, down_y);
                break;

            }
            case MotionEvent.ACTION_MOVE: {
                down_x = target_x = event.getX();
                down_y = target_y = event.getY();
                float degree = detaDegree(o_x, o_y, target_x, target_y);

                // 滑过的弧度增量
                float dete = degree - current_degree;
                if (dete < -270) {// 如果小于-90度说明 它跨周了，需要特殊处理350->17,
                    dete = dete + 360;
                } else if (dete > 270) {// 如果大于90度说明 它跨周了，需要特殊处理-350->-17,
                    dete = dete - 360;
                }

                addDegree(dete);
                current_degree = degree;

                Log.d(TAG, "deta_degree:" + deta_degree);

                if (deta_degree < 0) {
                    x = -(float) (mRadius * Math.sin(PI * deta_degree / 180));
                    y = -(float) (mRadius * Math.cos(PI * deta_degree / 180));
                } else {
                    if (deta_degree < 90) { //取原本二象限的值
                        x = -(float) (mRadius * Math.sin(PI * deta_degree / 180));
                        y = -(float) (mRadius * Math.cos(PI * deta_degree / 180));
                    } else if (deta_degree < 180) { //取原本三象限的值
                        y = (float) (mRadius * Math.sin(PI * (deta_degree - 90) / 180));
                        x = -(float) (mRadius * Math.cos(PI * (deta_degree - 90) / 180));
                    } else if (deta_degree < 270) { //取原本四象限的值
                        x = (float) (mRadius * Math.sin(PI * (deta_degree - 180) / 180));
                        y = (float) (mRadius * Math.cos(PI * (deta_degree - 180) / 180));
                    } else if (deta_degree < 360) { //取原本-象限的值
                        y = -(float) (mRadius * Math.sin(PI * (deta_degree - 270) / 180));
                        x = (float) (mRadius * Math.cos(PI * (deta_degree - 270) / 180));
                    }
                }
                Log.d(TAG, "x:" + x + "----" + "y:" + y);
                float angle = (float) Math.atan2(y, x);
                float unit = angle / (2 * PI);
                if (unit < 0) {
                    unit += 1;
                }
                mCenterPaint.setColor(interpColor(mColors, unit));
                postInvalidate();
                break;
            }

        }
        return true;
    }

    private int interpColor(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }

        float p = unit * (colors.length - 1);
        int i = (int) p;
        p -= i;

        // now p is just the fractional part [0...1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        return Color.argb(a, r, g, b);
    }

    private int ave(int s, int d, float p) {
        return s + Math.round(p * (d - s));
    }

    /**
     * 计算以(src_x,src_y)为坐标圆点，建立直角体系，求出(target_x,target_y)坐标与x轴的夹角
     * 主要是利用反正切函数的知识求出夹角
     */
    float detaDegree(float src_x, float src_y, float target_x, float target_y) {

        float detaX = target_x - src_x;
        float detaY = target_y - src_y;
        double d;
        if (detaX != 0) {
            float tan = Math.abs(detaY / detaX);

            if (detaX > 0) {

                if (detaY >= 0) {
                    d = Math.atan(tan);

                } else {
                    d = 2 * Math.PI - Math.atan(tan);
                }

            } else {
                if (detaY >= 0) {

                    d = Math.PI - Math.atan(tan);
                } else {
                    d = Math.PI + Math.atan(tan);
                }
            }

        } else {
            if (detaY > 0) {
                d = Math.PI / 2;
            } else {

                d = -Math.PI / 2;
            }
        }

        return (float) ((d * 180) / Math.PI);
    }


}
