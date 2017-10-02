package com.example.minxuan.socialprojectv2.MQTT.service;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.widget.ListView;

import com.example.minxuan.socialprojectv2.ListviewAdapter.personalmessageadapter;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SharedSocket;

import java.util.ArrayList;
import java.util.HashMap;

public class MQTTPersonalMessageBox extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqttpersonal_message_box);
        Bundle b = this.getIntent().getExtras();
        int position = b.getInt("position");

        SharedSocket sh = (SharedSocket) getApplication();//拿出applicatioon
        ListView mess = (ListView) findViewById(R.id.personalmessage);
        personalmessageadapter Btnadapter = new personalmessageadapter(
                MQTTPersonalMessageBox.this,
                (ArrayList<HashMap<String, Object>>) sh.MQTTmessagebox.get(position).get("ItemMessagebox"),
                R.layout.personalmessagelistview,
                new String[]{"ItemMessage"},
                new int[]{R.id.messagetext}
        );
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Btnadapter.addwidthofscreen(metrics.widthPixels);

        mess.setAdapter(Btnadapter);
    }
}

