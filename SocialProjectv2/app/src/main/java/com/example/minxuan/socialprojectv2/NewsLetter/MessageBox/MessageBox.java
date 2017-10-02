package com.example.minxuan.socialprojectv2.NewsLetter.MessageBox;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.example.minxuan.socialprojectv2.Message;
import com.example.minxuan.socialprojectv2.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageBox extends AppCompatActivity {
    ListView messgelist;
    Gson gson = new Gson();
    int count=0;
    private com.example.minxuan.socialprojectv2.NewsLetter.MessageBox.messageboxadapter ma;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_box);
        /**訊息顯示欄位**/
        messgelist = (ListView)findViewById(R.id.messagelist);
        /**定義每個item的資料**/
        List<Map<String, String>> itemList = new ArrayList<Map<String, String>>();
        /** 將訊息讀入*/
        try{
            FileReader fileReader = new FileReader("/storage/emulated/0/Download/message.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String temp = bufferedReader.readLine(); //readLine()讀取一整行
            Log.d("temp",temp);
            while (temp!=null){
                /** Gson 訊息解碼*/
                Type listType = new TypeToken<ArrayList<Message>>() {}.getType();
                ArrayList<Message> jsonArr = gson.fromJson("[" + temp + "]", listType);
                Message mess = jsonArr.get(0);

                /**為每一筆資料建立一個資料專屬的MAP，分別存寄信者、收信者、訊息**/
                Map<String, String> item = new HashMap<String, String>();
                item.put("sender",mess.getSender());
                item.put("receiver",mess.getReceiver());
                item.put("message",mess.getMessage());
                Log.d("re",mess.getReceiver());
                /**加入List裡面**/
                itemList.add(item);
                count++;

                /**讀取下一行**/
                temp = bufferedReader.readLine();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        ma = new com.example.minxuan.socialprojectv2.NewsLetter.MessageBox.messageboxadapter(MessageBox.this, itemList,count);
        messgelist.setAdapter(ma);
    }
}
