package com.example.minxuan.socialprojectv2;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by MinXuan on 2017/5/25.
 */

public class NetworkClientHandler {

    public static NetworkClient networkClient = null;
    public static Boolean isStreaming = false;
    public static ArrayList<String> watch_list = new ArrayList<>();
    public static byte[] streamingBuffer = new byte[1300];
    public static String serverAddress;
    public static String streamingServer = "140.117.168.128";

    public static int LIVE_PORT;
    public static String StreamingTarget;
    public static boolean scenario2 = true;/*false是S1,true是S2*/


    static void setNetworkClient(String serverAddr, String account, String password){
        networkClient = new NetworkClient(serverAddr, account, password);
        serverAddress = serverAddr;
    }

    /** 獲得本機IP*/
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
