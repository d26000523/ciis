package com.example.minxuan.socialprojectv2.Contacts;

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
 * Created by MinXuan on 2017/6/5.
 */

public class contactslistadapter extends BaseAdapter {

    private ArrayList<HashMap<String, Object>> contactsList;
    private LayoutInflater layoutInflater;
    private Context context;
    private String[] keyString;
    private int[] valueViewID;

    /** 聯絡人頭像*/
    private ItemView itemView;
    private class ItemView {
        ImageView ItemImage;
        TextView ItemName;
        TextView ItemPhone;
    }

    public contactslistadapter(Context context, ArrayList<HashMap<String, Object>> contactsList, String[] from, int[] to){
        this.contactsList = contactsList;
        this.context = context;
        layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        keyString = new String[from.length];
        valueViewID = new int[to.length];
        System.arraycopy(from, 0, keyString, 0, from.length);
        System.arraycopy(to, 0, valueViewID, 0, to.length);
    }


    @Override
    public int getCount() {
        return this.contactsList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.contactsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView != null){
            itemView = (ItemView) convertView.getTag();
        }else{
            convertView = layoutInflater.inflate(R.layout.contactslistview, null);
            itemView = new ItemView();
            itemView.ItemImage = (ImageView)convertView.findViewById(valueViewID[0]);
            itemView.ItemName = (TextView)convertView.findViewById(valueViewID[1]);
            itemView.ItemPhone = (TextView)convertView.findViewById(valueViewID[2]);
            convertView.setTag(itemView);
        }

        HashMap<String, Object> contactInfo = contactsList.get(position);
        if(contactInfo != null){

            int mid = (Integer)contactInfo.get(keyString[0]);
            String name = (String) contactInfo.get(keyString[1]);
            String phone = (String) contactInfo.get(keyString[2]);
            itemView.ItemName.setText(name);
            itemView.ItemPhone.setText(phone);
            itemView.ItemImage.setImageDrawable(itemView.ItemImage.getResources().getDrawable(mid));
        }


        return convertView;
    }
}
