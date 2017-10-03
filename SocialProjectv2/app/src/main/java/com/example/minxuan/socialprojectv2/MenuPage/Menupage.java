package com.example.minxuan.socialprojectv2.MenuPage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.minxuan.socialprojectv2.AccountHandler;
import com.example.minxuan.socialprojectv2.Contacts.Contacts;
import com.example.minxuan.socialprojectv2.Message;
import com.example.minxuan.socialprojectv2.NetworkClient;
import com.example.minxuan.socialprojectv2.NetworkClientHandler;
import com.example.minxuan.socialprojectv2.NewsLetter.MessageBox.MessageBox;
import com.example.minxuan.socialprojectv2.NewsLetter.SendMessage.SendMessage;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SingleVideoCall.VideoSingleCall;
import com.example.minxuan.socialprojectv2.UserInfo.UserInfo;
import com.example.minxuan.socialprojectv2.Video.VideoOnlineMembers;
import com.example.minxuan.socialprojectv2.Video.videolive_sender;
import com.example.minxuan.socialprojectv2.Voice.VoiceSingleCall;
import com.example.minxuan.socialprojectv2.Voice.groupchatlistcheck;
import com.example.minxuan.socialprojectv2.Voice.voicegroup;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Menupage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menupage);

        initview();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("=======================","onDestroy");


        /** 整理登出訊息*/
        Gson gson = new Gson();
        Message message = new Message();
        message.setTAG("LOGOUT");
        message.setMessage(NetworkClientHandler.getLocalIpAddress());
        String gsonStr = gson.toJson(message);

        if(NetworkClientHandler.networkClient!=null){
            NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("=======================","stop");

    }


    public void initview() {

        /** 設定Menupage顯示的Listview*/
        String[] name = {"Personal Info", "Contact", "Message Box", "Message", "Broadcast Message", "Voice Call", "Voice Broadcast", "Video Call","Live-Stream"};
        int[] pic = {R.drawable.settings, R.drawable.contact, R.drawable.inbox,R.drawable.message, R.drawable.messagegroup,R.drawable.phone,R.drawable.group0, R.drawable.singlevideocall, R.drawable.videocall};

        ListView listView = (ListView) findViewById(R.id.listView);
        ArrayList<HashMap<String, Object>> Item = new ArrayList<HashMap<String, Object>>();
        for(int i=0; i<name.length; i++){
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemImage", pic[i]);
            map.put("ItemName", name[i]);
            Item.add(map);
        }
        final menulistadapter adapter = new menulistadapter(
                this,
                Item,
                R.layout.menulistview,
                new String[] {"ItemImage","ItemName"},
                new int[] {R.id.ItemImage,R.id.ItemName}
        );
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position){
                    case 0: /** 個人資訊*/
                        UserInfo();
                        break;
                    case 1: /** 通訊錄*/
                        Contact();
                        break;
                    case 2: /** 個人收件夾*/
                        NewsLetter();
                        break;
                    case 3: /** 單人簡訊*/
                        singlemsg();
                        break;
                    case 4: /** 廣播簡訊*/
                        broadcastsend();
                        break;
                    case 5: /** 語音通話*/
                        Voicesinglecall();
                        break;
                    case 6: /** 語音廣播*/
                        broadcastvoice();
                        break;
                    case 7: /** 視訊通話*/
                        VideoSingleCall();
                        break;
                    case 8: /** 視訊直播*/
                        VideoLive();
                        break;
                }
            }
        });
    }

    /**使用者資訊*/
    public void UserInfo()
    {
        Intent intent = new Intent();
        intent.setClass(Menupage.this, UserInfo.class);
        startActivity(intent);
    }
    /** 個人收件夾*/
    public void NewsLetter(){
        Intent intent = new Intent();
        intent.setClass(Menupage.this, MessageBox.class);
        startActivity(intent);
    }
    /** 單人簡訊*/
    public void singlemsg(){
        Intent intent = new Intent();
        intent.setClass(Menupage.this, SendMessage.class);
        startActivity(intent);
    }
    /** 廣播簡訊*/
    AlertDialog dialog;
    AlertDialog.Builder newmemalert;
    LayoutInflater layoutInflater;
    View add;
    public void broadcastsend(){
        layoutInflater= LayoutInflater.from(Menupage.this);
        add = layoutInflater.inflate(R.layout.broadcastingcheckbox, null);
        newmemalert = new AlertDialog.Builder(Menupage.this);
        newmemalert.setView(add);
        dialog = newmemalert.show();

        ImageView finish = (ImageView) (add.findViewById(R.id.sendbroadcst));
        sendbroadcst sendbroadcst = new sendbroadcst();
        finish.setOnClickListener(sendbroadcst);
    }
    private class sendbroadcst implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            EditText ED_message = (EditText)add.findViewById(R.id.msg);
            EditText ED_password = (EditText)add.findViewById(R.id.checkpasswordtext);

            String message = ED_message.getText().toString();
            String password = ED_password.getText().toString();

            if("".compareTo(password)==0){
                Toast.makeText(getApplicationContext(), "Please Fill Password Field!", Toast.LENGTH_SHORT).show();
            }else if("".compareTo(message)==0){
                Toast.makeText(getApplicationContext(), "Please Fill Text Field!", Toast.LENGTH_SHORT).show();
            }else{
                if (message.compareTo("") != 0)
                {
                    if(password.compareTo(AccountHandler.adminPassword) == 0)
                    {
                        /** 整理訊息*/
                        Gson gson = new Gson();
                        Message mess = new Message();
                        mess.setTAG("BROADCAST_MESSAGE");
                        mess.setSender(AccountHandler.phoneNumber);
                        mess.setMessage(message);
                        String gsonStr = gson.toJson(mess);

                        /** 送出登入訊息*/
                        NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);
                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();

                        /** 儲存訊息*/
                        try {
                            FileWriter fileWriter = new FileWriter("/storage/emulated/0/Download/message.txt", true);
                            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter); /** 將BufferedWeiter與FileWrite物件做連結*/
                            bufferedWriter.write(gsonStr);
                            bufferedWriter.newLine();
                            bufferedWriter.close();
                            fileWriter.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        /** 視窗消失*/
                        dialog.dismiss();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Wrong Password !", Toast.LENGTH_SHORT).show();
                        /**清空欄位**/
                        ED_message.setText("");
                        ED_password.setText("");
                    }
                }
            }
        }
    }
    /** 通訊錄*/
    public void Contact(){
        Intent intent = new Intent();
        intent.setClass(Menupage.this, Contacts.class);
        startActivity(intent);
    }

    /** 視訊直播*/
    public void VideoLive(){
        layoutInflater= LayoutInflater.from(Menupage.this);
        add = layoutInflater.inflate(R.layout.videolivecheckbox, null);
        newmemalert = new AlertDialog.Builder(Menupage.this);//對話方塊
        newmemalert.setView(add);
        dialog = newmemalert.show();
        RelativeLayout live = (RelativeLayout)add.findViewById(R.id.videolive);
        RelativeLayout watch = (RelativeLayout)add.findViewById(R.id.videojoin);
        liveOnClickListener f = new liveOnClickListener();
        live.setOnClickListener(f);
        watchOnClickListener f2 = new watchOnClickListener();
        watch.setOnClickListener(f2);
    }
    class liveOnClickListener implements View.OnClickListener {
        public void onClick(View v){
            Intent i = new Intent();
            i.setClass(Menupage.this, videolive_sender.class);
            startActivity(i);
            dialog.dismiss();
        }
    }
    class watchOnClickListener implements View.OnClickListener {
        public void onClick(View v){
            Intent i = new Intent();
            i.setClass(Menupage.this, VideoOnlineMembers.class);
            startActivity(i);
            dialog.dismiss();
        }
    }
    /**按下返回鍵判斷**/
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //確定按下退出鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            new AlertDialog.Builder(Menupage.this)//對話方塊
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("Exit Social Project")
                    .setMessage("Are You Sure?")
                    .setCancelable(false)
                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNegativeButton("Yes",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Menupage.this.finish();
                        }
                    })
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /** 視訊通話*/
    public void VideoSingleCall(){
        Intent intent = new Intent();
        intent.setClass(Menupage.this, VideoSingleCall.class);
        startActivity(intent);
    }

    public void  Voicesinglecall(){
        Intent intent = new Intent();
        intent.setClass(Menupage.this, VoiceSingleCall.class);
        startActivity(intent);
    }
    /**群組通話**/
    public void  broadcastvoice(){

        layoutInflater= LayoutInflater.from(Menupage.this);
        add = layoutInflater.inflate(R.layout.voicegroupcheckbox, null);
        newmemalert = new AlertDialog.Builder(Menupage.this);//對話方塊
        newmemalert.setView(add);
        dialog = newmemalert.show();
        RelativeLayout newchat= (RelativeLayout)add.findViewById(R.id.voicelive);
        RelativeLayout joinchat = (RelativeLayout)add.findViewById(R.id.voicejoin);
        newchatListener f3 = new newchatListener();
        newchat.setOnClickListener(f3);
        joinchatListener f4 = new joinchatListener();
        joinchat.setOnClickListener(f4);
    }
    /**開啟新的聊天室**/
    class newchatListener implements View.OnClickListener {
        public void onClick(View v){
            Intent intent = new Intent();
            intent.setClass(Menupage.this, voicegroup.class);
            startActivity(intent);
            dialog.dismiss();
        }
    }
    /**加入聊天室**/
    class joinchatListener implements View.OnClickListener {
        public void onClick(View v){
            Intent i = new Intent();
            i.setClass(Menupage.this, groupchatlistcheck.class);
            startActivity(i);
            dialog.dismiss();
        }
    }
}
