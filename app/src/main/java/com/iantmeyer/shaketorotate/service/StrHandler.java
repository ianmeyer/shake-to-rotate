package com.iantmeyer.shaketorotate.service;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.Surface;

import com.iantmeyer.shaketorotate.dagger.DaggerStrHandlerComponent;
import com.iantmeyer.shaketorotate.dagger.StrHandlerModule;
import com.iantmeyer.shaketorotate.data.SettingsManager;
import com.squareup.seismic.ShakeDetector;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class StrHandler extends Handler implements LandscapeListener.Listener, ShakeDetector.Listener {

    private static final String TAG = "StrHandler";

    private static int COOLDOWN_DELAY = 1000;
    private static int MINIMUM_SHAKE_INTERVAL = 2000;

    @Inject
    protected SensorManager mSensorManager;

    @Inject
    protected ShakeDetector mShakeDetector;

    @Inject
    protected SettingsManager mSettingsManager;

    @Inject
    protected LandscapeListener mLandscapeListener;

    @Inject
    protected ServiceHelper mServiceHelper;

    private final Context mContext;
    private boolean mVibrate;

    private long mNextShakeMinTime = 0;
    private Set<String> mExcludedPackages = new HashSet<>();

    private boolean mHandlingShake = false;
    private boolean mRegisteredReceiver = false;

    private int mOrientation = -1;

    static final int MSG_INIT_SHAKE = 1;
    static final int MSG_ROTATE_TO_PORTRAIT = 2;
    static final int MSG_ROTATE_TO_LANDSCAPE = 3;

    StrHandler(Context context, Looper looper) {
        super(looper);

        DaggerStrHandlerComponent.builder()
                .strHandlerModule(new StrHandlerModule(context, this))
                .build()
                .inject(this);

        mContext = context;
        mVibrate = mSettingsManager.getVibrateSetting();
        mExcludedPackages = mSettingsManager.getExcludedPackages();

        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenStateFilter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mBroadcastReceiver, screenStateFilter);
        mRegisteredReceiver = true;

        EventBus.getDefault().register(this);

        if(mOrientation == -1) {
            mOrientation = mContext.getResources().getConfiguration().orientation;
        }
    }

    boolean sendMessage(int message) {
        Message msg = new Message();
        msg.what = message;
        return this.sendMessage(msg);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_INIT_SHAKE:
                initShakeSensor();
                break;
            case MSG_ROTATE_TO_PORTRAIT:
                setModePortrait();
                break;
            case MSG_ROTATE_TO_LANDSCAPE:
                setModeLandscape();
                break;
        }
    }

    private void initShakeSensor() {
        SettingsManager settingsManager = new SettingsManager(mContext);
        if (!settingsManager.isShakeToRotateEnabled()) {
            // Service shouldn't be running
            mServiceHelper.stopService();
        }

        if (!isDeviceLocked(mContext)) {
            mShakeDetector.setSensitivity(ShakeDetector.SENSITIVITY_HARD);
            mShakeDetector.start(mSensorManager);
        }
    }

    private boolean isDeviceLocked(Context context) {
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return myKM.inKeyguardRestrictedInputMode();
    }

    @Override
    public void onLandscapeChanged(int rotation) {
        setRotation(rotation);
    }

    @Override
    public synchronized void hearShake() {
        if(mHandlingShake) {
            return;
        }
        mHandlingShake = true;
        if (SystemClock.elapsedRealtime() < mNextShakeMinTime) {
            mHandlingShake = false;
            return;
        }
        mNextShakeMinTime = SystemClock.elapsedRealtime() + MINIMUM_SHAKE_INTERVAL;
        SettingsManager settingsManager = new SettingsManager(mContext);
        if (!settingsManager.isShakeToRotateEnabled()) {
            mShakeDetector.stop();
            mServiceHelper.stopService();
            mHandlingShake = false;
            return;
        }
        if (settingsManager.isRotationUnlocked()) {
            mHandlingShake = false;
            return;
        }
        mExcludedPackages = settingsManager.getExcludedPackages();
        if (mExcludedPackages.size() > 0 && mExcludedPackages.contains(settingsManager.getForegroundPackage())) {
            mHandlingShake = false;
            return;
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            sendMessage(MSG_ROTATE_TO_LANDSCAPE);
        } else {
            sendMessage(MSG_ROTATE_TO_PORTRAIT);
        }
        mHandlingShake = false;
    }

    private void setRotation(int rotation) {
        Settings.System.putInt(
                mContext.getContentResolver(),
                Settings.System.USER_ROTATION,
                rotation
        );
    }

    private void setModePortrait() {
        setRotation(Surface.ROTATION_0);
        mOrientation = Configuration.ORIENTATION_PORTRAIT;

        if(mLandscapeListener != null) {
            mLandscapeListener.disable();
            mLandscapeListener = null;
        }
        if(mVibrate) {
            long[] pattern = {0, 50, 75, 50};
            Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(pattern, -1);
        }
    }

    private void setModeLandscape() {
        setRotation(Surface.ROTATION_90);
        mOrientation = Configuration.ORIENTATION_LANDSCAPE;

        if (mLandscapeListener.canDetectOrientation()) {
            mLandscapeListener.enable();
        } else {
            Log.e(TAG, "Error: Failed to enable OrientationEventListener");
        }
        if(mVibrate) {
            Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(50);
        }
    }

    @Subscribe
    public void onVibrateChangedEvent(VibrateChangedEvent event) {
        mVibrate = event.mVibrate;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) && !isDeviceLocked(context)) {
                mNextShakeMinTime = SystemClock.elapsedRealtime() + COOLDOWN_DELAY;
                mShakeDetector.start(mSensorManager);

            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                mNextShakeMinTime = SystemClock.elapsedRealtime() + COOLDOWN_DELAY;
                mShakeDetector.start(mSensorManager);

            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                mShakeDetector.stop();
            }
        }
    };

    void close() {
        if(mRegisteredReceiver) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            mRegisteredReceiver = false;
        }
    }
}