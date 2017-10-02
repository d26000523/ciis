package com.example.minxuan.socialprojectv2.MQTT;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.minxuan.socialprojectv2.ListviewAdapter.messagesendadapter;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SharedSocket;
import com.example.minxuan.socialprojectv2.Video.StartSingleCall;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.util.ArrayList;
import java.util.HashMap;

public class MQTTvideoCall extends AppCompatActivity {

    int callposition = 0;
    int checknum = 0;
    ArrayList<HashMap<String, Object>> Item;
    Handler handler;
    SharedSocket sh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqttvideo_call);

        sh = (SharedSocket)getApplication();
        handler = new Handler();

        Item = new ArrayList<HashMap<String, Object>>();
        String user[] = sh.LIST_msg.split("\\{");

        for(int i=1;i<user.length;i++){
            String single[] = user[i].split("\n|:");
            HashMap<String, Object> map = new HashMap<String, Object>();

            map.put("ItemImage", R.drawable.boy);
            map.put("ItemName", single[2]);
            map.put("ItemPhone", single[6]);
            map.put("ItemClick", R.drawable.checkwhite);
            Item.add(map);
        }


        final ListView friendview = (ListView)findViewById(R.id.friendlistMQTTVideoCall);
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
                callposition = position;
                checknum++;
            }
        });
    }

    public void StartVideoMQTTVideoCall(View v){

        if(checknum==0){
            Toast.makeText(getApplicationContext(), "Choose Someone !", Toast.LENGTH_SHORT).show();
        }else{

            new Thread(new Runnable() {
                @Override
                public void run() {
                    publish(Item.get(callposition).get("ItemName").toString(), "VideoCall_"+sh.LocalAddress);
                }
            }).start();

            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("ip", "10.200.58."+Item.get(callposition).get("ItemName").toString());
            bundle.putString("service", "MQTT");
            intent.putExtras(bundle);
            intent.setClass(MQTTvideoCall.this, StartSingleCall.class);
            startActivity(intent);
            finish();
        }

    }

    void publish(String target, String msg){

        String topic = "MQTT_SMS_$("+target+")";

        SharedSocket sh = (SharedSocket)getApplication();
        String message = msg;
        int qos = 0;

        boolean retained = false;

        String[] args = new String[2];
        args[0] = message;
        args[1] = topic;

        try {
            Connections.getInstance(MQTTvideoCall.this).getConnection(sh.clientHandle).getClient()
                    .publish(topic, message.getBytes(), qos, retained, null, new ActionListener(MQTTvideoCall.this, ActionListener.Action.PUBLISH, sh.clientHandle, args));
        }catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + sh.clientHandle, e);
        }catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + sh.clientHandle, e);
        }

    }

}
