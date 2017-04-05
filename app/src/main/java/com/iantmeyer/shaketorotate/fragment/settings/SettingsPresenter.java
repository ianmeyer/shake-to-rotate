package com.iantmeyer.shaketorotate.fragment.settings;

import com.iantmeyer.shaketorotate.activity.MainActivity;
import com.iantmeyer.shaketorotate.data.SettingsManager;
import com.iantmeyer.shaketorotate.service.ServiceHelper;
import com.iantmeyer.shaketorotate.service.VibrateChangedEvent;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

public class SettingsPresenter implements SettingsMvp.Presenter {

    @Inject
    protected SettingsManager mSettingsManager;

    @Inject
    protected ServiceHelper mServiceHelper;

    private SettingsMvp.View mView;

    SettingsPresenter(SettingsMvp.View view) {
        mView = view;
        MainActivity.getComponent().inject(this);
    }

    @Override
    public void onVibrateChanged(boolean vibrate) {
        EventBus.getDefault().post(new VibrateChangedEvent(vibrate));
    }

    @Override
    public void initializeService() {
        boolean enabled = mSettingsManager.isShakeToRotateEnabled();
        if (enabled) {
            mServiceHelper.startService();
        } else {
            mServiceHelper.stopService();
        }
    }

    @Override
    public boolean isUsageStatsEnabled() {
        return mSettingsManager.isUsageStatsEnabled();
    }

    @Override
    public void startService() {
        mServiceHelper.startService();
    }

    @Override
    public void onShakeToRotateChanged(boolean shakeToRotate) {
        if (shakeToRotate) {
            mServiceHelper.startService();
        } else {
            mServiceHelper.stopService();
        }
    }
}