package com.example.minxuan.socialprojectv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

public class CreateAccount extends Activity {

    public NetworkClient networkClient=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
    }

    public void Finish(View V){

        /** 擷取欄位資訊*/
        EditText s = (EditText)findViewById(R.id.create_account_serverAddr);
        EditText f = (EditText)findViewById(R.id.create_account_lastName);
        EditText n = (EditText)findViewById(R.id.create_account_firstName);
        EditText pn = (EditText)findViewById(R.id.create_account_phoneNumber);
        EditText e = (EditText)findViewById(R.id.create_account_Email);
        EditText a = (EditText)findViewById(R.id.create_account_account);
        EditText p = (EditText)findViewById(R.id.create_account_passord);
        EditText cp = (EditText)findViewById(R.id.create_account_confirmPassword);
        final String serverAddr = s.getText().toString();
        final String lastName = f.getText().toString();
        final String firstName = n.getText().toString();
        final String phonenumber = pn.getText().toString();
        final String email = e.getText().toString();
        final String account = a.getText().toString();
        final String password = p.getText().toString();
        final String confirmpassword = cp.getText().toString();

        /** 防呆機制*/
        if("".equals(s.getText().toString().trim())||"".equals(f.getText().toString().trim())||"".equals(n.getText().toString().trim())||"".equals(pn.getText().toString().trim())||"".equals(e.getText().toString().trim())||"".equals(a.getText().toString().trim())||"".equals(p.getText().toString().trim())||"".equals(cp.getText().toString().trim())){
            Toast.makeText(getApplicationContext(), "Empty Field!", Toast.LENGTH_SHORT).show();
        }else{
            /** 確認密碼*/
            if (confirmpassword.compareTo(password) != 0) {
                new AlertDialog.Builder(CreateAccount.this)//對話方塊
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Alert")
                        .setMessage("Please check password field !")
                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CreateAccount.this.finish();
                            }
                        }).show();
            } else {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        /** 宣告WebSocketClient*/
                        networkClient = new NetworkClient(serverAddr, account, password);
                        networkClient.setActivity(CreateAccount.this);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }



                        /** 整理訊息*/
                        Gson gson = new Gson();
                        Message message = new Message();
                        message.setTAG("CREATE_NEW");
                        message.setLastName(lastName);
                        message.setFirstName(firstName);
                        message.setPhonenumber(phonenumber);
                        message.setEmail(email);
                        message.setAccount(account);
                        message.setPassword(password);
                        message.setConfirmpassword(confirmpassword);
                        String gsonStr = gson.toJson(message);

                        /** 送出註冊訊息*/
                        networkClient.webSocketClient.send(gsonStr);
                    }
                }).start();
            }
        }
    }
    public void Cancel(View V)
    {
        Intent intent = new Intent();
        intent.setClass(CreateAccount.this, HomeActivity.class);
        this.startActivity(intent);
    }
}
