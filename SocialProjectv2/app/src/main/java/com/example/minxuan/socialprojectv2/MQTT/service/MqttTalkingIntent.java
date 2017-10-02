package com.example.minxuan.socialprojectv2.MQTT.service;

/**
 * Created by RMO on 2017/6/16.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SharedSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MqttTalkingIntent extends Activity {
    Handler handler;
    SocketUDPClient socketUDPClient;
    SharedSocket sh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        sh = (SharedSocket)getApplication();
        try {
            socketUDPClient = new SocketUDPClient(this);
            socketUDPClient.listen_request();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public class SocketUDPClient{
        private byte[] buffer = new byte[1024];
        private boolean isReceive = false;
        private Context context;
        Activity activity;
        private String AnotherClientAddr;
        private int AnotherClientPort;
        private DatagramSocket datasock2;
        private DatagramPacket Datapack = new DatagramPacket(buffer, buffer.length);
        private DatagramPacket Datapack2 = new DatagramPacket(buffer, buffer.length);
        private SocketAddress ClientAddress,ClientAddress2,ClientAddress3;
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
        SharedSocket sharedSocket;
        Thread listen3;

        public SocketUDPClient(Context context) throws UnknownHostException {
            //初始化(和Server連接)
            try {
                //datasock = new DatagramSocket(10000);
                datasock2= new DatagramSocket(10001);
                this.context = context;
                activity = (Activity)context;
                sharedSocket = (SharedSocket)activity.getApplication();


            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        public void listen_request() {


            Thread listen = new Thread() {
                public void run() {
                    try {

                        //收Server訊息
                        if ((sharedSocket.mqttreceivecall).compareTo("") != 0) {
                            AnotherClientAddr = sharedSocket.mqttreceivecall;
                            handler.post(connecttoserversuccess);
                            sharedSocket.mqttreceivecall = "";
                            Log.d("enter", "");
                        }
                        Log.d("no enter", "");


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            listen.start();

            Thread listen2 = new Thread() {
                public void run() {
                    try {

                        //收Server訊息
                        datasock2.receive(Datapack2);

                        Log.d("listen2", "recieveMessage");
                        String[] msg = new String(Datapack2.getData(), Datapack2.getOffset(),
                                Datapack2.getLength()).split(":");
                        if (msg[0].compareTo("end") == 0) {
                            fin();
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            listen2.start();


        }
        Runnable connecttoserversuccess = new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MqttTalkingIntent.this)//對話方塊
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Receive " + AnotherClientAddr + " Call")
                        .setNegativeButton("Answer", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ServerReturn = new Thread() {
                                    public void run() {
                                        try {
                                            //傳送訊息給Server
                                            InetAddress clientaddr = null;

                                            clientaddr = InetAddress.getByName(AnotherClientAddr);
                                            ClientAddress3 = new InetSocketAddress(clientaddr, 10005);
                                            ClientAddress2 = new InetSocketAddress(clientaddr, 10001);
                                            ClientAddress = new InetSocketAddress(clientaddr, 10000);
                                            DatagramPacket pack = new DatagramPacket("accept".getBytes(), "accept".getBytes().length, ClientAddress3);
                                            sh.datasock.send(pack);

                                            Log.d("makecall", "return access");
                                            startPhone();
                                            handler.post(connecttoserversuccess2);
                                            tag=2;




                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                ServerReturn.start();

                            }
                        })
                        .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ServerReturn = new Thread() {
                                    public void run() {
                                        try {
                                            //傳送訊息給Server
                                            InetAddress clientaddr = null;
                                            clientaddr = InetAddress.getByName(AnotherClientAddr);
                                            ClientAddress = new InetSocketAddress(clientaddr, 10005);
                                            DatagramPacket pack = new DatagramPacket("reject".getBytes(), "reject".getBytes().length, ClientAddress);
                                            sh.datasock.send(pack);
                                            Log.d("makecall", "return reject");
                                            listen_request();




                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                ServerReturn.start();


                            }
                        })
                        .show();
                /*TextView t = (TextView)findViewById(R.id.Status);
                t.setText("Success");
                t.append("Connect to"+"\n");
                t.append(AnotherClientAddr+"\n");
                t.append(AnotherClientPort+"\n");*/

                // receive();
            }
        };
        AlertDialog dialog;
        AlertDialog.Builder newmemalert;
        LayoutInflater layoutInflater;
        View add;
        Runnable connecttoserversuccess2 = new Runnable() {
            @Override
            public void run() {
                layoutInflater= LayoutInflater.from(MqttTalkingIntent.this);
                add = layoutInflater.inflate(R.layout.mqttvideocalls, null);
                newmemalert = new AlertDialog.Builder(MqttTalkingIntent.this);//對話方塊
                newmemalert.setView(add);
                dialog = newmemalert.show();
                dialog.setCancelable(false);
                LinearLayout l = (LinearLayout)add.findViewById(R.id.Endcall);
                l.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fin();

                    }
                });
            }
        };

        public void fin(){
            ServerReturn = new Thread() {
                public void run() {
                    try {
                        type=0;
                        DatagramPacket pack = null;
                        try {
                            pack = new DatagramPacket("end".getBytes(), "end".getBytes().length, ClientAddress2);
                            datasock2.send(pack);
                        } catch (SocketException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        phoneMIC.stop();
                        //sh.datasock.close();
                        datasock2.close();

                        MqttTalkingIntent.this.finish();




                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            ServerReturn.start();

        }

        public void startPhone() throws Exception {
            initAudioHardware();

            outThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        //			Thread.sleep(3000);
                        Log.d("startPhoneMIC","  ");
                        startPhoneMIC();
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
            playBufferSize = 1024;
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
                            try {
                                pack = new DatagramPacket(compressedVoice2, compressedVoice2.length, ClientAddress);
                                sh.datasock.send(pack);
                            } catch (SocketException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int packnum=0;
                    byte[] audiot = new byte[recBufferSize*3];
                    while (type==1) {

                        try {
                            sh.datasock.receive(Datapack);
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

