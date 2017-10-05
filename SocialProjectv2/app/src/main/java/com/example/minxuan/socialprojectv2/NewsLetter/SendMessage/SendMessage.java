package com.example.minxuan.socialprojectv2.NewsLetter.SendMessage;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.minxuan.socialprojectv2.AccountHandler;
import com.example.minxuan.socialprojectv2.Contacts.ContactsHandler;
import com.example.minxuan.socialprojectv2.Message;
import com.example.minxuan.socialprojectv2.NetworkClientHandler;
import com.example.minxuan.socialprojectv2.R;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class SendMessage extends Activity {

    int[] select;
    int checkcount=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

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

        /** 初始化選取聯絡人之陣列*/
        select = new int[ContactsHandler.item.size()];

        /** 初始化聯絡人勾取*/
        for(int i=0;i<ContactsHandler.item.size();i++){
            ContactsHandler.item.get(i).put("ItemClick",R.drawable.checkwhite);
            select[i]=0;
        }

        /** 設定ListView*/
        final ListView listView = (ListView) findViewById(R.id.friendlist);
        final messagesendadapter Btnadapter = new messagesendadapter(
                this,
                ContactsHandler.item,
                new String[] {"ItemImage","ItemName","ItemPhone","ItemClick"},
                new int[] {R.id.ItemImage,R.id.ItemName,R.id.ItemPhone,R.id.check}
        );
        listView.setAdapter(Btnadapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Btnadapter.changefocusforgroup(position, R.drawable.checkwhite, R.drawable.checkblue);

                listView.setAdapter(Btnadapter);
                if(select[position]==0) {
                    select[position] = 1;
                    checkcount++;
                }else {
                    select[position] = 0;
                    checkcount--;
                }
            }
        });

    }

    /** 發送簡訊*/
    public void SendMessage(View v) {

        final EditText editText = (EditText) findViewById(R.id.textarea);

        /** 防呆機制*/
        if(checkcount==0){
            Toast.makeText(getApplicationContext(), "Please Choose Someone!", Toast.LENGTH_SHORT).show();
        }else if("".compareTo(editText.getText().toString())==0){
            Toast.makeText(getApplicationContext(), "Please fill text field!", Toast.LENGTH_SHORT).show();
        }else{
            /** 整理訊息*/
            String gsonTemp = "";
            Gson gson = new Gson();
            Message message = new Message();

            /** 逐一包裝訊息*/
            for(int i=0; i<ContactsHandler.item.size(); i++){
                if(select[i] == 1){
                    message.setTAG("MESSAGE");
                    message.setSender(AccountHandler.phoneNumber);
                    message.setReceiver(ContactsHandler.item.get(i).get("ItemPhone").toString());
                    message.setMessage(editText.getText().toString());
                    String temp = gson.toJson(message);
                    gsonTemp += temp;

                    /** 寫入檔案*/
                    writeMessage(temp);
                }
            }

            gsonTemp = gsonTemp.replace("}{", "},{");

            /** 發送訊息*/
            NetworkClientHandler.networkClient.webSocketClient.send(gsonTemp);
            Toast.makeText(SendMessage.this, "Success!", Toast.LENGTH_SHORT).show();
            editText.setText("");
        }

    }

    /** 寫入檔案*/
    public void writeMessage(String str){

        try {
            FileWriter fileWriter = new FileWriter("/storage/emulated/0/Download/message.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter); /** 將BufferedWeiter與FileWrite物件做連結*/
            bufferedWriter.write(str);
            bufferedWriter.newLine();
            bufferedWriter.close();
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
