package com.example.minxuan.socialprojectv2.NewsLetter.MessageBox;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.minxuan.socialprojectv2.R;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by RMO on 2017/6/13.
 */

public class messageboxadapter extends BaseAdapter {

    private LayoutInflater mLayInf;
    Iterator<Map<String, String>> mItemList;
    int Count=0;

    public messageboxadapter(Context context,  List<Map<String, String>> itemList,int count) {
        mItemList = itemList.iterator();
        mLayInf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Count = count;
        String vOut = Integer.toString(Count);
        Log.d("count",vOut);
    }

    /**取得 ListView 列表 Item 的數量。通常數量就是從建構子傳入的陣列或是集合大小**/
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return Count;
    }
    /**取得 ListView 列表於 position 位置上的 Item。position 通常是資料在陣列或是集合上的位置。**/
    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    /**取得 ListView 列表於 position 位置上的 Item 的 ID，一般用 position 的值即可。**/
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    /**會設定與回傳 convertView 作為顯示在這個 position 位置的 Item 的 View。**/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        /**將listview的顯示設定為自定義的顯示方式**/
        View v = mLayInf.inflate(R.layout.messageboxlistview, parent, false);
        TextView txtsend = (TextView) v.findViewById(R.id.phonesend);
        TextView txtrev = (TextView) v.findViewById(R.id.phonerev);
        TextView txtmsg = (TextView) v.findViewById(R.id.message);

        Map<String, String> item = mItemList.next();
            txtsend.setText(item.get("sender").toString());
            txtrev.setText(item.get("receiver").toString());
            txtmsg.setText(item.get("message").toString());

        Log.d("ss",item.get("sender").toString());
       // Log.d("ss",item.get("receiver").toString());
        Log.d("ss",item.get("message").toString());
        return v;
    }
}
