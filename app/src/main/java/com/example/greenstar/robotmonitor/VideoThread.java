package com.example.greenstar.robotmonitor;

import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by GreenStar on 2018/1/8.
 */

public class VideoThread implements Runnable {

    public static byte[] VideoMessage = new byte[1024*1024]; //接收消息
    public static byte[] msgRcv = new byte[1024*1024]; //接收消息
    private static byte[] TmpMsg=new byte[1024*1024];

    public static int index=0;
    public static int VideoIndex=0;
    final static int udpPort = 51000;


    InetAddress address ;

    private String groupHost="224.0.0.100" ;
    MulticastSocket multicastSocket=null;
    InetAddress inetAddress=null;
    private static DatagramSocket socket = null;
     private static DatagramPacket packetRcv = new DatagramPacket(msgRcv,msgRcv.length);

    public  boolean UdpConState = false; //udp生命线程
    public static boolean HandleDataState = false;

    private static boolean RecStatus=false;
    private static boolean Head=false;


    public android.os.Handler handler,revhandler;




    public VideoThread(){
        super();
    }

    public void GetHandler(android.os.Handler handler)
    {
        this.handler=handler;
    }

    public InetAddress getLocalHostLANAddress() throws Exception {
        try {
            InetAddress candidateAddress = null;
            // 遍历所有的网络接口
            for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
                // 在所有的接口下再遍历IP
                for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了
                            return inetAddr;
                        } else if (candidateAddress == null) {
                            // site-local类型的地址未被发现，先记录候选地址
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            if (candidateAddress != null) {
                return candidateAddress;
            }
            // 如果没有发现 non-loopback地址.只能用最次选的方案
            InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
            return jdkSuppliedAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    ////////////////////////////////////////////////
    public void StartUdpSocket()
    {
        try {
            //socket = new DatagramSocket(udpPort);
         multicastSocket = new MulticastSocket(udpPort); //MulticastSocket实例
        inetAddress = InetAddress.getByName(groupHost); //组地址
        multicastSocket.joinGroup(inetAddress); //加入到组播组中
        if(!UdpConState)
           address = getLocalHostLANAddress();
            Log.i("udpClient","Udp is Established");
            Log.i("IPaddress:",address.getHostAddress());
            UdpConState=true;
        } catch (SocketException e) {
            Log.i("udpClient","建立接收数据报失败");
            e.printStackTrace();
        }
        catch (UnknownHostException ue)
        {
            Log.i("udpClient","unkown host");
            ue.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
/////////////////////////////////////////////////////
    public void StopUdpSocket()
    {
            if(UdpConState) {
                //socket.close();
                 multicastSocket.close();
                Log.i("udpClient", "Udp正常关闭");
                HandleDataState=false;
                UdpConState = false;
            }

    }
///////////////////////////////////////////////////
    public void HandleMsgFromUI() {
         Looper.prepare();
         revhandler = new android.os.Handler() {
         @Override
         public void handleMessage(Message msg) {
               if (msg.what == 0x00) {

                        }
                    }
                };
         Looper.loop();
    }

/////////////////////////////////////////
    public void SendMsgToUI(byte[] bytes)
    {
        Message msg0 = new Message();
        msg0.what = 0x02;
        if(bytes!=null) {
            msg0.obj = bytes;
            handler.sendMessage(msg0);
        }
    }
    //////////////////////////////////////////
public void HanderPackage(DatagramPacket packet)
{
        String StartFlag="";
        if(packet.getAddress().getHostAddress().equals("192.168.1.13")) {
            switch (packet.getLength()) {
                case 5: {
                    for (int i = 0; i < 5; i++)
                        StartFlag += (char) msgRcv[i];
                    if (StartFlag.equals("start")) {
                        RecStatus = true;
                        Head = true;
                        index = 0;
                    }
                }
                break;
                case 3: {
                    for (int i = 0; i < 3; i++)
                        StartFlag += (char) msgRcv[i];
                    if (StartFlag.equals("end")) {
                        RecStatus = false;
                        if(index!=0)
                        SendMsgToUI(VideoMessage);
                    }
                }
                break;
                default: {
                    if (RecStatus) {
                        if (!Head) {
                            //System.arraycopy(msgRcv, 0, VideoMessage, index * 1024, packetRcv.getLength());
                                                       bytesToImageFile(packetRcv.getData(),packetRcv.getLength());
                            index++;
                            Log.i("package num:", String.valueOf(index));
                        }
                        Head = false;
                    }
                }
                break;
            }
        }

}
    /////////////////////////////////////////////////
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
    ///////////////////////////////////////////////////
    public void SetOppositeValue(boolean b)
    {
        if(b)b=false;
        else b=true;
    }
    ///////////////////////////////////////////////////
    public void ChangeVectorToByteArray(Vector v,byte[] bytes,int vecindex)
    {
        byte[][] tembyte=new byte[10240][1024];
        v.copyInto(tembyte);
        for(int j=0;j<150;j++) {
         //   int length=getLength(tembyte[j]);
                System.arraycopy(tembyte[j+vecindex*150], 0, bytes, j * 1024,1024);
            }
    }
    /////////////////////////////////////////////////
    public void ClearArray(byte[] b)
    {
        for(int j=0;j<b.length;j++) {
            b[j]=0;
        }
    }
    ////////////////////////////////////////////////
    /////////////////////////////////////////////////
    private void bytesToImageFile(byte[] bytes,int length) {
        try {
            String path= Environment.getExternalStorageDirectory().getAbsolutePath() ;
            File file = new File(path + "/video.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            Log.i("video.jpg path:",path);
          //  int length=getLength(bytes);
            fos.write(bytes, 0, length);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /////////////////////////////////////////////
    private byte[] bytesToImageStream(byte[] bytes,int length) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(bytes,0,length);
        byte[] vdata=baos.toByteArray();
        return baos.toByteArray();
    }
//////////////////////////////////////////////////////
    @Override
    public void run() {
        this.StartUdpSocket();
        ClearArray(VideoMessage);
        ClearArray(TmpMsg);

        new Thread(){
            @Override
            public void run()
            {
                HandleMsgFromUI();
            }
        }.start();
        while (UdpConState){
            try {
               // Log.i("udpClient", "UDP开始监听");

                multicastSocket.receive(packetRcv);
               // bytesToImageFile(packetRcv.getData(),packetRcv.getLength());
                SendMsgToUI( bytesToImageStream(packetRcv.getData(),packetRcv.getLength()));
               Log.i("time is :",String.valueOf(System.currentTimeMillis())) ;
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        Log.i("udpClient","UDP监听关闭");
    }
}