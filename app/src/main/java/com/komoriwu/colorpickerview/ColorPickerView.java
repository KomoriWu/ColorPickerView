package com.komoriwu.colorpickerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by KomoriWu
 * on 2018/7/8.
 */
public class ColorPickerView extends View {
    private Paint mPaint;
    private Bitmap mBitmap;
    private Context mContext;
    /**
     * 触摸点的坐标位置
     **/
    private float touchX;
    private float touchY;
    /**
     * 控件的中心位置,处于中心位置。x和y是相等的
     */
    private int mCenter;
    private volatile float mStartAngle = 0;

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
        mPaint = new Paint();
        mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.color);
        mCenter=mBitmap.getWidth()/2;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.translate((getWidth()-mBitmap.getWidth())/2,(getHeight()-mBitmap.getHeight())/2);
        canvas.rotate(mStartAngle, mCenter, mCenter);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = event.getX();
                touchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                //得到旋转的角度
                float arc = getRoundArc(touchX, touchY, moveX, moveY);
                //重新赋值
                touchX = moveX;
                touchY = moveY;
                //起始角度变化下，然后进行重新绘制
                mStartAngle += arc;
                invalidate();
                break;

        }
        return true;
    }


    //根据三点的坐标计算旋转的角度
    private float getRoundArc(float startX, float startY, float endX, float endY) {
        float arc = 0;
        //首先计算三边的长度
        float a = (float) Math.sqrt(Math.pow(startX - mCenter, 2) + Math.pow(startY - mCenter, 2));
        float b = (float) Math.sqrt(Math.pow(endX - mCenter, 2) + Math.pow(endY - mCenter, 2));
        float c = (float) Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
        //判断是否为三角形
        if (a + b > c) {//两边之和大于第三边为三角形
            /**
             * 接下来计算角度
             *
             * acos((a2+b2-c2)/2ab)
             *
             * **/
            arc = (float) (Math.acos((Math.pow(a, 2) + Math.pow(b, 2) - Math.pow(c, 2)) / (2 * a * b)) * 180 / Math.PI);

            if (startX <= mCenter && endX >= mCenter && startY < mCenter && endY < mCenter) {//上边顺时针越界，不管他
            } else if (startX >= mCenter && endX <= mCenter && startY < mCenter && endY < mCenter) {//上边逆时针越界
                arc = -arc;
            } else if (startX <= mCenter && endX >= mCenter && startY > mCenter && endY > mCenter) {//下边逆时针越界
                arc = -arc;
            } else if (startX <= mCenter && endX >= mCenter && startY < mCenter && endY < mCenter) {//下边顺时针越界，不管他
            } else if (endX >= mCenter && startX >= mCenter) {//这个时候表示在右半区
                if (startY > endY) {
                    arc = -arc;
                }
            } else if (endX < mCenter && startX < mCenter) {//此时在左半区
                if (startY < endY) {
                    arc = -arc;
                }
            }
        }
        if (Math.abs(arc) >= 0 && Math.abs(arc) <= 180) {//主要解决nan的问题
            return arc;
        } else {
            return 0;
        }
    }

}
