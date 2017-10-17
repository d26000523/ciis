package com.example.minxuan.socialprojectv2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.minxuan.socialprojectv2.MenuPage.Menupage;
import com.example.minxuan.socialprojectv2.Video.StartSingleCall;
import com.example.minxuan.socialprojectv2.Voice.Startvoicecall;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by MinXuan on 2017/5/24.
 */

public class NetworkClient {

    public WebSocketClient webSocketClient;

    /**Websocket 協定*/
    private Draft draft;

    String serverAddr;
    String account;
    String password;
    String WsAddress;

    Activity activity;
    Gson gson = new Gson();

    static Boolean isConnected = false;

    public NetworkClient(String serverAddr, String account, String password){
        this.serverAddr = serverAddr;
        this.account = account;
        this.password = password;
        setWsAddress();
        draft = new Draft_17();


        try {
            this.webSocketClient = new WebSocketClient(new URI(this.WsAddress), this.draft) {

                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    NetworkClientHandler.scenario2 = false;
                }

                @Override
                public void onMessage(ByteBuffer byteBuffer){
                    Arrays.fill(NetworkClientHandler.streamingBuffer, (byte) 0);
                    byteBuffer.get(NetworkClientHandler.streamingBuffer, 0, byteBuffer.remaining());
                }

                @Override
                public void onMessage(String message) {

                    /** Gson 訊息解碼*/
                    Type listType = new TypeToken<ArrayList<Message>>() {}.getType();
                    ArrayList<Message> jsonArr = gson.fromJson("[" + message + "]", listType);
                    Message mess = jsonArr.get(0);

                    if(message.compareTo("Connected")==0){
                        isConnected = true;
                    }

                    /** 登入訊息*/
                    switch(mess.getTAG()){
                        case "LOGIN_SUCCESS":

                            AccountHandler.account = mess.getAccount();
                            AccountHandler.password = mess.getPassword();
                            AccountHandler.phoneNumber = mess.getPhonenumber();
                            AccountHandler.firstName = mess.getFirstName();
                            AccountHandler.lastName = mess.getLastName();
                            AccountHandler.email = mess.getEmail();
                            AccountHandler.IP = NetworkClientHandler.getLocalIpAddress();
                            AccountHandler.print();

                            Intent i = new Intent();
                            i.setClass(activity, Menupage.class);
                            activity.startActivity(i);
                            activity.finish();
                            break;
                        case "ACCOUNT_NOT_FOUND":
                            webSocketClient.close();
                            break;
                        case "RE_LOGIN_ERROR":
                            webSocketClient.close();
                            break;
                        case "PASSWORD_FAILED":
                            webSocketClient.close();
                            break;
                    }

                    /** 註冊訊息*/
                    switch (mess.getTAG()){
                        case  "CREATE_NEW_SUCCESS":
                            activity.finish();
                            webSocketClient.close();
                            break;
                    }

                    /** 收件夾*/
                    switch(mess.getTAG()){
                        case "MESSAGE":
                            /** 將訊息IO到sdcard*/
                            writeMessage(message);
                        case "SEND_SUCCESS":
                            break;
                        case "BROADCAST_MESSAGE":
                            writeMessage(message);
                            break;
                    }

                    /** 直播視訊*/
                    switch (mess.getTAG()){
                        case "VIDEO_LIVE":
                            if(mess.getMessage().compareTo("SUCCESS")==0)
                            {
                                NetworkClientHandler.isStreaming = true;
                            }
                            else if(mess.getMessage().compareTo("FAILED")==0)
                            {
                                Toast.makeText(activity, "Error !", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case "VIDEO_LIST":
                            String[] temp = mess.getMessage().split(",");
                            for(int i=0;i<NetworkClientHandler.watch_list.size();i++){
                                NetworkClientHandler.watch_list.remove(0);
                            }
                            for(String str : temp){
                                if(str.compareTo("")!=0)
                                    NetworkClientHandler.watch_list.add(str);
                            }
                            break;
                        case "LISTEN_VIDEO_LIVE":
                            if(mess.getMessage().compareTo("SUCCESS")==0)
                            {
                                NetworkClientHandler.isStreaming = true;
                            }
                            else if(mess.getMessage().compareTo("FAILED")==0)
                            {
                                Toast.makeText(activity, "Eroor !", Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }

                    switch(mess.getTAG()){
                        case "VIDEO_SINGLE":
                            if(mess.getMessage().compareTo("REQUEST")==0){  /** 收到請求*/
                                NetworkClientHandler.StreamingTarget = mess.getIp();
                                NetworkClientHandler.isStreaming = true;

                                Bundle bundle = new Bundle();
                                bundle.putString("MODE", "ACCEPT");
                                bundle.putString("TARGET", mess.getSender());
                                Intent intent = new Intent();
                                intent.putExtras(bundle);
                                intent.setClass(activity, StartSingleCall.class);
                                activity.startActivity(intent);

                            }else if(mess.getMessage().compareTo("ACCEPT")==0){ /** 收到接受*/
                                NetworkClientHandler.StreamingTarget = mess.getIp();
                                NetworkClientHandler.isStreaming = true;
                            }
                            break;
                    }

                    switch(mess.getTAG()){
                        case "VOICE_SINGLE":
                            if(mess.getMessage().compareTo("REQUEST")==0){  /** 收到請求*/
                                NetworkClientHandler.StreamingTarget = mess.getIp();
                                NetworkClientHandler.isStreaming = true;

                                Bundle bundle = new Bundle();
                                bundle.putString("VOICE", "ACCEPT");
                                bundle.putString("TARGET", mess.getSender());
                                Intent intent = new Intent();
                                intent.putExtras(bundle);
                                intent.setClass(activity, Startvoicecall.class);
                                activity.startActivity(intent);

                            }else if(mess.getMessage().compareTo("ACCEPT")==0){ /** 收到接受*/
                                NetworkClientHandler.StreamingTarget = mess.getIp();
                                NetworkClientHandler.isStreaming = true;
                            }
                            else if(mess.getMessage().compareTo("CANCEL")==0){  /** 收到取消通話*/
                                Startvoicecall startvoicecallactivity = (Startvoicecall)activity;
                                startvoicecallactivity.voiceCancel();
                            }
                            break;
                    }
                    /**廣播聊天室*/
                    switch (mess.getTAG()){
                        case "VOICE_LIVE":
                            if(mess.getMessage().compareTo("SUCCESS")==0)
                            {

                                NetworkClientHandler.isStreaming = true;
                            }
                            else if(mess.getMessage().compareTo("FAILED")==0)
                            {
                                Toast.makeText(activity, "Error !", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case "VOICE_LIST":
                            String[] temp = mess.getMessage().split(",");
                            for(int i=0;i<NetworkClientHandler.watch_list.size();i++){
                                NetworkClientHandler.watch_list.remove(0);
                            }
                            for(String str : temp){
                                if(str.compareTo("")!=0)
                                    NetworkClientHandler.watch_list.add(str);
                            }
                            break;
                        case "LISTEN_VOICE_LIVE":
                            if(mess.getMessage().compareTo("SUCCESS")==0)
                            {
                                NetworkClientHandler.isStreaming = true;
                            }
                            else if(mess.getMessage().compareTo("FAILED")==0)
                            {
                                Toast.makeText(activity, "Error !", Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected = false;
                }

                @Override
                public void onError(Exception e) {
                    isConnected = false;
                    e.printStackTrace();
                }
            };
            try {
                this.webSocketClient.connect();
            }catch(Exception e){
                Log.e("TAG", "Connection failed");
            }

        } catch (URISyntaxException e) {

        }
    }

    public void setWsAddress(){
        /**要寫技術文件喔*/

        this.WsAddress = "ws://" + this.serverAddr + ":8080/WebSocketServerExample/websocketendpoint/" + this.account + "/" + this.password;
    }

    public void setActivity(Activity activity){
        this.activity = activity;
    }

    /** 寫入檔案*/
    public void writeMessage(String str){

        try {
            FileWriter fileWriter = new FileWriter("/storage/emulated/0/Download/message.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter); /** 將BufferedWeiter與FileWrite物件做連結*/
            bufferedWriter.write(str);
            bufferedWriter.newLine();
            bufferedWriter.close();
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
