package com.example.minxuan.socialprojectv2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.minxuan.socialprojectv2.MQTT.MainActivity;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class HomeActivity extends AppCompatActivity {

    private Button login;
    private Button create;
    private static final int request = 1;
    Boolean checkfill = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        create = (Button) findViewById(R.id.home_createNew);
        login = (Button) findViewById(R.id.home_signIN);

        permissionCheck();

        String ip = getLocalIpAddress();
        if(ip==null)
        {
            Toast.makeText(getApplicationContext(), "Internet isn't found !", Toast.LENGTH_LONG).show();
            create.setEnabled(false);
            login.setEnabled(false);
        }

        /**
         *      將登入紀錄預先顯示在EditeTextView上
         * */
        String[] userinfo;
        try {
            FileInputStream fis = this.openFileInput("current_user_info");
            ObjectInputStream ois = new ObjectInputStream(fis);
            userinfo = (String[]) ois.readObject();
            ois.close();
            fis.close();

            EditText et_server_address = (EditText)findViewById(R.id.home_serverAddr);
            EditText et_account = (EditText) findViewById(R.id.home_account);
            EditText et_password = (EditText) findViewById(R.id.home_password);

            et_server_address.setText(userinfo[0]);
            et_account.setText(userinfo[1]);
            et_password.setText(userinfo[2]);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     *  跳轉到註冊介面
     * */
    public void CreateNew(View view){
        Intent intent = new Intent();
        intent.setClass(HomeActivity.this, CreateAccount.class);
        startActivity(intent);
    }

    /**
     *   按下登入後
     *   1.寫入登入資訊
     *   2.檢查連線模式, normal mode 或是 all -in-one mode
     * */
    public void SignIN(View view){

        /**  將各個editText的內容寫入current user inifo 檔案, 以供下次開啟APP自動預設**/
        EditText et_server_address = (EditText)findViewById(R.id.home_serverAddr);
        EditText et_account = (EditText)findViewById(R.id.home_account);
        EditText et_password = (EditText)findViewById(R.id.home_password);
        final String serverAddr = et_server_address.getText().toString();
        final String account = et_account.getText().toString();
        final String password = et_password.getText().toString();

        /** 判斷是否所有欄位皆填入*/
        if("".compareTo(account)==0 || "".compareTo(password)==0 || "".compareTo(serverAddr)==0){
            Toast.makeText(getApplicationContext(), "Please fill every field!", Toast.LENGTH_SHORT).show();
        }else{
            checkfill = true;
            String[] userinfo = new String[3];
            userinfo[0] = serverAddr;
            userinfo[1] = account;
            userinfo[2] = password;

            /** 將登入資訊寫出檔案*/
            try {
                FileOutputStream fos = HomeActivity.this.openFileOutput("current_user_info", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(userinfo);
                oos.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /** 宣告WebSocketClient*/
        NetworkClientHandler.setNetworkClient(serverAddr, account, password);
        NetworkClientHandler.networkClient.setActivity(HomeActivity.this);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Loading for sign in...");
        progressDialog.setCancelable(false);
        progressDialog.create();
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /** 整理登入訊息*/
                Gson gson = new Gson();
                Message message = new Message();
                message.setTAG("LOGIN");
                message.setAccount(account);
                message.setPassword(password);
                message.setMessage(NetworkClientHandler.getLocalIpAddress());
                String gsonStr = gson.toJson(message);


                /** 送出登入訊息*/
                try{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });
                    NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);

                }catch (Exception e){
                    Log.i("error","error");
                    if(checkfill==true)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                        progressDialog.dismiss();
                            }
                        });

                        Intent intent = new Intent();
                        intent.setClass(HomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

            }
        }).start();

    }

    /**按下返回鍵判斷**/
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //確定按下退出鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            new AlertDialog.Builder(HomeActivity.this)//對話方塊
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("Social Project")
                    .setMessage("Exit ?")
                    .setCancelable(false)
                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNegativeButton("Yes",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            HomeActivity.this.finish();
                        }
                    })
                    .show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /**取得權限**/
    public void permissionCheck(){

        ActivityCompat.requestPermissions(HomeActivity.this,
                new String[] {
                        WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE,
                        ACCESS_FINE_LOCATION,
                        ACCESS_WIFI_STATE,
                        CHANGE_WIFI_STATE,
                        ACCESS_NETWORK_STATE,
                        INTERNET,
                        CAMERA,
                        RECORD_AUDIO,
                        READ_PHONE_STATE,
                        WAKE_LOCK},
                request);
        //}
    }
    /**權限判斷回饋*/
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case request: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                }
                else {
                    Toast.makeText(getApplicationContext(), "Permission denied!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
