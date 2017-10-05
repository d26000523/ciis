package com.example.minxuan.socialprojectv2.MQTT;

import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.minxuan.socialprojectv2.ListviewAdapter.messagesendadapter;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SharedSocket;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class MQTTAudioBroadcast extends ActionBarActivity {
    int select;
    ArrayList<HashMap<String, Object>> Item;
    Handler handler;
    SharedSocket sh;
    SocketUDPClient socketUDPClient;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqttaudio_broadcast);
        sh = (SharedSocket)getApplication();
        this.context = context;
        handler = new Handler();
        Item = new ArrayList<HashMap<String, Object>>();
        String user[] = sh.LIST_msg.split("\\{");
        for(int i=1;i<user.length;i++){
            String single[] = user[i].split("\n|:");
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemImage", R.drawable.boy);
            map.put("ItemName", single[2]);
            map.put("ItemPhone", single[6]);
            map.put("ItemClick", R.drawable.checkblue);
            Item.add(map);
        }
        final ListView friendview = (ListView)findViewById(R.id.friendlist);

        final messagesendadapter Btnadapter = new messagesendadapter(
                this,
                Item,
                R.layout.messagesendlsitview,
                new String[] {"ItemImage","ItemName","ItemPhone","ItemClick"},
                new int[] {R.id.ItemImage,R.id.ItemName,R.id.ItemPhone,R.id.check}
        );
        friendview.setAdapter(Btnadapter);
    }

    public void startphonecall(View v){
        try {
            socketUDPClient = new SocketUDPClient();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public class SocketUDPClient{
        private byte[] buffer = new byte[1024];
        private DatagramSocket datasock,datasock2;
        private final static int Sample_Rate = 8000;
        private final static int Channel_In_Configuration = AudioFormat.CHANNEL_IN_MONO;
        private final static int Channel_Out_Configuration = AudioFormat.CHANNEL_OUT_MONO;
        private final static int AudioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        private AudioRecord phoneMIC;
        private AudioTrack phoneSPK;
        private boolean stoped = true;
        private int recBufferSize;
        private int playBufferSize;
        private Thread inThread, outThread;
        int tag=0,type=1;

        public SocketUDPClient() throws UnknownHostException {
            //初始化(和Server連接)
            try {
                datasock = new DatagramSocket(10004);
                datasock2= new DatagramSocket(10001);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            MQTTbroadcastNotifcation();
            try {
                startPhone();
                CloseSpeaker();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //通知在線人員有新的廣播
        public void MQTTbroadcastNotifcation(){
            Thread thread2 = new Thread(new Runnable() {

                @Override
                public void run() {
                    publish("MQTTAudioBroadcast_"+sh.LocalAddress);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread2.start();
        }
        void publish(String msg)
        {
            String topic = "MQTT_SMS_SEND";
            SharedSocket sh = (SharedSocket)getApplication();
            String message = msg;
            int qos = 0;
            boolean retained = false;
            String[] args = new String[2];
            args[0] = message;
            args[1] = topic;
            try {
                Connections.getInstance(MQTTAudioBroadcast.this).getConnection(sh.clientHandle).getClient()
                        .publish(topic, message.getBytes(), qos, retained, null, new ActionListener(MQTTAudioBroadcast.this, ActionListener.Action.PUBLISH, sh.clientHandle, args));
            }
            catch (MqttSecurityException e) {
                Log.e(this.getClass().getCanonicalName(), "Failed to publish a messaged from the client with the handle " + sh.clientHandle, e);
            }
            catch (MqttException e) {
                Log.e(this.getClass().getCanonicalName(), "Failed to publish a messaged from the client with the handle " + sh.clientHandle, e);
            }
        }
        AlertDialog dialog;
        AlertDialog.Builder newmemalert;
        LayoutInflater layoutInflater;
        View add;
        Runnable connecttoserversuccess2 = new Runnable() {
            @Override
            public void run() {
                layoutInflater= LayoutInflater.from(MQTTAudioBroadcast.this);
                add = layoutInflater.inflate(R.layout.mqttvideocalls, null);
                newmemalert = new AlertDialog.Builder(MQTTAudioBroadcast.this);//對話方塊
                newmemalert.setView(add);
                dialog = newmemalert.show();
                dialog.setCancelable(false);
                LinearLayout l = (LinearLayout)add.findViewById(R.id.Endcall);
                l.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread() {
                            public void run() {
                                try {
                                    type=0;
                                    DatagramPacket pack = null;
                                    try {
                                        for(int i=0;i<sh.OnlineListener.size();i++) {
                                            final InetSocketAddress ClientAddress4 = new InetSocketAddress(sh.OnlineListener.get(i).getAddress(), 10006);
                                            pack = new DatagramPacket("end".getBytes(), "end".getBytes().length, ClientAddress4);
                                            datasock.send(pack);
                                        }
                                    } catch (SocketException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    sh.OnlineListener.clear();
                                    phoneMIC.stop();
                                    datasock2.close();
                                    dialog.dismiss();
                                    datasock.close();
                                    MQTTAudioBroadcast.this.finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                });
            }
        };

        public void startPhone() throws Exception {
            initAudioHardware();
            handler.post(connecttoserversuccess2);
            outThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Log.d("startPhoneMIC","  ");
                        startPhoneMIC();
                        CloseSpeaker();
                        //startPhoneSPK();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            outThread.start();
        }
        public void stopPhone() throws Exception {
            phoneMIC.stop();
            phoneSPK.stop();
            if(datasock!=null)
                datasock.close();
        }
        private void initAudioHardware() throws Exception {
            recBufferSize = 1024;
            playBufferSize = 1024;
            phoneMIC = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, Sample_Rate,
                    Channel_In_Configuration, AudioEncoding, recBufferSize);
            phoneSPK = new AudioTrack(AudioManager.STREAM_MUSIC, Sample_Rate,
                    Channel_Out_Configuration, AudioEncoding, playBufferSize,
                    AudioTrack.MODE_STREAM);
            phoneSPK.setStereoVolume(1f, 1f);
        }

        private void startPhoneMIC() throws Exception {
            phoneMIC.startRecording();
            phoneSPK.play();
            Log.d("startPhoneMIC", "startRecording");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while ((!Thread.interrupted()) && type==1 ) {
                        short[] compressedVoice = new short[recBufferSize/2];
                        byte[] compressedVoice2 = new byte[recBufferSize];
                        int b = phoneMIC.read(compressedVoice, 0, recBufferSize/2);
                        calc1(compressedVoice, 0, recBufferSize / 2);
                        for(int i=0,j=0;i<recBufferSize;i=i+2,j++){
                            compressedVoice2[i]= (byte)(compressedVoice[j] & 0x00FF);
                            compressedVoice2[i+1] = (byte)((compressedVoice[j] & 0xFF00) >> 8);
                        }
                        int v = 0;
                        for (int i = 0; i < compressedVoice.length; i++) {
                            v += compressedVoice[i] * compressedVoice[i];
                        }
                        double decibel = 10*Math.log10(v/(double)b);
                        DatagramPacket pack = null;
                        if(decibel > 35) {
                            try {
                                for(int i=0;i<sh.OnlineListener.size();i++) {
                                    Log.d("send",sh.OnlineListener.get(i).toString());
                                    pack = new DatagramPacket(compressedVoice2, compressedVoice2.length, sh.OnlineListener.get(i));
                                    datasock.send(pack);
                                }
                            } catch (SocketException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }

        void calc1(short[] lin,int off,int len) {
            int i,j;
            for (i = 0; i < len; i++) {
                j = lin[i+off];
                lin[i+off] = (short)(j>>2);
            }
        }
        /**啟用話筒**/
        public void CloseSpeaker(){
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if(audioManager!=null){
                if(audioManager.isSpeakerphoneOn()){
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.STREAM_VOICE_CALL);
                }
            }
        }
    }
}
