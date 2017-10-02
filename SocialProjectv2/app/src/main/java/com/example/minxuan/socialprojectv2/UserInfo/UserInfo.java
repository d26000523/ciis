package com.example.minxuan.socialprojectv2.UserInfo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.example.minxuan.socialprojectv2.AccountHandler;
import com.example.minxuan.socialprojectv2.R;

import java.util.ArrayList;
import java.util.HashMap;

public class UserInfo extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        ArrayList<HashMap<String, Object>> userDataList = new ArrayList<HashMap<String, Object>>();
        String[] info = new String[]{"Phone","Name","E-mail","Accounts"};
        String[] userData = new String[]{AccountHandler.phoneNumber, AccountHandler.firstName+" "+AccountHandler.lastName, AccountHandler.email, AccountHandler.account};

        for (int i=0;i<info.length;i++){
            HashMap<String, Object> map = new HashMap<>();

            map.put("ItemName", info[i]);
            map.put("ItemMessage", userData[i]);
            userDataList.add(map);
        }

        userinfoadapter userinfoadapter = new userinfoadapter(
                UserInfo.this,
                userDataList,
                new String[] {"ItemName", "ItemMessage"},
                new int[] {R.id.phonetext,R.id.latestmessage}
        );
        ListView messgelist = (ListView) findViewById(R.id.userinf);
        messgelist.setAdapter(userinfoadapter);

    }
}
