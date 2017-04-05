package com.iantmeyer.shaketorotate.fragment.applist;

import com.iantmeyer.shaketorotate.activity.MainActivity;
import com.iantmeyer.shaketorotate.data.AppData;
import com.iantmeyer.shaketorotate.data.AppItem;
import com.iantmeyer.shaketorotate.data.SettingsManager;

import java.util.Observable;
import java.util.Observer;

import javax.inject.Inject;

public class AppListPresenter implements AppListMvp.Presenter, Observer {

    @Inject
    protected SettingsManager mSettingsManager;

    @Inject
    protected AppData mAppData;

    private final AppListMvp.View mView;

    AppListPresenter(AppListMvp.View view) {
        mView = view;
        MainActivity.getComponent().inject(this);

        mAppData.addObserver(this);
        mView.setAppList(mAppData.getAppItemList());
    }

    @Override
    public void onAppItemClick(AppItem item) {
        mSettingsManager.editExcludedPackages(item.mPackage, item.mChecked);
    }

    @Override
    public void update(Observable observable, Object arg) {
        if (observable instanceof AppData) {
            AppData appData = (AppData) observable;
            mView.setAppList(appData.getAppItemList());
        }
    }
}