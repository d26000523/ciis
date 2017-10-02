package com.example.minxuan.socialprojectv2.Contacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minxuan.socialprojectv2.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
/**通訊錄功能**/
public class Contacts extends Activity {
    int tag = 0;
    int pposition = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        /** 開檔將通訊人資料寫進Buffer*/
        try{
            FileInputStream fis = this.openFileInput("contacts");
            ObjectInputStream ois = new ObjectInputStream(fis);
            ContactsHandler.item = (ArrayList<HashMap<String, Object>>)ois.readObject();
            ois.close();
            fis.close();

            for(int i=0;i<ContactsHandler.item.size();i++){
                ContactsHandler.gender.add(ContactsHandler.item.get(i).get("Itemgender").toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        /** 將圖片縮小且維持長寬相等*/
        final ImageView imageView = (ImageView)findViewById(R.id.plus);
        ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                float x = imageView.getHeight();
                imageView.getLayoutParams().width = (int) x;
                imageView.requestLayout();
            }
        });

        /** 設定Adapter並更新通訊錄*/
        ListView friendview = (ListView)findViewById(R.id.contactlist);
        contactslistadapter contactslistadapter = new contactslistadapter(
                this,
                ContactsHandler.item,
                new String[] {"ItemImage","ItemName","ItemPhone"},
                new int[] {R.id.ItemImage,R.id.ItemName,R.id.ItemPhone}
        );
        friendview.setAdapter(contactslistadapter);

        /** 更改通訊人資料*/
        ChangeContactinfo change = new ChangeContactinfo();
        friendview.setOnItemLongClickListener(change);

        /** 檢視通訊人資料*/
        WatchOtherinfo watch = new WatchOtherinfo();
        friendview.setOnItemClickListener(watch);

    }

    /** 更改或新增通訊人*/
    AlertDialog dialog;
    AlertDialog.Builder newmemalert;
    LayoutInflater layoutInflater;
    View add;
    public void addmember(View V){
        changeoraddmember(-1);
        tag = 1;
    }
    public void changeoraddmember(int position){

        layoutInflater= LayoutInflater.from(this);
        add = layoutInflater.inflate(R.layout.addnewcontacts, null);
        newmemalert = new AlertDialog.Builder(this);//對話方塊
        newmemalert.setView(add);
        dialog = newmemalert.show();
        final CheckBox boy = (CheckBox)add.findViewById(R.id.boy);
        final CheckBox girl = (CheckBox)add.findViewById(R.id.girl);

        TextView finish = (TextView) (add.findViewById(R.id.addmemfinish));
        TextView cancel = (TextView) (add.findViewById(R.id.addmemcancel));

        if(tag==2){

            TextView title = (TextView)add.findViewById(R.id.title);
            EditText name = (EditText)add.findViewById(R.id.nametext2);
            EditText phone = (EditText)add.findViewById(R.id.phonetext);
            EditText other = (EditText)add.findViewById(R.id.othertext);
            title.setText("Edit User Data");
            name.setText(ContactsHandler.item.get(position).get("ItemName").toString(), TextView.BufferType.EDITABLE);
            phone.setText(ContactsHandler.item.get(position).get("ItemPhone").toString(), TextView.BufferType.EDITABLE);
            other.setText(ContactsHandler.item.get(position).get("Itemother").toString(), TextView.BufferType.EDITABLE);
            if(ContactsHandler.item.get(position).get("Itemgender").toString().compareTo("boy")==0){
                boy.setChecked(true);
            }
            else{
                girl.setChecked(true);
            }
        }

        /** 設定OnClickerListener*/
        cancelOnClickListener c = new cancelOnClickListener();
        cancel.setOnClickListener(c);
        finishOnClickListener f = new finishOnClickListener();
        finish.setOnClickListener(f);

        /** 讓checkbox不能同時兩個都按*/
        boy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                girl.setChecked(false);

            }
        });
        girl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boy.setChecked(false);

            }
        });

    }

    /** 完成新增通訊人資料*/
    class finishOnClickListener implements View.OnClickListener {
        public void onClick(View v){
            ListView friendview = (ListView)findViewById(R.id.contactlist);
            EditText name = (EditText)add.findViewById(R.id.nametext2);
            EditText phone = (EditText)add.findViewById(R.id.phonetext);
            EditText other = (EditText)add.findViewById(R.id.othertext);
            CheckBox boy = (CheckBox)add.findViewById(R.id.boy);
            CheckBox girl = (CheckBox)add.findViewById(R.id.girl);

            String gen = "boy";
            int drawgen = R.drawable.boy;
            if (boy.isChecked() == true) {
                ContactsHandler.gender.add("boy");
                gen = "boy";
                drawgen = R.drawable.boy;
            } else if (girl.isChecked() == true) {
                ContactsHandler.gender.add("girl");
                gen = "girl";
                drawgen = R.drawable.girl;
            }

            if(tag == 1) {  /** 新增聯絡人*/

                HashMap<String, Object> map = new HashMap<String, Object>();

                map.put("ItemImage", drawgen);
                map.put("ItemName", name.getText().toString());
                map.put("ItemPhone", phone.getText().toString());
                map.put("Itemgender", gen);
                map.put("Itemother", other.getText().toString());
                if("".equals(name.getText().toString().trim())||"".equals(phone.getText().toString().trim())){
                    Toast.makeText(getApplicationContext(), "Please fill Name & Phone field !", Toast.LENGTH_SHORT).show();
                }else
                    ContactsHandler.item.add(map);
            }else if(tag == 2){//replace
                ContactsHandler.item.get(pposition).put("ItemImage", drawgen);
                ContactsHandler.item.get(pposition).put("ItemName", name.getText().toString());
                ContactsHandler.item.get(pposition).put("ItemPhone", phone.getText().toString());
                ContactsHandler.item.get(pposition).put("Itemgender", gen);
                ContactsHandler.item.get(pposition).put("Itemother", other.getText().toString());
            }
            try {
                FileOutputStream fout = Contacts.this.openFileOutput("contacts", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(ContactsHandler.item);
                oos.close();
                fout.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /** 設定Listview的adapter*/
            contactslistadapter Btnadapter = new contactslistadapter(
                    Contacts.this,
                    ContactsHandler.item,
                    new String[] {"ItemImage","ItemName","ItemPhone"},
                    new int[] {R.id.ItemImage, R.id.ItemName, R.id.ItemPhone}
            );
            friendview.setAdapter(Btnadapter);
            dialog.dismiss();
        }
    }
    /** 取消新增通訊人資料*/
    class cancelOnClickListener implements View.OnClickListener {
        public void onClick(View v){
            dialog.dismiss();
        }
    }

    /** 長壓更改通訊人資料*/
    AlertDialog dialog2;
    AlertDialog.Builder newmemalert2;
    LayoutInflater layoutInflater2;
    View add2;
    class ChangeContactinfo implements AdapterView.OnItemLongClickListener{

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

            layoutInflater2= LayoutInflater.from(Contacts.this);
            add2 = layoutInflater2.inflate(R.layout.modifyordeletemember, null);
            newmemalert2 = new AlertDialog.Builder(Contacts.this);//對話方塊
            newmemalert2.setView(add2);
            dialog2 = newmemalert2.show();

            RelativeLayout m = (RelativeLayout)add2.findViewById(R.id.modify);
            RelativeLayout d = (RelativeLayout)add2.findViewById(R.id.delete);
            m.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    pposition = position;
                    tag = 2;
                    changeoraddmember(position);
                    dialog2.dismiss();
                }
            });
            d.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListView friendview = (ListView)findViewById(R.id.contactlist);
                    ContactsHandler.item.remove(position);
                    contactslistadapter Btnadapter = new contactslistadapter(
                            Contacts.this,
                            ContactsHandler.item,
                            new String[] {"ItemImage","ItemName","ItemPhone"},
                            new int[] {R.id.ItemImage,R.id.ItemName,R.id.ItemPhone}
                    );
                    friendview.setAdapter(Btnadapter);
                    try {
                        FileOutputStream fout = Contacts.this.openFileOutput("contacts", Context.MODE_PRIVATE);
                        ObjectOutputStream oos = new ObjectOutputStream(fout);
                        oos.writeObject(ContactsHandler.item);
                        oos.close();
                        fout.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dialog2.dismiss();


                }
            });

            return true;
        }
    }
    class WatchOtherinfo implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            new AlertDialog.Builder(Contacts.this)//對話方塊
                    .setTitle("Remark")
                    .setMessage(ContactsHandler.item.get(position).get("Itemother").toString())
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }
    }
}
