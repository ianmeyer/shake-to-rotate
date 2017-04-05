package com.iantmeyer.shaketorotate.fragment.settings;

interface SettingsMvp {
    interface View {

    }

    interface Presenter {
        void onShakeToRotateChanged(boolean shakeToRotate);

        void onVibrateChanged(boolean vibrate);

        void initializeService();

        boolean isUsageStatsEnabled();

        void startService();
    }
}