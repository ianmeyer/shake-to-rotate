package com.iantmeyer.shaketorotate.dagger;

import android.content.Context;
import android.util.Log;

import com.iantmeyer.shaketorotate.data.AppData;
import com.iantmeyer.shaketorotate.data.SettingsManager;
import com.iantmeyer.shaketorotate.service.ServiceHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

    private final String TAG = "ActivityModule";

    private final Context mAppContext;

    public ActivityModule(Context context) {
        Log.d(TAG, "Init");
        mAppContext = context.getApplicationContext();
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
}