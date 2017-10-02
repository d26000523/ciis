package com.example.minxuan.socialprojectv2.Video;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * Created by Rmo on 2017/9/28.
 */
public class Decoder {

    private int PACKET_SIZE = 1300;/**封包大小**/

    Context mContext;
    DatagramSocket data_sk = null;
    private MediaCodec decoder;
    private MediaFormat format;
    private Surface surface;
    private boolean decoderConfigured = false;
    int port;
    private volatile boolean running = true;

    /**建構元**/
    public Decoder(Surface surface1, int port, Context context,int PACKET_SIZE)
    {
        this.PACKET_SIZE = PACKET_SIZE;
        surface = surface1;
        mContext = context;
        this.port = port;
    }
    public void startDecoding()
    {
        running = true;
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                receiveFromUDP();
            }
        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }
    /**停止解碼**/
    public void stopDecoding()
    {
        running = false;
    }
    /**開始接收封包**/
    private void receiveFromUDP()
    {
        int server_port = this.port;
        /**Buffer大小**/
        //demand: 1300
        byte[] message = new byte[PACKET_SIZE];
        DatagramPacket p = new DatagramPacket(message, message.length);

        boolean exception=false;
        try
        {
            data_sk = new DatagramSocket(server_port);
            /**封包接收間隔時間**/
            data_sk.setSoTimeout(50);
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        /**當解碼器開始運作以及Socket有開起來**/
        while (running && data_sk!= null)
        {
            /**嘗試接收封包**/
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while(true){
                try {
                    Log.d("data_sk:",String.valueOf(data_sk.getSendBufferSize()));
                    data_sk.receive(p);

                    Log.d("packetsize:",String.valueOf(p.getLength()));
                    Log.d("packet0:",String.valueOf(message[0]));
                    Log.d("packet1:",String.valueOf(message[1]));
                    byte[] tmp = new byte[p.getLength()-2];
                    System.arraycopy(message,2,tmp,0,tmp.length);
                    outputStream.write(tmp);
                    //收到封包的tail就結束並輸出
                    if (message[1] == 1)
                        break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            byte[] res = outputStream.toByteArray();
            outputStream.reset();
            Log.d("length",String.valueOf(res.length));
            decoder_check(res, res.length);

        }
        /**如果Socket沒有開起來**/
        if (data_sk != null)
        {
            data_sk.close();
        }
        /**將解碼器資源釋放乾淨**/
        if(decoder != null){
            decoder.flush();
            decoder.stop();
            decoder.release();
        }
    }
    /**檢查編碼器**/
    private void decoder_check(byte[] n, int len)
    {
        if(decoderConfigured==true)
        {
            offerDecoder(n, len);
        }
        else
        {
            configureDecoder(getCsd0(), getCsd1());
        }
    }
    /**SPS和PPS**/
    public static ByteBuffer getCsd0(){
        byte[] csd0={0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0, (byte) 137, (byte) 249, 13, (byte) 224, 32, 32, 32, 64};
        //0, 0, 0, 1, 103, 100, 0, 40, -84, 52, -59, 1, -32, 17, 31, 120, 11, 80, 16, 16, 31, 0, 0, 3, 3, -23, 0, 0, -22, 96, -108
        //0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0, (byte) 137, (byte) 249, 13, (byte) 224, 32, 32, 32, 64
        //0, 0, 0, 1, 103, 100, 64, 41, -84, 44, -88, 5, 0, 91, -112
        //0, 0, 0, 1, 103, 100, 64, 41, -84, 44, -88, 10, 2, -1, -107
        ByteBuffer BBcsd0 = ByteBuffer.wrap(csd0);
        return BBcsd0;
    }
    public static ByteBuffer getCsd1(){
        byte[] csd1={0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6, (byte) 229, 1, (byte) 151, (byte) 128};
        //0, 0, 0, 1, 104, -18, 60, -128
        //0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6, (byte) 229, 1, (byte) 151, (byte) 128
        //0, 0, 0, 1, 104, -18, 56, -128
        //0, 0, 0, 1, 104, -18, 56, -128
        ByteBuffer BBcsd1 = ByteBuffer.wrap(csd1);
        return BBcsd1;
    }
    /**解碼器配置**/
    private void configureDecoder(ByteBuffer csd0,ByteBuffer csd1)
    {
        try
        {
            /**通用解碼器**/
            decoder = MediaCodec.createByCodecName("OMX.google.h264.decoder");
            /**效率較好的解碼器**/
            //decoder = MediaCodec.createDecoderByType("video/avc");
        }
        catch (Exception e)
        {
            System.out.println("解碼器創建失敗!");
            running = false;
            return;
        }
        format = MediaFormat.createVideoFormat("video/avc", 240,320);
        format.setByteBuffer("csd-0",csd0);
        format.setByteBuffer("csd-1", csd1);
        try
        {
            decoder.configure(format, surface, null, 0);
            if (decoder == null)
            {
                System.out.println("解碼器配置錯誤!");
                running = false;
                return;
            }
        }
        catch (Exception e) {}
        decoder.start();
        decoderConfigured = true;
    }
    /**解碼H.264格式封包**/
    private void offerDecoder(byte[] input, int length) {
        try
        {
            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            int inputBufferIndex = decoder.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                try
                {
                    inputBuffer.put(input, 0, length);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                decoder.queueInputBuffer(inputBufferIndex, 0, length, 0, 0);
            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            int outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0)
            {
                decoder.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
}
