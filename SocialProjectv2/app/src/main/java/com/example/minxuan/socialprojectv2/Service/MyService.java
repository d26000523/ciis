package com.example.minxuan.socialprojectv2.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by MinXuan on 2017/7/10.
 */

public class MyService extends Service {

    public class LocalBinder extends Binder {
        MyService getService()
        {
            return  MyService.this;
        }
    }
    private LocalBinder mLocBin = new LocalBinder();

    public void myMethod(){

    }

    @Override
    public IBinder onBind(Intent arg0){
        // TODO Auto-generated method stub
        return mLocBin;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        // TODO Auto-generated method stub
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent){
        // TODO Auto-generated method stub
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        // TODO Auto-generated method stub
    }
}
