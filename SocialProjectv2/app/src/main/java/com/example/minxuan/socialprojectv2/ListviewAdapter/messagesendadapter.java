package com.example.minxuan.socialprojectv2.ListviewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.minxuan.socialprojectv2.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RMO on 2017/6/16.
 */

public class messagesendadapter extends BaseAdapter {
    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;

    private ItemView itemView;

    private class ItemView {
        ImageView ItemImage;
        ImageView ItemClick;
        TextView ItemName;
        TextView ItemPhone;
    }

    public messagesendadapter(Context c, ArrayList<HashMap<String, Object>> appList, int resource, String[] from, int[] to) {
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
            convertView = mInflater.inflate(R.layout.messagesendlsitview, null);
            itemView = new ItemView();
            itemView.ItemImage = (ImageView)convertView.findViewById(valueViewID[0]);
            itemView.ItemName = (TextView)convertView.findViewById(valueViewID[1]);
            itemView.ItemPhone = (TextView)convertView.findViewById(valueViewID[2]);
            itemView.ItemClick = (ImageView)convertView.findViewById(valueViewID[3]);
            convertView.setTag(itemView);
        }

        HashMap<String, Object> appInfo = mAppList.get(position);
        if (appInfo != null) {

            int mid = (Integer)appInfo.get(keyString[0]);
            String name = (String) appInfo.get(keyString[1]);
            String phone = (String) appInfo.get(keyString[2]);
            int check = (Integer)appInfo.get(keyString[3]);
            itemView.ItemName.setText(name);
            itemView.ItemPhone.setText(phone);
            itemView.ItemImage.setImageResource(mid);
            itemView.ItemClick.setImageResource(check);
        }

        return convertView;
    }
    public void changefocus(int position,int white,int color){//單人
        for(int i=0;i<mAppList.size();i++){
            if(i!=position)
                mAppList.get(i).put("ItemClick",white);
            else
                mAppList.get(i).put("ItemClick",color);
        }

    }
    public void changefocusforgroup(int position,int white,int color){//群組
        if((int)mAppList.get(position).get("ItemClick")==white)
            mAppList.get(position).put("ItemClick",color);
        else
            mAppList.get(position).put("ItemClick",white);

    }
}
