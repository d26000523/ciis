package com.example.minxuan.socialprojectv2;

import android.content.Context;
import android.content.Intent;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by RMO on 2017/7/5.
 */
/**視訊直播，聲音封包**/
public class UdpClientvoice {

    public static boolean isReceive = false;
    private Context context;
    public static DatagramSocket ds;
    private SocketAddress serverAddress;
    private Thread receiver;

    public UdpClientvoice(Context context){
        this.context = context;
        serverAddress = new InetSocketAddress(NetworkClientHandler.streamingServer, 19528);
    }

    public void start(final String type, final String phone) throws Exception{

        ds = new DatagramSocket();  /** 開啟UDP Socket*/
        isReceive = true;

        /** 負責註冊與取得對方資訊*/
        receiver = new Thread() {
            public void run() {
                try {
                    doSend(serverAddress, (type+","+phone).getBytes());   /** 送出註冊訊息*/
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
                    ds.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
