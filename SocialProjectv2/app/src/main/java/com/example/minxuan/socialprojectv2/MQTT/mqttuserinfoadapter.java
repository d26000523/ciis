package com.example.minxuan.socialprojectv2.MQTT;

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
 * Created by Rmo on 2017/10/5.
 */

public class mqttuserinfoadapter extends BaseAdapter {
    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;

    private ItemView itemView;

    private class ItemView {
        TextView itemName;
        TextView itemMessage;
    }

    public mqttuserinfoadapter(Context c, ArrayList<HashMap<String, Object>> appList, String[] from, int[] to){
        mAppList = appList;
        mContext = c;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyString = new String[from.length];
        valueViewID = new int[to.length];
        System.arraycopy(from, 0, keyString, 0, from.length);
        System.arraycopy(to, 0, valueViewID, 0, to.length);
    }

    public int getCount() {
        return mAppList.size();
    }

    public Object getItem(int position) {
        return mAppList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView != null) {
            itemView = (ItemView) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.userinfolistview, null);
            itemView = new ItemView();
            itemView.itemName = (TextView)convertView.findViewById(valueViewID[0]);
            itemView.itemMessage = (TextView)convertView.findViewById(valueViewID[1]);
            convertView.setTag(itemView);
        }

        HashMap<String, Object> appInfo = mAppList.get(position);
        if(appInfo != null){
            String name = (String) appInfo.get(keyString[0]);
            String message = (String) appInfo.get(keyString[1]);
            itemView.itemName.setText(name);
            itemView.itemMessage.setText(message);
        }
        return convertView;
    }
}
