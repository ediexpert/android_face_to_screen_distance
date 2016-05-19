package com.tec.fontsize;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.inputmethod.InputBinding;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tec.fontsize.messages.MeasurementStepMessage;
import com.tec.fontsize.messages.MessageHUB;
import com.tec.fontsize.messages.MessageListener;

/**
 * Created by Dell on 18/05/2016.
 */
public class MyService extends Service {

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public  void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this,"Service Started", Toast.LENGTH_LONG).show();
        MainActivity mainActivity = new MainActivity();
        mainActivity.starMediaRecording();
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
//        super.onDestroy();
        Toast.makeText(this,"Service Destroyed", Toast.LENGTH_LONG).show();

    }

}
