package com.example.minxuan.socialprojectv2.Voice;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.minxuan.socialprojectv2.AccountHandler;
import com.example.minxuan.socialprojectv2.MQTT.WaveHelper;
import com.example.minxuan.socialprojectv2.MQTT.WaveView;
import com.example.minxuan.socialprojectv2.MenuPage.Menupage;
import com.example.minxuan.socialprojectv2.Message;
import com.example.minxuan.socialprojectv2.NetworkClient;
import com.example.minxuan.socialprojectv2.NetworkClientHandler;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.VoiceGroupUDP;
import com.google.gson.Gson;

public class voicegroup extends AppCompatActivity {

    /**群組通話圖示**/
    public WaveHelper mWaveHelper;
    public WaveHelper mWaveHelper2;
    /**通話物件**/
    final groupcallvoice groupvoice = new groupcallvoice(this);
    AlertDialog dialog;
    AlertDialog.Builder newmemalert;
    LayoutInflater layoutInflater;
    View add;
    String type;
    /**廣播物件**/
    private VoiceGroupUDP socket;
    /** 是否等待server回傳訊息*/
    private boolean isWaiting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voicegroup);

        socket = new VoiceGroupUDP(this);
        NetworkClientHandler.isStreaming = false;
        isWaiting = true;
        type = "open";
        register();

        NetworkClientHandler.networkClient.setActivity(this);
        while(isWaiting){
            /**當建立成功 isStreaming為true，開始向串流server傳封包**/
            if(NetworkClientHandler.isStreaming) {
                init();
                isWaiting = false;
                break;
            }
        }
    }
    /** 向WsServer註冊*/
    public void register(){
        /** 整理訊息*/
        Gson gson = new Gson();
        Message message = new Message();
        message.setTAG("VOICE_LIVE");
        message.setVoiceLive("open");
        String gsonStr = gson.toJson(message);
        NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);
    }
    public void init(){
        /**設定群組通話圖示**/
        layoutInflater= LayoutInflater.from(this);
        add = layoutInflater.inflate(R.layout.videocalls, null);
        newmemalert = new AlertDialog.Builder(this);//對話方塊
        newmemalert.setCancelable(false);
        newmemalert.setView(add);
        dialog = newmemalert.show();
        ImageView im = (ImageView)add.findViewById(R.id.groupcalling);
        TextView gr = (TextView)add.findViewById(R.id.group);
        im.setImageResource(R.drawable.group0);
        gr.setTextColor(Color.parseColor("#FE9C02"));
        /**設定通話waveview**/
        WaveView waveView = (WaveView) add.findViewById(R.id.sendwave);
        waveView.setBorder(10, Color.parseColor("#E9E9E9"),"#00B6DE");
        mWaveHelper = new WaveHelper(waveView);
        WaveView waveView2 = (WaveView) add.findViewById(R.id.revwave);
        waveView.setBorder(10, Color.parseColor("#E9E9E9"),"#FDB3C2");
        mWaveHelper2 = new WaveHelper(waveView2);
        waveView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWaveHelper.start();
                mWaveHelper2.start();
            }
        }, 1000);

        /** 初始化socket並送出註冊UDP封包*/
        try{
            socket.start(type, AccountHandler.phoneNumber);
        }catch (Exception e) {
            e.printStackTrace();
        }

        /**使用CALLVOICE**/
        try{
            groupvoice.startPhone();
        }catch (Exception e){
            e.printStackTrace();
        }

        LinearLayout endcall = (LinearLayout)add.findViewById(R.id.Endcall);
        endcallOnClickListener c = new endcallOnClickListener();
        endcall.setOnClickListener(c);
    }
    /**結束群組通話**/
    class endcallOnClickListener implements View.OnClickListener {
        public void onClick(View v){
            new AlertDialog.Builder(voicegroup.this)//對話方塊
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("離開群組聊天頻道")
                    .setMessage("確定要離開??")
                    .setCancelable(false)
                    .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNegativeButton("確定",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            try{
                                groupvoice.stopPhone();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            mWaveHelper.cancel();
                            mWaveHelper2.cancel();

                            /** 變更參數*/
                            NetworkClientHandler.isStreaming = false;
                            /**傳送聊天室關閉的請求**/
                            Gson gson = new Gson();
                            Message message = new Message();
                            message.setTAG("VOICE_LIVE");
                            message.setVoiceLive("close");
                            String gsonStr = gson.toJson(message);
                            NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);

                            /** 跳轉回去*/
                            Intent i = new Intent();
                            i.setClass(voicegroup.this, Menupage.class);
                            startActivity(i);

                            dialog2.dismiss();
                            dialog.dismiss();
                            voicegroup.this.finish();

                        }
                    })
                    .show();
        }
    }
}
