package com.example.greenstar.robotmonitor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.WifiManager;


/**
 * Created by GreenStar on 2018/1/4.
 */

public class MonitorActivity extends Activity {


    EditText ed2, ed3;
    TextView tv0,tv1;
    TextView tv3,tv2;
    Button bt1;
    MonitorSticker monitorSticker;
    SeekBar seekBar;
    RtVideoView rtvideoview;

    private SurfaceHolder holder;

    public android.os.Handler Mainhandler;

    public SocketThread sT=new SocketThread();
    public VideoThread vT=new VideoThread();


    static boolean ButtonConClickState = false;
    static boolean RtViewClickState = false;

    static boolean UIstatus=true;
    static int velocity=0;
    static int LastRad=0;
   // static byte[][] VideoStream=new byte[3][1024*1024];
    static byte[] VideoStream=new byte[1024*1024];
    static int VideoIndex=0;

    int screenHeight,screenWidth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        final WifiManager.MulticastLock lock= manager.createMulticastLock("test wifi");

        /*WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight =display.getHeight();*/

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        screenWidth =metric.widthPixels;
        screenHeight =metric.heightPixels;
       // float density =metric.density;
        //int densityDpi =metric.densityDpi;

        ed2 = (EditText) findViewById(R.id.editText2);
        ed3 = (EditText) findViewById(R.id.editText3);
        bt1 = (Button) findViewById(R.id.button1);
        tv0=(TextView)findViewById(R.id.textView0) ;
        tv1=(TextView)findViewById(R.id.textView1) ;
        tv2=(TextView)findViewById(R.id.textView2) ;
        tv3=(TextView)findViewById(R.id.textView3) ;

        seekBar=(SeekBar)findViewById(R.id.seekBar);
        monitorSticker=(MonitorSticker)findViewById(R.id.sticker);
        rtvideoview=(RtVideoView)findViewById(R.id.rtvideoview);

        Mainhandler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what) {
                    case 0x01: {
                        Toast.makeText(MonitorActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
                        tv2.setText(msg.obj.toString());
                        break;
                    }
                    default:break;
                }
            }
        };

        monitorSticker.setOnMoveActionListener(new MonitorSticker.OnMoveActionListener() {
            @Override
            public void OnMove() {
                tv1.setText(String.valueOf(monitorSticker.DirectionRad));
                tv0.setText(String.valueOf(seekBar.getProgress()));
                SendCmd();
            }
        });
        monitorSticker.setOnUpActionListener(new MonitorSticker.OnUpActionListener() {
            @Override
            public void OnUp() {
                tv1.setText(String.valueOf(monitorSticker.DirectionRad));
                SendCmd();
            }
        });
        rtvideoview.setOnDoubleClickListener(new RtVideoView.OnDoubleClickListener() {
            @Override
            public void OnDoubleClick() {
                if (!RtViewClickState) {
                    Toast.makeText(MonitorActivity.this, "video is open", Toast.LENGTH_LONG).show();
                    rtvideoview.SetFullScreen(screenWidth,screenHeight);
                    rtvideoview.StartPlay();
                    RtViewClickState = true;
                } else {
                    Toast.makeText(MonitorActivity.this, "video is closed", Toast.LENGTH_LONG).show();
                  //  vT.SurfaceViewStatus=false;
                    rtvideoview.StopPlay();
                    RtViewClickState = false;
                }
            }

        });

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(MonitorActivity.this,"height is :"+String.valueOf(screenHeight), Toast.LENGTH_LONG).show();
                //Toast.makeText(MonitorActivity.this, "width is :"+String.valueOf(screenWidth), Toast.LENGTH_LONG).show();
               if((!ed2.getText().toString().equals(""))&&(!ed3.getText().toString().equals("")))
               {
                   if (!ButtonConClickState) {
                    bt1.setBackgroundResource(R.mipmap.buttongrey_c);
                    bt1.setText("Close");
                    ButtonConClickState=true;
                    sT.GetIPandAddr(ed2.getText().toString(),Integer.parseInt(ed3.getText().toString()));
                   // vT.rtVideoView.SetFullScreen(screenWidth,screenHeight);
                       if(!sT.ConStatus) {
                           vT.GetHandler(rtvideoview.RecHandler);
                           sT.GetHandler(Mainhandler);
                           new Thread(sT).start();
                       }
                       if(!vT.UdpConState) {
                           lock.acquire();
                           new Thread(vT).start();
                       }
                } else {
                    bt1.setBackgroundResource(R.mipmap.buttonblue_c);
                    bt1.setText("Connect");
                    ButtonConClickState=false;
                    if(sT.ConStatus)
                       sT.SocketClose();
                    if(vT.UdpConState){
                        lock.release();
                       vT.StopUdpSocket();
                    }
                }
            }
             else
             {
                    Toast.makeText(MonitorActivity.this, "IP Or Port Error is Required", Toast.LENGTH_LONG).show();
             }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                velocity=seekBar.getProgress();
                tv0.setText(String.valueOf(seekBar.getProgress()));
            }
        });

    }
    public boolean onTouchEvent(MotionEvent event) {
        if(null != this.getCurrentFocus()){
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super .onTouchEvent(event);
    }

    public String GetCmd(){
            String Cmd="";
            String Tem="";
            if((monitorSticker.DirectionRad-LastRad)>=1) {
                if (monitorSticker.DirectionRad == 0) Tem = "0";
                if ((monitorSticker.DirectionRad > 0) && (monitorSticker.DirectionRad < 45))
                    Tem = "3";
                if ((monitorSticker.DirectionRad >= 45) && (monitorSticker.DirectionRad < 90))
                    Tem = "4";
                if ((monitorSticker.DirectionRad >= 90) && (monitorSticker.DirectionRad < 135))
                    Tem = "5";
                if ((monitorSticker.DirectionRad >= 135) && (monitorSticker.DirectionRad < 180))
                    Tem = "6";
                if ((monitorSticker.DirectionRad >= 180) && (monitorSticker.DirectionRad < 225))
                    Tem = "7";
                if ((monitorSticker.DirectionRad >= 225) && (monitorSticker.DirectionRad < 270))
                    Tem = "8";
                if ((monitorSticker.DirectionRad >= 270) && (monitorSticker.DirectionRad < 315))
                    Tem = "1";
                if ((monitorSticker.DirectionRad >= 315) && (monitorSticker.DirectionRad < 360))
                    Tem = "2";
                Cmd = "t" + Tem + "a" + String.valueOf(monitorSticker.DirectionRad) + "v" + String.valueOf(velocity);
            }
        return Cmd;

    }
    public void SendCmd()
    {

        if(ButtonConClickState) {
            String cmd=GetCmd();
            Message msg = new Message();
            msg.what = 0x00;
            msg.obj = cmd;
            if(cmd.length()>0)
             sT.revhandler.sendMessage(msg);
        }
    }


}
