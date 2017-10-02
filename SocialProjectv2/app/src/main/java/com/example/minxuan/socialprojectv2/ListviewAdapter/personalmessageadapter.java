package com.example.minxuan.socialprojectv2.ListviewAdapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.minxuan.socialprojectv2.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RMO on 2017/6/16.
 */
public class personalmessageadapter extends BaseAdapter {
    private ArrayList<HashMap<String, Object>> mAppList;
    private LayoutInflater mInflater;
    private Context mContext;
    private String[] keyString;
    private int[] valueViewID;
    private int ScreenWidth;

    private ItemView itemView;

    private class ItemView {
        TextView ItemMessage;
    }

    public personalmessageadapter(Context c, ArrayList<HashMap<String, Object>> appList, int resource, String[] from, int[] to) {
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
            convertView = mInflater.inflate(R.layout.personalmessagelistview, null);
            itemView = new ItemView();
            itemView.ItemMessage = (TextView)convertView.findViewById(valueViewID[0]);
            convertView.setTag(itemView);
        }

        HashMap<String, Object> appInfo = mAppList.get(position);
        if (appInfo != null) {
            String message = (String) appInfo.get(keyString[0]);
            itemView.ItemMessage.setText(message);


            //set ItemMessage width
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) itemView.ItemMessage.getLayoutParams();
            params.width = ScreenWidth*2/3;
            itemView.ItemMessage.setLayoutParams(params);
            //送出的訊息要至右
            /*RelativeLayout.LayoutParams layoutParams=
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            itemView.ItemMessage.setLayoutParams(layoutParams);
            itemView.ItemMessage.setBackgroundResource(R.drawable.sendmessageinmessagebox);*/
            int bottom = itemView.ItemMessage.getPaddingBottom();
            int top = itemView.ItemMessage.getPaddingTop();
            int right = itemView.ItemMessage.getPaddingRight();
            int left = itemView.ItemMessage.getPaddingLeft();

            if(mAppList.get(position).get("Itemtype")==null) {
                ((RelativeLayout) convertView).setGravity(Gravity.LEFT);
                itemView.ItemMessage.setBackgroundResource(R.drawable.receivemessageinmessagebox);
            }else if((int)mAppList.get(position).get("Itemtype")==0) {//0->send,1->receive
                ((RelativeLayout) convertView).setGravity(Gravity.RIGHT);
                itemView.ItemMessage.setBackgroundResource(R.drawable.sendmessageinmessagebox);
            }else if((int)mAppList.get(position).get("Itemtype")==1){
                ((RelativeLayout) convertView).setGravity(Gravity.LEFT);
                itemView.ItemMessage.setBackgroundResource(R.drawable.receivemessageinmessagebox);
            }
            itemView.ItemMessage.setPadding(left, top, right, bottom);
        }

        return convertView;
    }
    public void addwidthofscreen(int width){
        ScreenWidth = width;
    }


}
