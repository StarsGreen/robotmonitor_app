package com.example.greenstar.robotmonitor;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.BitmapFactory;
/**
 * Created by GreenStar on 2018/1/15.
 */


public class RtVideoView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    //Threading
    private SurfaceHolder holder;
    private Thread thread;
    public Handler RecHandler;
    mThread mthread=new mThread();

    public byte[][] video0=new byte[3][1024*1024];
    private static int index=0;
    public byte[] video1=new byte[1024*1024];
    private Bitmap Btmap;

    private static int mSurfaceViewWidth,mSurfaceViewHeight ;
    private static int PicWidth=320,PicHeight=240 ;
    private static long lastDown = 0;


    public boolean running=false;
    public boolean ShowEnable=false;

    private OnDoubleClickListener onDoubleClick = null;

    public void setOnDoubleClickListener(OnDoubleClickListener onDobClick) {
        this.onDoubleClick = onDobClick;
    }

    public RtVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Get surface holder
        holder = getHolder();
        holder.addCallback(this);
        //holder.setFixedSize(mSurfaceViewWidth,mSurfaceViewHeight);
        thread = new Thread(this);
        mthread.start();
        //Background
    }

    public void SetFullScreen(int width,int height)
    {
        this.mSurfaceViewHeight=height;
        this.mSurfaceViewWidth=width;

    }
    //////////////////////////////////////////
    public void StartPlay()
    {
        running=true;
        thread.start();
    }
    ////////////////////////////////////////
    public void StopPlay()
    {
        running=false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent e) {

        long DOUBLE_TIME = 500;
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            long nowDown = System.currentTimeMillis();

            if (nowDown - lastDown < DOUBLE_TIME)
            {
                onDoubleClick.OnDoubleClick();
            } else
                lastDown = nowDown;
        }
        return false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            running=false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    ////////////////////////////////////////////////////
    public int  getLength(byte[] b)
    {
        int num=0;
        for(int i=0;i<b.length;i++) {
            if (b[i] != 0)
                num++;
            else
            {
                if((i<b.length-2)&&(b[i+1]==0)&&(b[i+2]==0))
                    break;
            }
        }
        return num;
    }
    /////////////////////////////////////////////////
    private void bytesToImageFile(byte[] bytes) {
        try {
            String path=Environment.getExternalStorageDirectory().getAbsolutePath() ;
            File file = new File(path + "/video.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            Log.i("video.jpg path:",path);
            int length=getLength(bytes);
            fos.write(bytes, 0, length);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public class mThread extends Thread
    {
        public void run()
        {
            Looper.prepare();
            RecHandler=new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    if(msg.what==0x02)
                        video1=(byte[]) msg.obj;
                }
            };
            Looper.loop();
        }

    }
    /////////////////////////////////////////////////////
    @Override
    public void run() {
        //Loop while running is true
       // bytesToImageFile(video1);
        while (running) {
            ShowEnable=false;
           //YuvImage yuvimage=new YuvImage(video1, ImageFormat.YUY2, PicWidth, PicHeight, null);
            //ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //yuvimage.compressToJpeg(new Rect(0, 0, PicWidth, PicHeight), 80, baos);  //这里 80 是图片质量，取值范围 0-100，100为品质最高
            //byte[] jdata = baos.toByteArray();
            Bitmap bp=BitmapFactory.decodeByteArray(video1, 0,video1.length);
            Btmap = Bitmap.createScaledBitmap(bp, mSurfaceViewWidth, mSurfaceViewHeight, true);
                Canvas canvas = holder.lockCanvas();
            if(Btmap!=null)
                canvas.drawBitmap(Btmap, 0, 0, null);
                holder.unlockCanvasAndPost(canvas);
            Btmap.recycle();
            ShowEnable=true;

        }
    }


    public interface OnDoubleClickListener {
        public void OnDoubleClick();
    }
}