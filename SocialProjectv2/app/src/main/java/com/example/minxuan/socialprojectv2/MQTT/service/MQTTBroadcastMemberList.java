package com.example.minxuan.socialprojectv2.MQTT.service;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.minxuan.socialprojectv2.ListviewAdapter.videoonlinememberadapter;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SharedSocket;

import java.util.ArrayList;
import java.util.HashMap;

public class MQTTBroadcastMemberList extends ActionBarActivity {
    SharedSocket sh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqttbroadcast_member_list);

        sh = (SharedSocket)getApplication();
        ArrayList<HashMap<String, Object>> Item = new ArrayList<HashMap<String, Object>>();
        for(int i=0; i<sh.BroadcastMember.size(); i++)
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemImage", R.drawable.videowatch);
            map.put("ItemName", sh.BroadcastMember.get(i).toString());
            Item.add(map);
        }
        videoonlinememberadapter Btnadapter = new videoonlinememberadapter(
                MQTTBroadcastMemberList.this,
                Item,
                R.layout.videolist,
                new String[] {"ItemImage","ItemName"},
                new int[] {R.id.ItemImage,R.id.ItemName}
        );

        ListView listView = (ListView)findViewById(R.id.onlinemember);
        listView.setAdapter(Btnadapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("key", sh.BroadcastMember.get(position).toString());
                        i.putExtras(bundle);
                        i.setClass(MQTTBroadcastMemberList.this, MQTTListenBroadcast.class);
                        startActivity(i);
                        finish();
                    }
                }).start();
            }
        });
    }
}
