package com.example.minxuan.socialprojectv2;

import android.content.Context;
import android.content.Intent;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by MinXuan on 2017/6/13.
 */
/**視訊直播跟串流server進行溝通(影像封包)**/
public class UdpClient {

    public static boolean isReceive = false;
    private Context context;
    public static DatagramSocket ds;
    private SocketAddress serverAddress;
    private Thread receiver;

    public UdpClient(Context context){
        this.context = context;
        serverAddress = new InetSocketAddress(NetworkClientHandler.streamingServer, 19527);
    }

    public void start(final String type, final String phone) throws Exception{

        ds = new DatagramSocket();  /** 開啟UDP Socket*/
        isReceive = true;

        /** 負責註冊與取得對方資訊*/
        receiver = new Thread() {
            public void run() {
                try {
                    doSend(serverAddress, (type+","+phone).getBytes());   /** 送出註冊訊息*/



                    broadcast("[System]: Register success.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        receiver.start();
    }

    public SocketAddress getServerAddress(){
        return serverAddress;
    }

    public void doSend(SocketAddress addr, byte[] data) throws Exception {
        DatagramPacket pack = new DatagramPacket(data, data.length, addr);
        System.out.println(addr);
        System.out.println(data);
        System.out.println(data.length);
        ds.send(pack);
    }
    public void broadcast(String message) {
        Intent intent = new Intent("DISPLAY_MESSAGE_ACTION");
        intent.putExtra("Message", message);
        context.sendBroadcast(intent);
    }
    public void stop(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doSend(serverAddress, ("stop"+",").getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
