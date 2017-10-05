package com.example.minxuan.socialprojectv2.MQTT.service;

import android.app.AlertDialog;
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

import com.example.minxuan.socialprojectv2.MQTT.ActionListener;
import com.example.minxuan.socialprojectv2.MQTT.Connections;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SharedSocket;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MQTTListenBroadcast extends ActionBarActivity {
    SharedSocket sh;
    String id;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqttlisten_broadcast);
        sh = (SharedSocket)getApplication();
        handler = new Handler();

        Bundle bundle = this.getIntent().getExtras();
        final String ip =  bundle.getString("key");
        String getid[] = ip.split("\\.");
        id = getid[2]+"_"+getid[3];
        sendaccept();
        try {
            SocketUDPClient socketUDPClient = new SocketUDPClient();
            socketUDPClient.startPhone();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendaccept(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                publish(id,"MQTTAcceptAudio_"+sh.LocalAddress);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();

    }
    public void leave(View v){
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                publish(id,"MQTTLeaveAudio_"+sh.LocalAddress);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();

    }
    void publish(String id,String msg)
    {
        String topic = "MQTT_SMS_SEND_$("+id+")";
        SharedSocket sh = (SharedSocket)getApplication();
        String message = msg;
        int qos = 0;
        boolean retained = false;
        String[] args = new String[2];
        args[0] = message;
        args[1] = topic;
        try {
            Connections.getInstance(this).getConnection(sh.clientHandle).getClient()
                    .publish(topic, message.getBytes(), qos, retained, null, new ActionListener(this, ActionListener.Action.PUBLISH, sh.clientHandle, args));
        }
        catch (MqttSecurityException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + sh.clientHandle, e);
        }
        catch (MqttException e) {
            Log.e(this.getClass().getCanonicalName(), "Failed to publish a messged from the client with the handle " + sh.clientHandle, e);
        }

    }
    class SocketUDPClient{
        private byte[] buffer = new byte[1024];
        private DatagramSocket datasock,datasock2;
        private DatagramPacket Datapack = new DatagramPacket(buffer, buffer.length);
        private DatagramPacket Datapack2 = new DatagramPacket(buffer, buffer.length);
        private final static int Sample_Rate = 8000;
        private final static int Channel_In_Configuration = AudioFormat.CHANNEL_IN_MONO;
        private final static int Channel_Out_Configuration = AudioFormat.CHANNEL_OUT_MONO;
        private final static int AudioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        private AudioRecord phoneMIC;
        private AudioTrack phoneSPK;
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
            try
            {
                // startPhone();
                handler.post(connecttoserversuccess2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Thread() {
                public void run() {
                    try {
                        DatagramSocket datasock4= new DatagramSocket(10006);
                        //收Server訊息
                        datasock4.receive(Datapack2);
                        String[] msg = new String(Datapack2.getData(), Datapack2.getOffset(),
                                Datapack2.getLength()).split(":");
                        if(msg[0].compareTo("end")==0){
                            datasock4.close();
                            type=0;
                            sh.BroadcastMember.clear();
                            datasock.close();
                            datasock2.close();
                            finish();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        AlertDialog dialog;
        AlertDialog.Builder newmemalert;
        LayoutInflater layoutInflater;
        View add;
        Runnable connecttoserversuccess2 = new Runnable() {
            @Override
            public void run() {
                layoutInflater= LayoutInflater.from(MQTTListenBroadcast.this);
                add = layoutInflater.inflate(R.layout.mqttvideocalls, null);
                newmemalert = new AlertDialog.Builder(MQTTListenBroadcast.this);//對話方塊
                newmemalert.setView(add);
                dialog = newmemalert.show();
                dialog.setCancelable(false);
                LinearLayout l = (LinearLayout)add.findViewById(R.id.Endcall);
                l.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        type=0;
                        sh.BroadcastMember.clear();
                        datasock.close();
                        datasock2.close();
                        MQTTListenBroadcast.this.finish();
                    }
                });
            }
        };

        public void startPhone() throws Exception {
            initAudioHardware();
            outThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // startPhoneMIC();
                        startPhoneSPK();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            outThread.start();
        }

        private void initAudioHardware() throws Exception
        {
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
                        }
                    }
                }
            }).start();
        }
        void calc1(short[] lin,int off,int len)
        {
            int i,j;
            for (i = 0; i < len; i++) {
                j = lin[i+off];
                lin[i+off] = (short)(j>>2);
            }
        }

        private void startPhoneSPK() throws Exception {
            byte[] gsmdata = new byte[recBufferSize];
            final int numBytesRead = 0;
            phoneSPK.play();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int packnum=0;
                    byte[] audiot = new byte[recBufferSize*3];
                    while (type==1) {
                        try {
                            if(datasock!=null){

                                datasock.receive(Datapack);
                            }
                            packnum++;
                            if(packnum==1){
                                for(int i=0;i<1024;i++)
                                    audiot[i] = Datapack.getData()[i];
                            }else if(packnum==2){
                                for(int i=1024;i<2048;i++)
                                    audiot[i] = Datapack.getData()[i-1024];
                            }else{
                                for(int i=2048;i<3072;i++)
                                    audiot[i] = Datapack.getData()[i-2048];
                                phoneSPK.write(audiot, 0, audiot.length);
                                packnum=0;
                            }
                            int v = 0;
                            for (int i = 0; i < Datapack.getData().length; i++) {
                                v += Datapack.getData()[i] * Datapack.getData()[i];
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            datasock = null;
                        }
                    }
                }
            }).start();
        }

        public void stopPhone() throws Exception {
            phoneMIC.stop();
            phoneSPK.stop();
            if(datasock!=null)
                datasock.close();
        }
    }
}

