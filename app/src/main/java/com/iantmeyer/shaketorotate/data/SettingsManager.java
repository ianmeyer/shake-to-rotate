package com.iantmeyer.shaketorotate.data;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsManager {

    private static final String TAG = "SettingsManager";

    public static final String SETTINGS_SHAKE_TO_ROTATE = "settings_shake_to_rotate";
    public static final String SETTINGS_VIBRATE = "settings_vibrate";
    public static final String SETTINGS_LAUNCH_APP_LIST = "settings_launch_app_list";
    public static final String SETTINGS_OPEN_GOOGLE_PLAY = "settings_open_google_play";
    public static final String SETTINGS_EXCLUDE_APP_LIST = "settings_exclude_app_list";
    public static final String SETTINGS_LICENSES = "settings_licenses";

    private final Context mContext;
    private SharedPreferences mPrefs;

    public SettingsManager(Context context) {
        mContext = context;
    }

    private SharedPreferences prefs() {
        if (mPrefs == null) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        return mPrefs;
    }

    public boolean getVibrateSetting() {
        return prefs().getBoolean(SETTINGS_VIBRATE, false);
    }

    public Set<String> getExcludedPackages() {
        return prefs().getStringSet(SETTINGS_EXCLUDE_APP_LIST, new HashSet<String>());
    }

    public boolean editExcludedPackages(String packageName, boolean add) {
        Set<String> excludedPackages = getExcludedPackages();
        if (add) {
            excludedPackages.add(packageName);
        } else {
            excludedPackages.remove(packageName);
        }
        return prefs().edit().putStringSet(SETTINGS_EXCLUDE_APP_LIST, excludedPackages).commit();
    }

    public List<ResolveInfo> getResolveInfoList() {
        PackageManager pm = mContext.getPackageManager();
        return pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);
    }

    public boolean isShakeToRotateEnabled() {
        return prefs().getBoolean(SETTINGS_SHAKE_TO_ROTATE, false);
    }

    public boolean isRotationUnlocked() {
        int orientationSetting = 0;
        try {
            orientationSetting = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "SettingNotFoundException: " + e.getLocalizedMessage());
        }
        return (orientationSetting == 1);
    }

    public String getForegroundPackage() {

        String packageName = "";
        List<UsageStats> stats = getUsageStats();
        if (stats != null) {
            long lastUsedAppTime = 0;
            for (UsageStats usageStats : stats) {
                if (usageStats.getLastTimeUsed() > lastUsedAppTime) {
                    packageName = usageStats.getPackageName();
                    lastUsedAppTime = usageStats.getLastTimeUsed();
                }
            }
        }
        return packageName;
    }

    public boolean isUsageStatsEnabled() {
        List<UsageStats> stats = getUsageStats();
        if (stats == null || stats.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    private List<UsageStats> getUsageStats() {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        return mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 60, time);
    }
}