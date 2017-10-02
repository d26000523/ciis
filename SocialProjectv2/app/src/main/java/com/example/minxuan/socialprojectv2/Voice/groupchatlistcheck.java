package com.example.minxuan.socialprojectv2.Voice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.minxuan.socialprojectv2.Message;
import com.example.minxuan.socialprojectv2.NetworkClientHandler;
import com.example.minxuan.socialprojectv2.R;
import com.google.gson.Gson;




public class groupchatlistcheck extends AppCompatActivity {
    ListView listView;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchatlistcheck);
        listView = (ListView) this.findViewById(R.id.voiceonlinemember);

        register();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        init();

    }

    public void register() {

        /** 清空LIST*/
        for(int i=0;i<NetworkClientHandler.watch_list.size();i++){
            NetworkClientHandler.watch_list.remove(0);
        }

        /** 整理登入訊息*/
        Gson gson = new Gson();
        Message message = new Message();
        message.setTAG("VOICE_LIST");
        message.setVoiceList("get");
        String gsonStr = gson.toJson(message);

        /** 送出登入訊息*/
        NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);
    }

    public void init(){

        if(NetworkClientHandler.watch_list.isEmpty()){
            Toast.makeText(this, "目前沒有群聊頻道喔!", Toast.LENGTH_SHORT).show();
        }else{
            adapter = new ArrayAdapter<String>(this,R.layout.voicegrouplistview,R.id.vlName, NetworkClientHandler.watch_list);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("key", listView.getItemAtPosition(position).toString());
                            i.putExtras(bundle);
                            i.setClass(groupchatlistcheck.this, VoiceGroupListener.class);
                            startActivity(i);
                        }
                    }).start();
                }
            });
        }
    }
}

