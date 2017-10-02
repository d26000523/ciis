package com.example.minxuan.socialprojectv2.Voice;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.example.minxuan.socialprojectv2.NetworkClientHandler;
import com.example.minxuan.socialprojectv2.R;
import com.google.gson.Gson;

import java.net.InetSocketAddress;


public class Startvoicecall extends AppCompatActivity {
    /**群組通話圖示**/
    public WaveHelper mWaveHelper;
    public WaveHelper mWaveHelper2;
    /**通話物件**/
    AlertDialog dialog;
    AlertDialog.Builder newmemalert;
    LayoutInflater layoutInflater;
    View add;
    final CallVoice callvoice = new CallVoice(this, new InetSocketAddress(NetworkClientHandler.StreamingTarget, 10003));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startvoicecall);
        final Bundle bundle = this.getIntent().getExtras();

        if(bundle.get("VOICE").toString().compareTo("REQUEST")==0){
            String targetPhone = bundle.getString("key");

            /** 整理要求通話訊息*/
            Gson gson = new Gson();
            Message message = new Message();
            message.setTAG("VOICE_SINGLE");
            message.setSender(AccountHandler.phoneNumber);
            message.setReceiver(targetPhone);
            message.setIP(AccountHandler.IP);
            message.setMessage("REQUEST");
            String gsonStr = gson.toJson(message);

            /** 送出訊息*/
            NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);
            OK();

        }else if(bundle.get("VOICE").toString().compareTo("ACCEPT")==0){

            String targetPhone = bundle.get("TARGET").toString();

            /** 整理接受視訊訊息*/
            Gson gson = new Gson();
            Message message = new Message();
            message.setTAG("VOICE_SINGLE");
            message.setSender(AccountHandler.phoneNumber);
            message.setReceiver(targetPhone);
            message.setIP(AccountHandler.IP);
            message.setMessage("ACCEPT");
            String gsonStr = gson.toJson(message);

            /** 送出訊息*/
            NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);
            check();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void check()
    {
        /**讓使用者決定要不要接**/
        new AlertDialog.Builder(Startvoicecall.this)//對話方塊
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("接收到語音通訊要求")
                .setMessage("要接通嗎??")
                .setCancelable(false)
                .setPositiveButton("拒絕", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent();
                        i.setClass(Startvoicecall.this, Menupage.class);
                        finish();
                        startActivity(i);
                    }
                })
                .setNegativeButton("接聽",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OK();
                    }
                })
                .show();
    }
    public void OK()
    {
        /**設定群組通話圖示**/
        layoutInflater= LayoutInflater.from(this);
        add = layoutInflater.inflate(R.layout.singlecallview, null);
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
        LinearLayout endcall = (LinearLayout)add.findViewById(R.id.Endcall);
        endcallOnClickListener c = new endcallOnClickListener();
        endcall.setOnClickListener(c);
        /**使用CALLVOICE**/
        try
        {
            callvoice.startPhone();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    /**結束群組通話**/
    class endcallOnClickListener implements View.OnClickListener {
        public void onClick(View v){
            new AlertDialog.Builder(Startvoicecall.this)//對話方塊
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("離開聊天")
                    .setMessage("確定要離開??")
                    .setCancelable(false)
                    .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNegativeButton("確定",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                callvoice.stopPhone();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            Intent i = new Intent();
                            i.setClass(Startvoicecall.this, Menupage.class);
                            finish();
                            startActivity(i);
                        }
                    })
                    .show();
        }
    }
}