package com.example.minxuan.socialprojectv2.MQTT.service;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minxuan.socialprojectv2.ListviewAdapter.messagesendadapter;
import com.example.minxuan.socialprojectv2.MQTT.ActionListener;
import com.example.minxuan.socialprojectv2.MQTT.Connections;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SharedSocket;
import com.example.minxuan.socialprojectv2.Voice.CallVoice;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class MQTTSingleCall extends ActionBarActivity {
    int select;
    int checknum=0;
    ArrayList<HashMap<String, Object>> Item;
    Handler handler;
    SharedSocket sh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqttsingle_call);

        sh = (SharedSocket)getApplication();
        handler = new Handler();

        Item = new ArrayList<HashMap<String, Object>>();;
        String user[] = sh.LIST_msg.split("\\{");
        for(int i=1;i<user.length;i++){
            String single[] = user[i].split("\n|:");
            HashMap<String, Object> map = new HashMap<String, Object>();

            String ItemName = (single[6].split("\\."))[2]+"_"+(single[6].split("\\."))[3];

            map.put("ItemImage", R.drawable.boy);
            map.put("ItemName", ItemName);
            map.put("ItemPhone", single[6]);
            map.put("ItemClick", R.drawable.checkwhite);
            Item.add(map);
        }

        final ListView friendview = (ListView)findViewById(R.id.friendlist);
        final messagesendadapter Btnadapter = new messagesendadapter(
                this,
                Item,
                R.layout.messagesendlsitview,
                new String[] {"ItemImage","ItemName","ItemPhone","ItemClick"},
                new int[] {R.id.ItemImage,R.id.ItemName,R.id.ItemPhone,R.id.check}
        );
        friendview.setAdapter(Btnadapter);
        friendview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Btnadapter.changefocus(position, R.drawable.checkwhite, R.drawable.checkblue);
                friendview.setAdapter(Btnadapter);
                select = position;
                checknum++;
            }
        });

    }

    AlertDialog dialog;
    AlertDialog.Builder newmemalert;
    LayoutInflater layoutInflater;
    View add;

    public void startphonecall(View v){

        if(checknum==0){
            Toast.makeText(getApplicationContext(), "Choose One!", Toast.LENGTH_SHORT).show();
        }else{

            //發出通知
            new Thread(new Runnable() {
                @Override
                public void run() {
                    publish(Item.get(select).get("ItemName").toString(), "VoiceCall_"+sh.LocalAddress);
                }
            }).start();

            //產生通話框
            layoutInflater= LayoutInflater.from(this);
            add = layoutInflater.inflate(R.layout.videocalls, null);
            newmemalert = new AlertDialog.Builder(this);//對話方塊
            newmemalert.setCancelable(false);
            newmemalert.setView(add);
            //  dialog.setCancelable(false);
            dialog = newmemalert.show();
            ImageView im = (ImageView)add.findViewById(R.id.groupcalling);
            TextView gr = (TextView)add.findViewById(R.id.group);
            im.setImageResource(R.drawable.group0);
            gr.setText("From User : "+Item.get(select).get("ItemName").toString());
            gr.setTextColor(Color.parseColor("#FE9C02"));
            //開始傳音訊
            final CallVoice callvoice = new CallVoice(this, new InetSocketAddress("10.58."+Item.get(select).get("ItemName").toString().replace("_","."), 10003));

            try {
                callvoice.startPhone();
            } catch (Exception e) {
                e.printStackTrace();
            }
            LinearLayout endcall = (LinearLayout)add.findViewById(R.id.Endcall);
            endcall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    try {
                        callvoice.stopPhone();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    void publish(String target, String msg){
        String topic = "MQTT_SMS_SEND_$("+target+")";
        SharedSocket sh = (SharedSocket)getApplication();
        String message = msg;
        int qos = 0;
        boolean retained = false;
        String[] args = new String[2];
        args[0] = message;
        args[1] = topic;
        try {
            Connections.getInstance(MQTTSingleCall.this).getConnection(sh.clientHandle).getClient()
                    .publish(topic, message.getBytes(), qos, retained, null, new ActionListener(MQTTSingleCall.this, ActionListener.Action.PUBLISH, sh.clientHandle, args));
        }catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messaged from the client with the handle " + sh.clientHandle, e);
        }catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messaged from the client with the handle " + sh.clientHandle, e);
        }
    }
}

