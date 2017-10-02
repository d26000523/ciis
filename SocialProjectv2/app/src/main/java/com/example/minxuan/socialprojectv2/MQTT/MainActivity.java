package com.example.minxuan.socialprojectv2.MQTT;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minxuan.socialprojectv2.ListviewAdapter.menulistadapter;
import com.example.minxuan.socialprojectv2.MQTT.ActionListener.Action;
import com.example.minxuan.socialprojectv2.MQTT.Connection.ConnectionStatus;
import com.example.minxuan.socialprojectv2.MQTT.service.MQTTBroadcastMemberList;
import com.example.minxuan.socialprojectv2.MQTT.service.MQTTMessageBox;
import com.example.minxuan.socialprojectv2.MQTT.service.MQTTSingleCall;
import com.example.minxuan.socialprojectv2.MQTT.service.MqttClientAndroidService;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SharedSocket;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

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

public class MainActivity extends Activity {

    private ChangeListener changeListener = new ChangeListener();
    private MainActivity clientConnections = this;
    String clientHandle;
    Context context = this;
    static String message ="";
    Button button;
    TextView textView;
    Handler handler = new Handler();
    private static final int request = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        perchec();
        final SharedSocket sh = (SharedSocket)getApplication();

        try {
            sh.datasock = new DatagramSocket(10000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        //Connect
        connect();
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                publish();
            }
        });
        thread2.start();
        //Subscribe
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String[] tmp = getLocalIpAddress().split("\\.");
                sh.LocalAddress = getLocalIpAddress();
                subscribe(new String[]{"MQTT_UE_LIST","MQTT_SMS","MQTT_SMS_$("+tmp[3]+")"});
                sh.id = tmp[3];

                try {
                    Thread.sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView mqttip = (TextView) findViewById(R.id.mqttip);
                            TextView mqttid = (TextView) findViewById(R.id.mqttid);
                            mqttip.setText("IP Address : " + getLocalIpAddress());
                            mqttid.setText("ID : " + sh.id);
                        }
                    });

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        //設定Menupage顯示的Listview
        String[] name = {"Message Box","Message","Message Broadcast","Voice Call","Voice Broadcast", "Video Call"};
        int[] pic = {R.drawable.inbox,R.drawable.message,
                R.drawable.broadcasting,R.drawable.phone,R.drawable.phone, R.drawable.singlevideocall};
        ListView listView = (ListView)findViewById(R.id.listView);
        ArrayList<HashMap<String, Object>> Item = new ArrayList<HashMap<String, Object>>();
        for(int i=0; i<6; i++)
        {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemImage", pic[i]);
            map.put("ItemName", name[i]);
            Item.add(map);
        }

        menulistadapter Btnadapter = new menulistadapter(
                this,
                Item,
                R.layout.menulistview,
                new String[] {"ItemImage","ItemName"},
                new int[] {R.id.ItemImage,R.id.ItemName}
        );
        listView.setAdapter(Btnadapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //"收件夾","簡訊","廣播簡訊","語音通話","語音廣播"
                switch (position){
                    case 1:
                        Intent i = new Intent();
                        i.setClass(MainActivity.this, MQTTSingleMessage.class);
                        startActivity(i);
                        sh.clientHandle = clientHandle;
                        break;
                    case 0:
                        try {
                            SharedSocket sh = (SharedSocket)getApplication();//拿出applicatioon
                            FileInputStream fis = null;
                            fis = openFileInput(sh.Account + "MQTT");
                            ObjectInputStream ois = new ObjectInputStream(fis);
                            sh.MQTTmessagebox = (ArrayList<HashMap<String, Object>>)ois.readObject();
                            ois.close();
                            fis.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (OptionalDataException e) {
                            e.printStackTrace();
                        } catch (StreamCorruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Intent i2 = new Intent();
                        i2.setClass(MainActivity.this, MQTTMessageBox.class);
                        startActivity(i2);
                        break;
                    case 2 :
                        Broadcasting();
                        break;
                    case 3:
                        Intent i3 = new Intent();
                        i3.setClass(MainActivity.this, MQTTSingleCall.class);
                        startActivity(i3);
                        break;
                    case 4:
                        AudioBroadcasting();
                        break;
                    case 5:
                        videoCall();
                        break;
                }
            }
        });
    }

    public void getmsg(View v){
        SharedSocket sh = (SharedSocket)getApplication();
        TextView t = (TextView)findViewById(R.id.msg);
        t.setText(sh.SMS_msg);

    }

    void connect() {
        MqttConnectOptions conOpt = new MqttConnectOptions();


        // The basic client information
        String server = "10.102.81.58";
        String clientId = "Client"+getLocalIpAddress();
        int port = 1883;
        boolean cleanSession = false;

        boolean ssl = false;
        String uri = null;
        if (ssl) {
            Log.e("SSLConnection", "Doing an SSL Connect");
            uri = "ssl://";

        }
        else {
            uri = "tcp://";
        }

        uri = uri + server + ":" + port;

        MqttClientAndroidService client;
        client = Connections.getInstance(this).createClient(this, uri, clientId);
        // create a client handle
        clientHandle = uri + clientId;

        // last will message
        String message = "Will message";
        String topic = "Will topic";
        Integer qos = 0;
        Boolean retained = false;

        // connection options

        String username = "";

        String password = "";

        int timeout = 1000;

        int keepalive = 10;

        Connection connection = new Connection(clientHandle, clientId, server, port, this, client, ssl);


        connection.registerChangeListener(changeListener);
        // connect client

        String[] actionArgs = new String[1];
        actionArgs[0] = clientId;
        connection.changeConnectionStatus(ConnectionStatus.CONNECTING);

        conOpt.setCleanSession(cleanSession);
        conOpt.setConnectionTimeout(timeout);
        conOpt.setKeepAliveInterval(keepalive);
        if (!username.equals(ActivityConstants.empty)) {
            conOpt.setUserName(username);
        }
        if (!password.equals(ActivityConstants.empty)) {
            conOpt.setPassword(password.toCharArray());
        }

        final ActionListener callback = new ActionListener(this,
                ActionListener.Action.CONNECT, clientHandle, actionArgs);

        boolean doConnect = true;

        if ((!message.equals(ActivityConstants.empty))
                || (!topic.equals(ActivityConstants.empty))) {
            // need to make a message since last will is set
            try {
                conOpt.setWill(topic, message.getBytes(), qos.intValue(),
                        retained.booleanValue());
            }
            catch (Exception e) {
                doConnect = false;
                callback.onFailure(null, e);
            }
        }
        client.setCallback(new MqttCallbackHandler(this, clientHandle));
        connection.addConnectionOptions(conOpt);
        Connections.getInstance(this).addConnection(connection);
        if (doConnect) {
            try {
                client.connect(conOpt, null, callback);
            }
            catch (MqttException e) {
                Log.e(this.getClass().getCanonicalName(),
                        "MqttException Occured", e);
            }
        }
    }

    private class ChangeListener implements PropertyChangeListener {

        /**
         * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
         */
        @Override
        public void propertyChange(PropertyChangeEvent event) {

            if (!event.getPropertyName().equals(ActivityConstants.ConnectionStatusProperty)) {
                return;
            }
            clientConnections.runOnUiThread(new Runnable() {

                @Override
                public void run() {

                }

            });

        }
    }

    void subscribe(String[] topics)
    {
        int qos = 0;

        try {
            Connections.getInstance(context).getConnection(clientHandle).getClient()
                    .subscribe(topics[0], qos, null, new ActionListener(context, Action.SUBSCRIBE, clientHandle, topics));
            Connections.getInstance(context).getConnection(clientHandle).getClient()
                    .subscribe(topics[1], qos, null, new ActionListener(context, Action.SUBSCRIBE, clientHandle, topics));
            Connections.getInstance(context).getConnection(clientHandle).getClient()
                    .subscribe(topics[2], qos, null, new ActionListener(context, Action.SUBSCRIBE, clientHandle, topics));
        }
        catch (MqttSecurityException e) {
            
            Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + topics[0] + " the client with the handle " + clientHandle, e);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to subscribe to" + topics[0] + " the client with the handle " + clientHandle, e);
        }
    }

    void publish()
    {
        String topic = "MQTT_UE_INFO";


        String[] tmp = getLocalIpAddress().split("\\.");
        String message = "imsi:"+tmp[3]+"\nip:"+getLocalIpAddress()+"\nphone:0999099999\n";
        int qos = 0;


        boolean retained = false;

        String[] args = new String[2];
        args[0] = message;
        args[1] = topic;


        try {
            SharedSocket sh = (SharedSocket)getApplication();
            sh.clientHandle = clientHandle;
            Connections.getInstance(context).getConnection(clientHandle).getClient()
                    .publish(topic, message.getBytes(), qos, retained, null, new ActionListener(context, Action.PUBLISH, clientHandle, args));
        }
        catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }

    }

    //get my IP
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        //Log.e("TAG", inetAddress.getHostAddress());

                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    AlertDialog dialog;
    AlertDialog.Builder newmemalert;
    LayoutInflater layoutInflater;
    View add;
    String sendb,passwordtest;
    public void Broadcasting(){
        layoutInflater= LayoutInflater.from(this);
        add = layoutInflater.inflate(R.layout.mqttbroadcastingcheckbox, null);
        newmemalert = new AlertDialog.Builder(this);//對話方塊
        newmemalert.setView(add);
        dialog = newmemalert.show();

        ImageView finish = (ImageView) (add.findViewById(R.id.sendbroadcst));

        sendbroadcst c = new sendbroadcst();
        finish.setOnClickListener(c);

    }
    class sendbroadcst implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final EditText e = (EditText)add.findViewById(R.id.msg);
            if ("".equals(e.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "Please fill text field!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Thread thread2 = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        publish2(e.getText().toString());

                    }
                });
                thread2.start();
            }
        }
    }
    void publish2(String msg)
    {
        String topic = "MQTT_SMS";

        SharedSocket sh = (SharedSocket)getApplication();
        String message = sh.id + ":"+msg;
        int qos = 0;


        boolean retained = false;


        String[] args = new String[2];
        args[0] = message;
        args[1] = topic;

        try {
            Connections.getInstance(this).getConnection(sh.clientHandle).getClient()
                    .publish(topic, message.getBytes(), qos, retained, null, new ActionListener(this, ActionListener.Action.PUBLISH, sh.clientHandle, args));

            handler.post(connecttoserversuccess2);

        }
        catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + clientHandle, e);
        }

    }

    public void AudioBroadcasting(){

        layoutInflater= LayoutInflater.from(this);
        add = layoutInflater.inflate(R.layout.audiobroadcastcheckbox, null);
        newmemalert = new AlertDialog.Builder(this);//對話方塊
        newmemalert.setView(add);
        dialog = newmemalert.show();

        RelativeLayout live = (RelativeLayout)add.findViewById(R.id.Live);
        RelativeLayout watch = (RelativeLayout)add.findViewById(R.id.Watch);
        liveOnClickListener f = new liveOnClickListener();
        live.setOnClickListener(f);
        watchOnClickListener f2 = new watchOnClickListener();
        watch.setOnClickListener(f2);
    }
    class liveOnClickListener implements View.OnClickListener {
        public void onClick(View v){
            Intent i = new Intent();
            i.setClass(MainActivity.this, MQTTAudioBroadcast
                    .class);
            startActivity(i);
            dialog.dismiss();
        }

    }
    class watchOnClickListener implements View.OnClickListener {
        public void onClick(View v){
            Intent i = new Intent();
            i.setClass(MainActivity.this, MQTTBroadcastMemberList.class);
            startActivity(i);
            dialog.dismiss();

        }
    }
    Runnable connecttoserversuccess2 = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), "Success !", Toast.LENGTH_SHORT).show();
        }
    };

    public void videoCall(){

        Intent i = new Intent();
        i.setClass(MainActivity.this, MQTTvideoCall.class);
        startActivity(i);
        finish();

    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   //確定按下退出鍵
            new AlertDialog.Builder(MainActivity.this)//對話方塊
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("Exit Social Project")
                    .setMessage("Are You Sure?")
                    .setCancelable(false)
                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setNegativeButton("Yes",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }
                    })
                    .show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /**取得權限**/
    public void perchec()
    {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[] {WRITE_EXTERNAL_STORAGE,
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
    }
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case request: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                }
                else {
                    Toast.makeText(getApplicationContext(), "Permission Denied !", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}
