package com.example.minxuan.socialprojectv2;

import android.app.Application;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * Created by RMO on 2017/6/16.
 */

public class SharedSocket extends Application{
    public ArrayList<String> gender;
    public ObjectInputStream bReader;
    public ObjectOutputStream bWriter;
    public ArrayList<HashMap<String, Object>> Item;//放通訊錄
    public ArrayList<HashMap<String, Object>> message;//放更新server的message(尚未處理)->處理後會清空
    public ArrayList<HashMap<String, Object>> messagebox;//所有[聯絡人+最新訊息]按照時間插入

    public ArrayList<HashMap<String, Object>> MQTTmessagebox;//所有[聯絡人+最新訊息]按照時間插入
    public String Account;//做為內存的filename
    ArrayList<String> userdata = null;
    public String LIST_msg,SMS_msg,id,clientHandle;
    public ArrayList<String> USER_msg;
    public String LocalAddress;
    public ArrayList<String> BroadcastMember;
    public ArrayList<InetSocketAddress> OnlineListener;
    public String mqttreceivecall="";
    public DatagramSocket datasock;
    public void onCreate(){
        // TODO Auto-generated method stub
        super.onCreate();
        //   friendlist = new ArrayList<Map<String,String>>();
        //    otherlist = new ArrayList<String>();
        gender = new ArrayList<String>();
        Item = new ArrayList<HashMap<String, Object>>();
        //  contactItemsDAO = new ContactItemsDAO(getApplicationContext());
        message = new ArrayList<HashMap<String, Object>>();
        messagebox = new ArrayList<HashMap<String, Object>>();
        MQTTmessagebox = new ArrayList<HashMap<String, Object>>();
        USER_msg = new ArrayList<String>();
        LocalAddress = getLocalIpAddress();
        BroadcastMember = new ArrayList<>();
        OnlineListener = new ArrayList<>();

    }
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

