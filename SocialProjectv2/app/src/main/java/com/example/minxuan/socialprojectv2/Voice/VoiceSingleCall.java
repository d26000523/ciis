package com.example.minxuan.socialprojectv2.Voice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.minxuan.socialprojectv2.Contacts.ContactsHandler;
import com.example.minxuan.socialprojectv2.R;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class VoiceSingleCall extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_single_call);
        /** 讀取通訊錄資料*/
        try{
            FileInputStream fis = this.openFileInput("contacts");
            ObjectInputStream ois = new ObjectInputStream(fis);
            ContactsHandler.item = (ArrayList<HashMap<String, Object>>)ois.readObject();
            ois.close();
            fis.close();

        }catch (Exception e){
            e.printStackTrace();
        }

        /** 設定ListView*/
        final ListView listView = (ListView) findViewById(R.id.voice_single_call);
        final voicesinglecalladapter voicesinglecalladapter = new voicesinglecalladapter(
                this,
                ContactsHandler.item,
                new String[] {"ItemImage","ItemName","ItemPhone"},
                new int[] {R.id.ItemImage,R.id.ItemName,R.id.ItemPhone}
        );
        listView.setAdapter(voicesinglecalladapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("VOICE", "REQUEST");
                        bundle.putString("key",ContactsHandler.item.get(position).get("ItemPhone").toString());
                        Log.e("key", ContactsHandler.item.get(position).get("ItemPhone").toString());
                        i.putExtras(bundle);
                        i.setClass(VoiceSingleCall.this, Startvoicecall.class);
                        startActivity(i);
                    }
                }).start();
            }
        });
    }
}
