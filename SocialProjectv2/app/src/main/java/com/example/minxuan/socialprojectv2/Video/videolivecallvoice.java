package com.example.minxuan.socialprojectv2.Video;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import com.example.minxuan.socialprojectv2.NetworkClientHandler;
import com.example.minxuan.socialprojectv2.UdpClientvoice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;



/**
 * Created by RMO on 2017/7/4.
 */

public class videolivecallvoice {
    private byte[] buffer = new byte[1024];
    Activity activity;
    private DatagramPacket Datapack = new DatagramPacket(buffer, buffer.length);

    private final static int Sample_Rate = 8000;
    private final static int Channel_In_Configuration = AudioFormat.CHANNEL_IN_MONO;
    private final static int Channel_Out_Configuration = AudioFormat.CHANNEL_OUT_MONO;
    private final static int AudioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord phoneMIC;
    private AudioTrack phoneSPK;
    private int recBufferSize;
    private int playBufferSize;

    private Thread outThread, outThread2;
    private UdpClientvoice socket;
    public videolivecallvoice(Context context) {
        socket = new UdpClientvoice(context);
        activity = (Activity)context;
    }
    public void startPhone() throws Exception {
        initAudioHardware();
        outThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startPhoneMIC();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        outThread.start();
    }
    public void startPhone2() throws Exception {
        initAudioHardware();
        outThread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startPhoneSPK();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        outThread2.start();
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                while ((!Thread.interrupted()) && NetworkClientHandler.isStreaming) {
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
                    if(decibel > 10) {
                        try {
                            pack = new DatagramPacket(compressedVoice2, compressedVoice2.length,socket.getServerAddress());
                            if(UdpClientvoice.ds!=null)
                                UdpClientvoice.ds.send(pack);
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

    private void startPhoneSPK() throws Exception {
        phoneSPK.play();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int packnum=0;
                byte[] audiot = new byte[recBufferSize*3];
                while (NetworkClientHandler.isStreaming) {
                    try {
                        if(UdpClientvoice.ds!=null)
                            UdpClientvoice.ds.receive(Datapack);
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
                            v += Datapack.getData()[i] * Datapack.getData()[i];
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void stopPhone() throws Exception {
        phoneMIC.stop();
        socket.stop();

    }
    public void stopPhone2() throws Exception {
        phoneSPK.stop();
        socket.stop();

    }
}



