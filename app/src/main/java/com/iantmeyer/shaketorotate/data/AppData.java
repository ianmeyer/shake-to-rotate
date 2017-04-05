package com.iantmeyer.shaketorotate.data;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Set;

public class AppData extends Observable {

    private final Context mAppContext;
    private ArrayList<AppItem> mAppItemList = new ArrayList<>();
    private GetInstalledAppTask mTask;

    public AppData(Context appContext) {
        mAppContext = appContext;
    }

    public void init() {
        mTask = new GetInstalledAppTask();
        mTask.execute();
    }

    public ArrayList<AppItem> getAppItemList() {
        return mAppItemList;
    }

    private class GetInstalledAppTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... unused) {
            SettingsManager settingsManager = new SettingsManager(mAppContext);
            Set<String> excludedPackages = settingsManager.getExcludedPackages();

            List<ResolveInfo> appInfoList = settingsManager.getResolveInfoList();
            final PackageManager pm = mAppContext.getPackageManager();
            for (ResolveInfo appInfo : appInfoList) {
                AppItem appItem = new AppItem(pm, appInfo);
                if (excludedPackages.contains(appInfo.activityInfo.packageName)) {
                    appItem.mChecked = true;
                }
                mAppItemList.add(appItem);
            }
            Collections.sort(mAppItemList);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            hasChanged();
            AppData.this.notifyObservers();
        }
    }
}