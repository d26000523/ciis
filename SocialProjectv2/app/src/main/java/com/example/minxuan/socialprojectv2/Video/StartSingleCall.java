package com.example.minxuan.socialprojectv2.Video;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.example.minxuan.socialprojectv2.AccountHandler;
import com.example.minxuan.socialprojectv2.HomeActivity;
import com.example.minxuan.socialprojectv2.MenuPage.Menupage;
import com.example.minxuan.socialprojectv2.Message;
import com.example.minxuan.socialprojectv2.NetworkClient;
import com.example.minxuan.socialprojectv2.NetworkClientHandler;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.Voice.CallVoice;
import com.example.minxuan.socialprojectv2.Voice.Startvoicecall;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class StartSingleCall extends Activity implements SurfaceHolder.Callback,TextureView.SurfaceTextureListener, Camera.PreviewCallback {

    /**********************傳送端變數**********************/
    private final static String TAG = HomeActivity.class.getSimpleName();
    private final static String SP_CAM_WIDTH = "cam_width";/**鏡頭寬度(surface寬度)**/
    private final static String SP_CAM_HEIGHT = "cam_height";/**鏡頭長度(surface長度)**/
    private final static String SP_DEST_IP = "dest_ip";/**目標IP**/
    private final static String SP_DEST_PORT = "dest_port";/**目標Port**/
    private final static int DEFAULT_FRAME_RATE = 15;/**偵率，越高越流暢(原本設定15)**/
    private final static int DEFAULT_BIT_RATE = 500000;/**碼率，這個值越高，影像越清晰，但流量越大**/
    private int PACKET_SIZE = 1300;/**封包大小**/
    /**鏡頭方向0後 1前**/
    static int cameraPosition = 1;
    /**調用相機**/
    Camera camera;
    SurfaceHolder previewHolder;
    byte[] previewBuffer;
    boolean isStreaming = false;
    Encoder encoder;
    DatagramSocket udpSocket;
    InetAddress address;
    int port;
    ArrayList<byte[]> encDataList = new ArrayList<byte[]>();
    ArrayList<Integer> encDataLengthList = new ArrayList<Integer>();
    Context context;
    /********************接收端變數**********************/
    private Decoder mDecoder = null;
    private TextureView mTextureView;

    ///test global value for reduce memory size
    private byte[] encData;
    private int splitIndex = 0;
    private boolean tail = false;
    private int length = 0;
    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    String mode = "normal";
    private CallVoice callvoice;
    private String targetPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_single_call);

        /*********************************傳送端執行****************************************/
        SharedPreferences sp = StartSingleCall.this.getPreferences(Context.MODE_PRIVATE);
        sp.edit().putInt(SP_CAM_WIDTH, 320).apply();
        sp.edit().putInt(SP_CAM_HEIGHT,240).apply();
        /**調用預覽介面，預覽我們要傳送的畫面**/
        SurfaceView local = (SurfaceView) this.findViewById(R.id.local);
        this.previewHolder = local.getHolder();
        this.previewHolder.addCallback(this);

        /*******************************************接收影像顯示用的Tectureview**************************************/

        mTextureView = (TextureView) this.findViewById(R.id.remote);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setRotation(270.0f);

//        while (NetworkClientHandler.StreamingTarget ==null){}
        startStream(NetworkClientHandler.StreamingTarget, 10002);



        final Bundle bundle = this.getIntent().getExtras();
        NetworkClientHandler.networkClient.setActivity(this);

        if(bundle.getString("MODE").equals("REQUEST")){
            targetPhone = bundle.getString("key");

//            /** 整理要求視訊訊息*/
//            Gson gson = new Gson();
//            Message message = new Message();
//            message.setTAG("VIDEO_SINGLE");
//            message.setSender(AccountHandler.phoneNumber);
//            message.setReceiver(targetPhone);
//            message.setIP(AccountHandler.IP);
//            message.setMessage("REQUEST");
//            String gsonStr = gson.toJson(message);
//
//            /** 送出訊息*/
//            NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);


            //while (NetworkClientHandler.StreamingTarget ==null){}
            ok();
        }else if(bundle.get("MODE").toString().compareTo("ACCEPT")==0){
            targetPhone = bundle.getString("TARGET");
            check();
        }

    }
    public void check()
    {
        /**讓使用者決定要不要接**/
        new AlertDialog.Builder(StartSingleCall.this)//對話方塊
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("You get a video call !")
                .setMessage("Answer It ?")
                .setCancelable(false)
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendCancelMsg(targetPhone);
                        finish();
                    }
                })
                .setNegativeButton("Answer",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /** 整理接受視訊訊息*/
                        Gson gson = new Gson();
                        Message message = new Message();
                        message.setTAG("VIDEO_SINGLE");
                        message.setSender(AccountHandler.phoneNumber);
                        message.setReceiver(targetPhone);
                        message.setIP(AccountHandler.IP);
                        message.setMessage("ACCEPT");
                        String gsonStr = gson.toJson(message);

                        /** 送出訊息*/
                        NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);
                        ok();
                    }
                })
                .show();
    }

    private void sendCancelMsg(String targetPhone) {
        /** 整理拒絕視訊訊息*/
        Gson gson = new Gson();
        Message message = new Message();
        message.setTAG("VIDEO_SINGLE");
        message.setSender(AccountHandler.phoneNumber);
        message.setReceiver(targetPhone);
        message.setIP(AccountHandler.IP);
        message.setMessage("CANCEL");
        String gsonStr = gson.toJson(message);

        /** 送出訊息*/
        NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);
    }

    public void ok()
    {
        /**開始串流需要的資訊是:對方的IP以及雙方共同的port**/
        //對方接聽後開始出現聲音
//        Log.e("TARGET", NetworkClientHandler.StreamingTarget);
        /**使用CALLVOICE**/
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (NetworkClientHandler.StreamingTarget ==null){}
                    callvoice = new CallVoice(StartSingleCall.this, new InetSocketAddress(NetworkClientHandler.StreamingTarget, 10003));
                    callvoice.startPhone();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();



        /**是否結束的按鈕監聽**/
        this.findViewById(R.id.exit).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(StartSingleCall.this)//對話方塊
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle("Leave Video Call")
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
                                        mDecoder.stopDecoding();
                                        try{
                                            if(callvoice!= null)
                                                callvoice.stopPhone();
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        sendCancelMsg(targetPhone);
                                        //清空StreamingTarget
                                        NetworkClientHandler.StreamingTarget = null;
                                        finish();
                                    }
                                })
                                .show();
                    }
                });

    }
    //收到對方掛斷時停止繼續傳送影像及聲音
    public void voiceCancel(){
        mDecoder.stopDecoding();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if(callvoice!= null)
                        callvoice.stopPhone();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(StartSingleCall.this,"Finished calling.",Toast.LENGTH_SHORT).show();

                            //清空StreamingTarget
                            NetworkClientHandler.StreamingTarget = null;

                            finish();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Surface mDecoderSurface=new Surface(surface);

        mDecoder=new Decoder(mDecoderSurface,10002,this,1300);
        //while (NetworkClientHandler.StreamingTarget ==null){}
        mDecoder.startDecoding();
        //mDecoderSurface.release();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if(mDecoder != null){
            mDecoder.stopDecoding();
        }
        return false;
    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
    @Override
    protected void onResume(){
        super.onResume();
        System.out.println("0nResume");
    }

    /*********************************************************************************************/
    /*********************************************************************************************/
    /*********************************************************************************************/
    /*********************************************************************************************/
    /*********************************************************************************************/
    /*********************************************************************************************/

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
    private void startStream(String ip, int port) {
        SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
        /**********創建編碼器**********/
        this.encoder = new Encoder();
        this.encoder.init(320, 240, DEFAULT_FRAME_RATE, DEFAULT_BIT_RATE);

        /**UDP嘗試送封包**/
        try {
            this.udpSocket = new DatagramSocket();
            this.address = InetAddress.getByName(ip);
            this.port = port;
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        //sp.edit().putString(SP_DEST_IP, ip).apply();
        //sp.edit().putInt(SP_DEST_PORT, port).apply();

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
            //設置封包編號與結尾判斷
            splitIndex = 0;
            tail = false;
            //將壓縮大小依序切割放入封包中，前兩碼為編號及是否結尾
//            Log.i("i","split start");
            while(encData.length> (PACKET_SIZE-2)*splitIndex){
                if(encData.length-(PACKET_SIZE-2)*(splitIndex+1)>0) {
                    length = PACKET_SIZE-2;
                    tail = false;
                }
                else{
                    length = encData.length - (PACKET_SIZE-2)*(splitIndex);
                    tail = true;
                }
                byte[] splitData = new byte[length];
                //將encData分段複製到暫存splitData中
                System.arraycopy(encData,(PACKET_SIZE-2)*splitIndex,splitData,0,length);
                try {
                    //header 依序為 index, tail, splitData
                    outputStream.write(splitIndex);
                    if(tail) {
                        outputStream.write(1);
                    }
                    else {
                        outputStream.write(0);
                    }
                    outputStream.write(splitData);

                    byte[] res = outputStream.toByteArray();

                    synchronized (this.encDataList) {
                        this.encDataList.add(res);
                        //outputStream值清空
                        outputStream.reset();
                    }
                    splitIndex++;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
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
                    //System.out.println("address:"  + InetAddress.getByName(NetworkClientHandler.StreamingTarget));

                    DatagramPacket packet = new DatagramPacket(encData, encData.length, InetAddress.getByName(NetworkClientHandler.StreamingTarget), port);
                    udpSocket.send(packet);
                } catch (IOException e) {
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

