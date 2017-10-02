package com.example.minxuan.socialprojectv2.MQTT.service;

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

import com.example.minxuan.socialprojectv2.MQTT.ActionListener;
import com.example.minxuan.socialprojectv2.MQTT.Connections;
import com.example.minxuan.socialprojectv2.MQTT.WaveHelper;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SharedSocket;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
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
        id = getid[3];
        sendaccept();

       /* WaveView waveView = (WaveView) findViewById(R.id.wave);
        waveView.setBorder(10, Color.parseColor("#E9E9E9"),"#00B6DE");

        final WaveHelper mWaveHelper = new WaveHelper(waveView);


        waveView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWaveHelper.start();
            }
        }, 1000);*/

        try {
            SocketUDPClient socketUDPClient = new SocketUDPClient();
            // socketUDPClient.mWaveHelper = mWaveHelper;
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

        String topic = "MQTT_SMS_$("+id+")";


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
        private boolean isReceive = false;
        private Context context;
        private String AnotherClientAddr;
        private int AnotherClientPort;
        private DatagramSocket datasock,datasock2;
        private DatagramPacket Datapack = new DatagramPacket(buffer, buffer.length);
        private DatagramPacket Datapack2 = new DatagramPacket(buffer, buffer.length);
        private SocketAddress ClientAddress,ClientAddress2;
        private Thread ServerReturn, tester;


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
        double recievedecibel;

        public WaveHelper mWaveHelper;
        public WaveHelper mWaveHelper2;

        public SocketUDPClient() throws UnknownHostException {
            //初始化(和Server連接)
            try {
                datasock = new DatagramSocket(10004);
                datasock2= new DatagramSocket(10001);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            try {

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

                        Log.d("connectServer", "recieveEnd");
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
        public void startvideo(){

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
                l.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();

                        type=0;
                        sh.BroadcastMember.clear();

                        datasock.close();
                        datasock2.close();
                        finish();
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
                        //			Thread.sleep(3000);
                        Log.d("startPhoneMIC", "  ");
                        // startPhoneMIC();
                        startPhoneSPK();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            outThread.start();
            /*    inThread = new Thread(this);
                inThread.start();*/

        }

        private void initAudioHardware() throws Exception {
            // recBufferSize = AudioRecord.getMinBufferSize(Sample_Rate,
            // Channel_In_Configuration, AudioEncoding);
            // playBufferSize = AudioTrack.getMinBufferSize(Sample_Rate,
            // Channel_Out_Configuration, AudioEncoding);
            //recBufferSize =  AudioRecord.getMinBufferSize(Sample_Rate,Channel_In_Configuration,AudioEncoding);// 4k bytes
            //playBufferSize =   AudioRecord.getMinBufferSize(Sample_Rate,Channel_In_Configuration,AudioEncoding);// 4k bytes
            recBufferSize = 1024;
            playBufferSize = android.media.AudioTrack.getMinBufferSize(8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            phoneMIC = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, Sample_Rate,
                    Channel_In_Configuration, AudioEncoding, recBufferSize);
            phoneSPK = new AudioTrack(AudioManager.STREAM_MUSIC, Sample_Rate,
                    Channel_Out_Configuration, AudioEncoding, playBufferSize,
                    AudioTrack.MODE_STREAM);
            phoneSPK.setStereoVolume(1f, 1f);

        }


         /*   @Override
            public void run() {
                startPhoneSPK();
            }*/

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

                        //Log.i("NetPhone","mic read "+b);
                            /*for(int i=0;i<recBufferSize/2;i++)
                            {
                                //tmp=(compressedVoice[i]+32768);
                                //b1= tmp % 256;
                                //b2= tmp / 256;
                                compressedVoice2[i*2  ]=(byte) ((compressedVoice[i]+32768)%256-128);
                                compressedVoice2[i*2+1]=(byte) ((compressedVoice[i]+32768)/256-128);
                            }*/
                        for(int i=0,j=0;i<recBufferSize;i=i+2,j++){
                            // (byte) ((s & 0xFF00) >> 8), (byte) (s & 0x00FF)
                            compressedVoice2[i]= (byte)(compressedVoice[j] & 0x00FF);
                            compressedVoice2[i+1] = (byte)((compressedVoice[j] & 0xFF00) >> 8);
                        }
                        int v = 0;
                        for (int i = 0; i < compressedVoice.length; i++) {
                            // 这里没有做运算的优化，为了更加清晰的展示代码
                            v += compressedVoice[i] * compressedVoice[i];
                        }

                        //     DatagramPacket pack = new DatagramPacket("Test".getBytes(),"Test".getBytes().length, pointAddress);
                        double decibel = 10*Math.log10(v/(double)b);


                        //Log.d("decibel",decibel+"");
                        //     DatagramPacket pack = new DatagramPacket("Test".getBytes(),"Test".getBytes().length, pointAddress);
                        DatagramPacket pack = null;
                        if(decibel > 35) {
                            /*try {
                                pack = new DatagramPacket(compressedVoice2, compressedVoice2.length, ClientAddress);
                                datasock.send(pack);
                            } catch (SocketException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/
                        }


                        // Log.d("startPhoneMIC", "SendRecording");
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

        private void startPhoneSPK() throws Exception {
            byte[] gsmdata = new byte[recBufferSize];
            final int numBytesRead = 0;
            phoneSPK.play();
            //startanimation(1);
            final int[] ss = {0};
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ss[0]++;
                    int packnum=0;
                    byte[] audiot = new byte[recBufferSize*3];
                    while (type==1) {


                        try {
                            datasock.receive(Datapack);
                            Log.d("recieve","dd"+ ss[0]);
                            packnum++;
                            if(packnum==1){
                                for(int i=0;i<1024;i++)
                                    audiot[i] = Datapack.getData()[i];
                            }
                            else if(packnum==2){
                                for(int i=1024;i<2048;i++)
                                    audiot[i] = Datapack.getData()[i-1024];

                            }
                            else{
                                for(int i=2048;i<3072;i++)
                                    audiot[i] = Datapack.getData()[i-2048];
                                phoneSPK.write(audiot, 0, audiot.length);
                                packnum=0;

                            }
                            int v = 0;
                            for (int i = 0; i < Datapack.getData().length; i++) {
                                // 这里没有做运算的优化，为了更加清晰的展示代码
                                v += Datapack.getData()[i] * Datapack.getData()[i];
                            }
                            //  recievedecibel = 10*Math.log10(v/(double)1024);
                            //  Log.d("startPhoneSPK", "RecieveDataPack");

                            // numBytesRead = curCallLink.getInputStream().read(gsmdata);
                            //  Log.i("NetPhone","recv data "+numBytesRead);

                            // phoneSPK.write(Datapack.getData(), 0, Datapack.getLength());

                            //     Log.d("startPhoneMIC", "RecieveRecording");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }).start();



        }
        public void startanimation(final int color) throws Exception {

            Log.d("refresh", "initAudioHardware");
            if(color ==1) {
                Thread thread2 = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            while(type==1) {
                                //Log.d("refresh", decibel + "");
                                handler.post(new Runnable() {
                                    public void run()
                                    {
                                        mWaveHelper.drawanother((float) recievedecibel);
                                        recievedecibel=0;
                                    }
                                });

                                Thread.sleep(500);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread2.start();
            }

        }

            /*public void stopPhone() throws Exception {
                stoped = true;
                while (inThread.isAlive() || outThread.isAlive()) {
                    Thread.sleep(100);
                }
                phoneMIC.stop();
                phoneSPK.stop();
                curCallLink.close();
            }*/

    }

}

