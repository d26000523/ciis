package com.example.minxuan.socialprojectv2.Voice;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.minxuan.socialprojectv2.NetworkClientHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;



/**
 * Created by MinXuan on 2017/6/14.
 */

public class CallVoice {

    private byte[] buffer = new byte[1024];
    private Context context;
    Activity activity;
    private DatagramPacket Datapack = new DatagramPacket(buffer, buffer.length);
    private SocketAddress ClientAddress;
    private DatagramSocket datasock;

    private final static int Sample_Rate = 8000;
    private final static int Channel_In_Configuration = AudioFormat.CHANNEL_IN_MONO;
    private final static int Channel_Out_Configuration = AudioFormat.CHANNEL_OUT_MONO;
    private final static int AudioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord phoneMIC;
    private AudioTrack phoneSPK;
    private int recBufferSize;
    private int playBufferSize;

    private Thread outThread;
    int type=1;

    public CallVoice(Context context,SocketAddress clientAddress) {
        //初始化(和Server連接)
        //datasock = new DatagramSocket(10000);
        this.ClientAddress = clientAddress;

        this.context = context;
        try {
            datasock = new DatagramSocket(10003);
            datasock.setSoTimeout(300);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        activity = (Activity)context;
    }
    public void startPhone() throws Exception {
        outThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initAudioHardware();
                    System.out.println("initAudioHardware();");

                    startPhoneMIC();
                    System.out.println("startPhoneMIC();");

                    speak();
                    System.out.println("speak();");

                    //startPhoneSPK();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        outThread.start();
    }

    private void initAudioHardware() throws Exception {
        recBufferSize = 1024;
        playBufferSize = 1024;
        //VOICE_COMMUNICATION
        phoneMIC = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, Sample_Rate,
                Channel_In_Configuration, AudioEncoding, recBufferSize);
        phoneMIC.startRecording();
        //STREAM_VOICE_CALL
        phoneSPK = new AudioTrack(AudioManager.STREAM_VOICE_CALL, Sample_Rate,
                Channel_Out_Configuration, AudioEncoding, playBufferSize,
                AudioTrack.MODE_STREAM);
        phoneSPK.setStereoVolume(1f, 1f);
        phoneSPK.play();
    }

    private void startPhoneMIC() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (type==1 && ClientAddress!= null) {
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
                    //Log.i("compressedVoice2:", String.valueOf(compressedVoice2.length));
                    //System.out.println(" phoneSPK.write(compressedVoice2, 0, compressedVoice2.length);");
                    //phoneSPK.write(compressedVoice2, 0, compressedVoice2.length);

                    if(decibel > 10) {
                        try {
                            DatagramPacket pack = new DatagramPacket(compressedVoice2, compressedVoice2.length, ClientAddress);
                            if(datasock!=null){
                                //Log.i("send","send");
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
    private void calc1(short[] lin,int off,int len) {
        int i,j;
        for (i = 0; i < len; i++) {
            j = lin[i+off];
            lin[i+off] = (short)(j>>2);
        }
    }
    private void speak(){
        System.out.println("phoneSPK" + phoneSPK.getState());
        new Thread(new Runnable() {
            @Override
            public void run() {
                int packnum = 0;
                while (type==1 && datasock != null) {
                    if(NetworkClientHandler.StreamingTarget!= null)
                    try {

                        datasock.receive(Datapack);
                        phoneSPK.write(Datapack.getData(), 0, Datapack.getData().length);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
        }).start();

    }
    private void startPhoneSPK() throws Exception {
        int packnum=0;
        byte[] audiot = new byte[recBufferSize*3];
        while (type==1) {
            try {
                if(datasock!=null)
                    datasock.receive(Datapack);
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
                    //Log.i("audiot:", String.valueOf(audiot.length));
                    phoneSPK.write(audiot, 0, audiot.length);
                    packnum=0;
                }
//                int v = 0;
//                for (int i = 0; i < Datapack.getData().length; i++) {
//                    v += Datapack.getData()[i] * Datapack.getData()[i];
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void stopPhone() throws Exception {
        Log.d("stopPhone","=============================stopPhone");
        type = 2;
        phoneMIC.stop();
        phoneSPK.stop();
        if(datasock!=null)
            datasock.close();
    }
}
