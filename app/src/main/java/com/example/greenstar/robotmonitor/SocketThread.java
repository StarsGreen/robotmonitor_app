package com.example.greenstar.robotmonitor;


import android.os.Looper;
import android.os.Message;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.os.Handler;


/**
 * Created by GreenStar on 2018/1/4.
 */

public class SocketThread implements Runnable {
    private android.os.Handler handler;
    private Socket socket;
    public  android.os.Handler revhandler;
    private String IP;
    private int PORT;
    private BufferedReader br=null;
    private OutputStream os=null;

    public boolean ConStatus=false;

    public void GetIPandAddr(String ip,int port)
    {
        this.IP=ip;
        this.PORT=port;
    }
    public void GetHandler(android.os.Handler handler)
    {
        this.handler=handler;
    }

    public void SocketConnect() {
        try {
            if (!ConStatus) {
                socket = new Socket(this.IP, this.PORT);
            //    socket.setSoTimeout(5000);
                if(socket.isConnected())
                ConStatus=true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void SocketClose() {
        try {
            if (socket.isConnected()) {
                socket.close();
                br.close();
                os.close();
                ConStatus=false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
       @Override
  public void run()
    {
            try {
                            this.SocketConnect();
                            if(ConStatus) {
                                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                os = socket.getOutputStream();
                            }
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        String socketmsg;
                        char [] buff=new char[100];
                        int rcvLen;
                        Message msg=new Message();
                        msg.what=0x01;
                        try
                        {
                             while(ConStatus)
                             {
                                 rcvLen = br.read(buff);
                                 socketmsg = new String(buff,0,rcvLen);
                                 if(socketmsg!=null) {
                                     msg.obj=socketmsg;
                                     handler.sendMessage(msg);
                                 }
                             }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }.start();
                Looper.prepare();
                revhandler=new android.os.Handler()
                {
                    @Override
                    public void handleMessage(Message msg) {
                            if (msg.what == 0x00) {
                                try {
                                    if (socket.isConnected())
                                    os.write((msg.obj.toString()).getBytes("utf-8"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    }
                };
                Looper.loop();
             }
                 catch (SocketTimeoutException se) {
                    System.out.println("network timeout");
             }
                catch (IOException e) {
                    e.printStackTrace();
             }
    }

}
