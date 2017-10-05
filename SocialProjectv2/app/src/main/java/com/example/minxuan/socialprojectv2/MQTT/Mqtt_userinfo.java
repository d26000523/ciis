package com.example.minxuan.socialprojectv2.MQTT;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.example.minxuan.socialprojectv2.R;

import java.util.ArrayList;
import java.util.HashMap;

public class Mqtt_userinfo extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_userinfo);
        ArrayList<HashMap<String, Object>> userDataList = new ArrayList<HashMap<String, Object>>();
        String[] info = new String[]{"Server IP","User ID","User IMSI","User Phone","User IP"};
        Intent intent = getIntent();
        String[] userData = intent.getStringArrayExtra("info");

        for (int i=0;i<info.length;i++){
            HashMap<String, Object> map = new HashMap<>();
            map.put("ItemName", info[i]);
            map.put("ItemMessage", userData[i]);
            userDataList.add(map);
        }

        mqttuserinfoadapter mqttuserinfoadapter = new mqttuserinfoadapter(
                Mqtt_userinfo.this,
                userDataList,
                new String[] {"ItemName", "ItemMessage"},
                new int[] {R.id.phonetext,R.id.latestmessage}
        );
        ListView messgelit = (ListView) findViewById(R.id.mqtttuserinfo);
        messgelit.setAdapter(mqttuserinfoadapter);
    }
}
