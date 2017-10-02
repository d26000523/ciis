package com.example.minxuan.socialprojectv2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by RMO on 2017/7/3.
 */
/**語音廣播負責跟串流server做溝通**/
public class VoiceGroupUDP {

    private byte[] buffer = new byte[1024];
    static boolean isReceive = false;
    private Context context;
    public static DatagramSocket ds;
    private SocketAddress serverAddress;
    private Thread receiver;
    String phone;

    public VoiceGroupUDP(Context context){
        this.context = context;
        serverAddress = new InetSocketAddress(NetworkClientHandler.streamingServer, 19529);
    }

    public void start(final String type, final String phone) throws Exception{

        this.phone = phone;

        ds = new DatagramSocket();  /** 開啟UDP Socket*/
        isReceive = true;

        /** 負責註冊*/
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

    public void stop(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doSend(serverAddress, ("stop"+","+phone).getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
