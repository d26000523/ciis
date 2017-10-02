package com.example.minxuan.socialprojectv2.Video;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.example.minxuan.socialprojectv2.MenuPage.Menupage;
import com.example.minxuan.socialprojectv2.Message;
import com.example.minxuan.socialprojectv2.NetworkClientHandler;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.UdpClient;
import com.example.minxuan.socialprojectv2.UdpClientvoice;
import com.google.gson.Gson;

public class videolive_receiver extends AppCompatActivity implements TextureView.SurfaceTextureListener{
    private videoliveDecoder mDecoder = null;
    private TextureView mTextureView;
    private UdpClient socket;
    private UdpClientvoice socketvoice;
    Bundle Sender;
    String SenderS;
    final videolivecallvoice groupvoice = new videolivecallvoice(this);

    /** 是否等待server回應*/
    boolean isWaiting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videolive_receiver);
        socket = new UdpClient(this);
        socketvoice = new UdpClientvoice(this);
        mTextureView = (TextureView) this.findViewById(R.id.remote);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setRotation(270.0f);
        /**獲得使用者選擇**/
        Sender = this.getIntent().getExtras();
        SenderS = Sender.getString("key");
        /**判斷是否連線成功的變數**/
        NetworkClientHandler.isStreaming=false;
        isWaiting = true;
        /**送出連線封包**/
        register();

        NetworkClientHandler.networkClient.setActivity(this);
        while(isWaiting){
            /**當建立成功 isStreaming為true，開始向串流server傳封包**/
            if(NetworkClientHandler.isStreaming) {
                init();
                isWaiting = false;
                break;
            }
        }
        this.findViewById(R.id.exit).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(videolive_receiver.this)//對話方塊
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
                                            groupvoice.stopPhone2();
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

                                        /** 跳轉頁面*/
                                        Intent i = new Intent();
                                        i.setClass(videolive_receiver.this, Menupage.class);
                                        finish();
                                        startActivity(i);
                                    }
                                })
                                .show();
                    }
                });
    }
    public void register()
    {
        Gson gson = new Gson();
        Message message = new Message();
        message.setTAG("LISTEN_VIDEO_LIVE");
        message.setVideoListen(SenderS);/**向第一個Server註冊我選擇的頻道**/
        String gsonStr = gson.toJson(message);
        NetworkClientHandler.networkClient.webSocketClient.send(gsonStr);
    }
    public void init() {
        try
        {
            socket.start("watch",SenderS);
            socketvoice.start("watch",SenderS);
            groupvoice.startPhone2();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        Surface mDecoderSurface=new Surface(surface);
        mDecoder=new videoliveDecoder(mDecoderSurface,10002,this);
        mDecoder.startDecoding();
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
    }
}
