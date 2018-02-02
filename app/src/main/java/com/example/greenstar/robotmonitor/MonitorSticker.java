package com.example.greenstar.robotmonitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by GreenStar on 2018/1/4.
 */

public class MonitorSticker extends View {

    private boolean flag;
    private static float bigCircleX =100;
    private static float bigCircleY =100;
    private static float bigCircleR =60;
    //摇杆的X,Y坐标以及摇杆的半径
    private static float smallCircleX = 100;
    private static float smallCircleY = 100;
    private static float smallCircleR = 30;
    public int DirectionRad=0;
    //自定义控件监听
    private OnDownActionListener mDown = null;
    private OnMoveActionListener mMove = null;
    private OnUpActionListener mUp = null;
    //setup painter
    Paint paint=new Paint();
    //DisplayMetrics dm;
    public  MonitorSticker(Context context)
    {
        super(context);
    }
    public  MonitorSticker(Context context, AttributeSet set)
    {
        super(context,set);
    }

    public void AdaptScreen(float screenX,float screenY)
    {
        bigCircleX= bigCircleX*screenX*3/40;
        bigCircleY= bigCircleY*screenY*7/8;
        bigCircleR=bigCircleR* 400*(float) Math.sqrt(5)/(float)Math.sqrt(Math.pow(screenX, 2) + Math.pow(screenY, 2));
        smallCircleX= smallCircleX*screenX*3/40;
        smallCircleY= smallCircleY*screenY*7/8;
        smallCircleR=smallCircleR* 400*(float) Math.sqrt(5)/(float)Math.sqrt(Math.pow(screenX, 2) + Math.pow(screenY, 2));
    }
    /* @Override
   * protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
     {;
         int widthSize = MeasureSpec.getSize(widthMeasureSpec);
         int heightSize = MeasureSpec.getSize(heightMeasureSpec);
         AdaptScreen(heightSize,widthSize);

     }*/
    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        paint.setColor(Color.GREEN);
        canvas.drawCircle(bigCircleX,bigCircleY,bigCircleR,paint);
        paint.setColor(Color.RED);
        canvas.drawCircle(smallCircleX,smallCircleY,smallCircleR,paint);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float temRad=0;
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            // 在范围外触摸
            if (Math.sqrt(Math.pow((bigCircleX - (int) event.getX()), 2) + Math.pow((bigCircleY - (int) event.getY()), 2)) >= bigCircleR) {
               temRad = getRad(bigCircleX, bigCircleY, event.getX(), event.getY());
                getXY(bigCircleX, bigCircleY, bigCircleR,  temRad);
                DirectionRad=(int)(temRad/3.14*180);
                mMove.OnMove();
            } else {//范围内触摸
                smallCircleX = (int) event.getX();
                smallCircleY = (int) event.getY();
                temRad = getRad(bigCircleX, bigCircleY, smallCircleX, smallCircleY);
                DirectionRad=(int)(temRad/3.14*180);
                mMove.OnMove();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            smallCircleX = bigCircleX;
            smallCircleY = bigCircleY;
            DirectionRad=0;
            mUp.OnUp();
        }
    invalidate();
    return true;
    }
    //get the rad
    public float getRad(float px1, float py1, float px2, float py2) {
        float x = px2 - px1;
        float y = py2- py1;
        //斜边的长
        float z = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        float cosAngle = x / z;
        float rad=(float) Math.acos(cosAngle);
        if(y<0) {
            rad = (float)6.28-(float) Math.acos(cosAngle);
        }
        return rad;
    }
    //get the location
    public void getXY(float x, float y, float R, double rad) {
        //获取圆周运动的X坐标
        smallCircleX = (float) (R * Math.cos(rad)) + x;
        //获取圆周运动的Y坐标
        smallCircleY = (float) (R * Math.sin(rad)) + y;
    }
    // 为每个接口设置监听器
   public void setOnDownActionListener(OnDownActionListener down) {
        this.mDown = down;
    }
    public void setOnMoveActionListener(OnMoveActionListener move) {
        this.mMove = move;
    }
    public void setOnUpActionListener(OnUpActionListener up) {
        this.mUp = up;
    }
    // 定义三个接口
    public interface OnDownActionListener {
        public void OnDown();
    }

    public interface OnMoveActionListener {
        public void OnMove();
    }

    public interface OnUpActionListener {
        public void OnUp();
    }
}
