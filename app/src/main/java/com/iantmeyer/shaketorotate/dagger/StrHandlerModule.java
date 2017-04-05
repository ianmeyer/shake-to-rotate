package com.iantmeyer.shaketorotate.dagger;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

import com.iantmeyer.shaketorotate.data.AppData;
import com.iantmeyer.shaketorotate.data.SettingsManager;
import com.iantmeyer.shaketorotate.service.LandscapeListener;
import com.iantmeyer.shaketorotate.service.ServiceHelper;
import com.iantmeyer.shaketorotate.service.StrHandler;
import com.squareup.seismic.ShakeDetector;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static android.content.Context.SENSOR_SERVICE;

@Module
public class StrHandlerModule {

    private final String TAG = "StrHandlerModule";

    private final Context mAppContext;

    private final StrHandler mHandler;

    public StrHandlerModule(Context context, StrHandler handler) {
        Log.d(TAG, "Init");
        mAppContext = context.getApplicationContext();
        mHandler = handler;
    }

    @Singleton
    @Provides
    Context providesAppContext() {
        return mAppContext;
    }

    @Singleton
    @Provides
    ServiceHelper providesServiceHelper(Context context) {
        return new ServiceHelper(context);
    }

    @Singleton
    @Provides
    SettingsManager providesSettingsManager(Context context) {
        return new SettingsManager(context);
    }

    @Singleton
    @Provides
    AppData providesAppData(Context context) {
        AppData appData = new AppData(context);
        appData.init();
        return appData;
    }

    @Singleton
    @Provides
    SensorManager providesSensorManager(Context context) {
        return (SensorManager) context.getSystemService(SENSOR_SERVICE);
    }

    @Singleton
    @Provides
    ShakeDetector providesShakeDetector() {
        return new ShakeDetector(mHandler);
    }

    @Singleton
    @Provides
    LandscapeListener providesLandscapeListener(Context context) {
        return new LandscapeListener(context, mHandler);
    }
}