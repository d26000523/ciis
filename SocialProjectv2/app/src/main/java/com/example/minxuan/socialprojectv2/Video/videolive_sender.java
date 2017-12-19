package com.example.minxuan.socialprojectv2.Video;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.minxuan.socialprojectv2.AccountHandler;
import com.example.minxuan.socialprojectv2.HomeActivity;
import com.example.minxuan.socialprojectv2.MenuPage.Menupage;
import com.example.minxuan.socialprojectv2.Message;
import com.example.minxuan.socialprojectv2.NetworkClientHandler;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.UdpClient;
import com.example.minxuan.socialprojectv2.UdpClientvoice;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class videolive_sender extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    /**********************傳送端變數**********************/
    private final static String TAG = HomeActivity.class.getSimpleName();
    private final static String SP_CAM_WIDTH = "cam_width";/**鏡頭寬度(surface寬度)**/
    private final static String SP_CAM_HEIGHT = "cam_height";/**鏡頭長度(surface長度)**/
    private final static int DEFAULT_FRAME_RATE = 15;/**偵率，越高越流暢(原本設定15)**/
    private final static int DEFAULT_BIT_RATE = 900000;/**碼率，這個值越高，影像越清晰，但流量越大**/
    private int PACKET_SIZE = 9600;/**封包大小**/
    private UdpClient socket;
    private UdpClientvoice socketvoice;

    /**鏡頭方向0後 1前**/
    static int cameraPosition = 1;
    /**調用相機**/
    Camera camera;
    SurfaceHolder previewHolder;
    byte[] previewBuffer;
    boolean isStreaming = false;
    Encoder encoder;
    ArrayList<byte[]> encDataList = new ArrayList<>();
    ArrayList<Integer> encDataLengthList = new ArrayList<>();
    final videolivecallvoice groupvoice = new videolivecallvoice(this);

    /** 是否等待server回應*/
    boolean isWaiting;

    ///test global value
    private byte[] encData;
    private int splitIndex = 0;
    private boolean tail = false;
    private int length = 0;
    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videolive_sender);
        NetworkClientHandler.isStreaming = false;
        isWaiting = true;
        socket = new UdpClient(this);
        socketvoice = new UdpClientvoice(this);
        register();
        /**調用預覽介面，預覽我們要傳送的畫面**/
        SurfaceView local = (SurfaceView) this.findViewById(R.id.local);
        SharedPreferences sp = videolive_sender.this.getPreferences(Context.MODE_PRIVATE);
        sp.edit().putInt(SP_CAM_WIDTH, 320).apply();
        sp.edit().putInt(SP_CAM_HEIGHT,240).apply();
        this.previewHolder = local.getHolder();
        this.previewHolder.addCallback(this);
        NetworkClientHandler.networkClient.setActivity(this);

        while(isWaiting){
            /**當建立成功 isStreaming為true，開始向串流server傳封包**/
            if(NetworkClientHandler.isStreaming) {
                start();
                isWaiting = false;
                break;
            }
        }
        this.findViewById(R.id.exit).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(videolive_sender.this)//對話方塊
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle("Exit Live-Stream")
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
                                        try{
                                            groupvoice.stopPhone();
                                            socket.stop();
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        dialog.dismiss();

                                        /**傳送聊天室關閉的請求**/
                                        Gson gson = new Gson();
                                        Message message = new Message();
                                        message.setTAG("VIDEO_LIVE");
                                        message.setVideoLive("close");
                                        String gsonStr = gson.toJson(message);
                                        NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);


                                        finish();

                                    }
                                })
                                .show();
                    }
                });
    }
    public void register(){
        /** 整理訊息*/
        Gson gson = new Gson();
        Message message = new Message();
        message.setTAG("VIDEO_LIVE");
        message.setVideoLive("open");
        String gsonStr = gson.toJson(message);
        NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);
    }
    /**當確認在第一個server註冊完成**/
    public void start(){

        /**使用CALLVOICE**/
        try{
            groupvoice.startPhone();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        startStream();
    }
    /**當視窗一建立，馬上調用相機(傳輸用)**/
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startCamera();
    }
    /**設定相機永遠維持在正常的方向**/
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        camera.setDisplayOrientation(90);
    }

    /**當應用結束(整個畫面結束)，釋放相機資源並停止解碼**/
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopCamera();
    }
    /**開始串流(傳輸影像)**/
    private void startStream() {

        /**********創建編碼器**********/
        this.encoder = new Encoder();
        this.encoder.init(320, 240, DEFAULT_FRAME_RATE, DEFAULT_BIT_RATE);

        /**UDP嘗試送封包**/
        if (!isStreaming) {
            try {
                socket.start("open", AccountHandler.phoneNumber);
                socketvoice.start("open", AccountHandler.phoneNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.isStreaming = true;
        Thread thrd = new Thread(senderRun);
        thrd.start();
    }
    /**當有新視訊資料要送**/
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        this.camera.addCallbackBuffer(this.previewBuffer);

        if (this.isStreaming) {
            if (this.encDataLengthList.size() > 100)
            {
                return;
            }
            //data經過h.264壓縮成encData分兩組frame(i,p)
            encData = this.encoder.data_change(data);
            System.out.println("encData.length" + encData.length);
            //設置封包編號與結尾判斷
            synchronized (this.encDataList){
                this.encDataList.add(encData);
            }

            splitIndex = 0;
            tail = false;
            //將壓縮大小依序切割放入封包中，前兩碼為編號及是否結尾
//            Log.i("i","split start");
//            while(encData.length> (PACKET_SIZE-2)*splitIndex){
//                if(encData.length-(PACKET_SIZE-2)*(splitIndex+1)>0) {
//                    length = PACKET_SIZE-2;
//                    tail = false;
//                }
//                else{
//                    length = encData.length - (PACKET_SIZE-2)*(splitIndex);
//                    tail = true;
//                }
//                byte[] splitData = new byte[length];
//                //將encData分段複製到暫存splitData中
//                System.arraycopy(encData,(PACKET_SIZE-2)*splitIndex,splitData,0,length);
//                try {
//                    //header 依序為 index, tail, splitData
//                    outputStream.write(splitIndex);
//                    if(tail)
//                    {
//                        outputStream.write(1);
//                    }
//                    else
//                    {
//                        outputStream.write(0);
//                    }
//                    outputStream.write(splitData);
//
//                    byte[] res = outputStream.toByteArray();
//
//                    synchronized (this.encDataList)
//                    {
//                        this.encDataList.add(res);
//                        //outputStream值清空
//                        outputStream.reset();
//
//                    }
//                    splitIndex++;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
        }
    }
    /**執行串流的封包傳送**/
    Runnable senderRun = new Runnable() {
        @Override
        public void run() {
            while (isStreaming) {

                boolean empty = false;
                byte[] encData = null;

                synchronized (encDataList) {
                    if (encDataList.size() == 0) {
                        empty = true;
                    } else
                        encData = encDataList.remove(0);
                }
                if (empty) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                try {

                    socket.doSend(socket.getServerAddress(), encData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //TODO:
        }
    };
    /**停止串流**/
    private void stopStream() {
        this.isStreaming = false;
        /*******停止編碼*******/
        if (this.encoder != null)
            this.encoder.close();
        this.encoder = null;
    }
    /**執行相機所做的初始設定**/
    private void startCamera() {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        int width = sp.getInt(SP_CAM_WIDTH, 0);
        int height = sp.getInt(SP_CAM_HEIGHT, 0);

        /***********設定預覽介面的大小***********/
        this.previewHolder.setFixedSize(width, height);

        int stride = (int) Math.ceil(width / 16.0f) * 16;
        int cStride = (int) Math.ceil(width / 32.0f) * 16;
        final int frameSize = stride * height;
        final int qFrameSize = cStride * height / 2;

        this.previewBuffer = new byte[frameSize + qFrameSize * 2];
        /**相機使用參數設定**/
        try {
            camera = Camera.open(cameraPosition);
            camera.setPreviewDisplay(this.previewHolder);
            Camera.Parameters params = camera.getParameters();
            params.setPreviewSize(width, height);
            params.setPreviewFormat(ImageFormat.YV12);
            camera.setParameters(params);
            camera.addCallbackBuffer(previewBuffer);
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
            camera.setDisplayOrientation(180);
        }

        catch (RuntimeException e) {
            //TODO:
        }

        catch (IOException e){
            //TODO:
        }
    }
    /**當相機停用後要釋放資源**/
    private void stopCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
    /********當整個activity暫停*********/
    @Override
    protected void onPause() {
        this.stopStream();
        if (encoder != null)
            encoder.close();
        super.onPause();
    }
    /********當整個activity結束*********/
    protected void onDestroy() {
        super.onDestroy();
    }

}
