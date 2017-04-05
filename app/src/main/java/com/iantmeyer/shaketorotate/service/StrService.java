package com.iantmeyer.shaketorotate.service;

import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;

import static com.iantmeyer.shaketorotate.service.StrHandler.MSG_INIT_SHAKE;

public class StrService extends Service {

    private volatile StrHandler mServiceHandler;

    public StrService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("StrHandler");
        thread.start();

        Looper looper = thread.getLooper();
        mServiceHandler = new StrHandler(getBaseContext(), looper);

        mServiceHandler.sendMessage(MSG_INIT_SHAKE);
    }

    @Override
    public void onDestroy() {
        mServiceHandler.close();
        mServiceHandler = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}