package com.example.minxuan.socialprojectv2.MQTT;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.minxuan.socialprojectv2.ListviewAdapter.messagesendadapter;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SharedSocket;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.util.ArrayList;
import java.util.HashMap;

public class MQTTSingleMessage extends AppCompatActivity {
    int select;
    int checkcount = 0;
    ArrayList<HashMap<String, Object>> Item;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqttsingle_message);
        list();
    }

    String clientHandle;
    public void sendmessage(View v){
        final EditText e = (EditText)findViewById(R.id.textarea);
        if(checkcount==0)
        {
            Toast.makeText(getApplicationContext(), "Choose Someone!", Toast.LENGTH_SHORT).show();
        }
        else if ("".equals(e.getText().toString().trim())) {
            Toast.makeText(getApplicationContext(), "Please fill text field!", Toast.LENGTH_SHORT).show();
        }
        else {
            Thread thread2 = new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    publish(Item.get(select).get("ItemName").toString(), e.getText().toString());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            thread2.start();
        }
    }
    public void refresh(View v){
        list();

    }
    public void list(){
        SharedSocket sh = (SharedSocket)getApplication();

        Item = new ArrayList<HashMap<String, Object>>();;
        String user[] = sh.LIST_msg.split("\\{");
        for(int i=1;i<user.length;i++){
            String single[] = user[i].split("\n|:");
            if(!single[6].equals(MainActivity.getLocalIpAddress())) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                String ItemName = (single[6].split("\\."))[2] + "_" + (single[6].split("\\."))[3];
                map.put("ItemImage", R.drawable.boy);
                map.put("ItemName", ItemName);
                map.put("ItemPhone", single[6]);
                map.put("ItemClick", R.drawable.checkwhite);
                Item.add(map);
            }
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
                checkcount++;
            }
        });
    }
    void publish(String id,String msg)
    {
        String topic = "MQTT_SMS_SEND_$("+id+")";
        SharedSocket sh = (SharedSocket)getApplication();
        String message = sh.id + ":"+msg;
        int qos = 0;
        boolean retained = false;

        String[] args = new String[2];
        args[0] = message;
        args[1] = topic;

        try {
            Connections.getInstance(this).getConnection(sh.clientHandle).getClient()
                    .publish(topic, message.getBytes(), qos, retained, null, new ActionListener(this, ActionListener.Action.PUBLISH, sh.clientHandle, args));
            handler.post(connecttoserversuccess2);

        }
        catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }

    }
    Runnable connecttoserversuccess2 = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
        }
    };

}
