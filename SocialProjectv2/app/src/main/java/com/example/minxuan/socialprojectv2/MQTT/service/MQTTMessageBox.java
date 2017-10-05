package com.example.minxuan.socialprojectv2.MQTT.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.minxuan.socialprojectv2.ListviewAdapter.mqttmsgboxadapter;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SharedSocket;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MQTTMessageBox extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqttmessage_box);

        SharedSocket sh = (SharedSocket)getApplication();//拿出applicatioon
        ListView messgelist = (ListView)findViewById(R.id.messagelist);
        Log.d("refreshmsg", sh.USER_msg.size() + "");
        for(int i=0;i<sh.USER_msg.size();i++) {//處理每筆訊息
            Log.d("refreshmsg", sh.USER_msg.get(i));
            int tag=0;
            String[] tmp = sh.USER_msg.get(i).split(":");
            String messagephone = tmp[0];//sender的imsi
            for(int j=0;j<sh.MQTTmessagebox.size();j++){
                if(messagephone.compareTo(sh.MQTTmessagebox.get(j).get("ItemPhone").toString())==0){//找到相同的話更新
                    HashMap<String, Object> map = sh.MQTTmessagebox.get(j);
                    ArrayList<HashMap<String, Object>> messbox = (ArrayList<HashMap<String, Object>>) map.get("ItemMessagebox");

                    HashMap<String, Object> mess = new HashMap<String, Object>();//新建一筆資料
                    mess.put("ItemMessage",tmp[1]);
                    mess.put("Itemtype",1);
                    messbox.add(mess);//加到此sender的arraylist中

                    sh.MQTTmessagebox.remove(j);
                    sh.MQTTmessagebox.add(0, map);
                    tag=1;

                }
            }
            if(tag==0){//新建一個新的view插入messagebox
                HashMap<String, Object> map= new HashMap<String, Object>();

                ArrayList<HashMap<String, Object>> messbox = new ArrayList<HashMap<String, Object>>();
                HashMap<String, Object> mess = new HashMap<String, Object>();//新建一筆資料
                mess.put("ItemMessage",tmp[1]);
                mess.put("Itemtype",1);
                messbox.add(mess);//加到此sender的arraylist中

                map.put("ItemPhone", messagephone);
                map.put("ItemMessagebox", messbox);
                sh.MQTTmessagebox.add(0, map);
            }

        }
        sh.USER_msg.clear();

        try {
            FileOutputStream fout = this.openFileOutput(sh.Account + "MQTT", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(sh.MQTTmessagebox);
            oos.close();
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mqttmsgboxadapter Btnadapter = new mqttmsgboxadapter(
                this,
                sh.MQTTmessagebox,
                R.layout.mqttmsgboxlistview,
                new String[] {"ItemPhone","ItemMessagebox"},
                new int[] {R.id.phonetext,R.id.latestmessage}
        );
        messgelist.setAdapter(Btnadapter);

        messgelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent();
                i.setClass(MQTTMessageBox.this, MQTTPersonalMessageBox.class);
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                //將Bundle物件assign給intent
                i.putExtras(bundle);
                startActivity(i);
            }
        });

    }
}

