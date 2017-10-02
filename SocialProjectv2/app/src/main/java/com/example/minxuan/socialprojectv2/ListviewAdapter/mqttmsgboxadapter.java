package com.example.minxuan.socialprojectv2.ListviewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.minxuan.socialprojectv2.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RMO on 2017/6/16.
 */

public class mqttmsgboxadapter extends BaseAdapter {

    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;

    private ItemView itemView;

    private class ItemView {
        TextView ItemPhone;
        TextView ItemLastMessage;
    }

    public mqttmsgboxadapter(Context c, ArrayList<HashMap<String, Object>> appList, int resource, String[] from, int[] to) {
        mAppList = appList;
        mContext = c;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyString = new String[from.length];
        valueViewID = new int[to.length];
        System.arraycopy(from, 0, keyString, 0, from.length);
        System.arraycopy(to, 0, valueViewID, 0, to.length);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        //return 0;
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        //return null;
        return mAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        //return 0;
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        //return null;

        if (convertView != null) {
            itemView = (ItemView) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.messageboxlistview, null);
            itemView = new ItemView();
            itemView.ItemPhone = (TextView)convertView.findViewById(valueViewID[0]);
            itemView.ItemLastMessage = (TextView)convertView.findViewById(valueViewID[1]);
            convertView.setTag(itemView);
        }

        HashMap<String, Object> appInfo = mAppList.get(position);
        if (appInfo != null) {

            String phone = (String) appInfo.get(keyString[0]);
            ArrayList<HashMap<String, Object>> message = (ArrayList<HashMap<String, Object>>) appInfo.get(keyString[1]);
            itemView.ItemPhone.setText(phone);
            int lastmessageposition = message.size();
            itemView.ItemLastMessage.setText(message.get(lastmessageposition-1).get("ItemMessage").toString());
            // itemView.ItemLastMessage.setTypeface(font);

        }

        return convertView;
    }
}
