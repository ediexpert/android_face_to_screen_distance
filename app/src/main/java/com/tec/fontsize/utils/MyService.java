package com.tec.fontsize.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.tec.fontsize.MainActivity;

/**
 * Created by Dell on 6/4/2016.
 */
public class MyService extends Service {
    public float protectedDistance=25.0f;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setProtectedDistance(int x){

        protectedDistance = (float) x;
        getApplicationContext().sendBroadcast(new Intent("mymessage"));

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }



    public float getMinDistanceToCheck(){
        return protectedDistance;
    }
}
