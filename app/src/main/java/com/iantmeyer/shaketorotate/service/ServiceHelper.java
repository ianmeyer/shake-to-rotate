package com.iantmeyer.shaketorotate.service;

import android.content.Context;
import android.content.Intent;

public class ServiceHelper {

    private final Context mAppContext;

    public ServiceHelper(Context context) {
        mAppContext = context.getApplicationContext();
    }

    public void startService() {
        Intent serviceIntent = new Intent(mAppContext, StrService.class);
        mAppContext.startService(serviceIntent);
    }

    public void stopService() {
        mAppContext.stopService(new Intent(mAppContext, StrService.class));
    }
}