package com.example.minxuan.socialprojectv2;

import com.google.gson.annotations.SerializedName;

/**
 * Created by MinXuan on 2017/5/24.
 */

public class Message {

    @SerializedName("TAG")
    private String TAG;

    @SerializedName("account")
    private String account;

    @SerializedName("password")
    private String password;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("phonenumber")
    private String phonenumber;

    @SerializedName("email")
    private String email;

    @SerializedName("confirmpassword")
    private String confirmpassword;

    @SerializedName("sender")
    private String sender;

    @SerializedName("receiver")
    private String receiver;

    @SerializedName("message")
    private String message;

    @SerializedName("videoLive")
    private String videoLive;

    @SerializedName("videoList")
    private String videoList;

    @SerializedName("videoListen")
    private String videoListen;

    @SerializedName("voiceLive")
    private String voiceLive;

    @SerializedName("voiceList")
    private String voiceList;

    @SerializedName("voiceListen")
    private String voiceListen;

    @SerializedName("ip")
    private String ip;

    public void setTAG(String tag){
        this.TAG = tag;
    }
    public void setAccount(String account){
        this.account = account;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public void setLastName(String lastName){
        this.lastName = lastName;
    }
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }
    public void setPhonenumber(String phonenumber){
        this.phonenumber = phonenumber;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public void setConfirmpassword(String confirmpassword){
        this.confirmpassword = confirmpassword;
    }
    public void setSender(String sender){
        this.sender = sender;
    }
    public void setReceiver(String receiver){
        this.receiver = receiver;
    }
    public void setMessage(String message){
        this.message = message;
    }
    public void setVideoLive(String videoLive){
        this.videoLive = videoLive;
    }
    public void setVideoList(String videoList){
        this.videoList = videoList;
    }
    public void setVideoListen(String videoListen){
        this.videoListen = videoListen;
    }
    public void setVoiceLive(String voiceLive){
        this.voiceLive = voiceLive;
    }
    public void setVoiceList(String voiceList){
        this.voiceList = voiceList;
    }
    public void setVoiceListen(String voiceListen){
        this.voiceListen = voiceListen;
    }
    public void setIP(String ip){
        this.ip = ip;
    }

    public String getTAG(){
        return this.TAG;
    }
    public String getAccount(){
        return this.account;
    }
    public String getPassword(){
        return this.password;
    }
    public String getLastName(){
        return this.lastName;
    }
    public String getFirstName(){
        return this.firstName;
    }
    public String getPhonenumber(){
        return this.phonenumber;
    }
    public String getEmail(){
        return this.email;
    }
    public String getConfirmpassword(){
        return this.confirmpassword;
    }
    public String getSender(){
        return this.sender;
    }
    public String getReceiver(){
        return this.receiver;
    }
    public String getMessage(){
        return this.message;
    }
    public String getVideoLive(){
        return this.videoLive;
    }
    public String getIp(){
        return this.ip;
    }
}
