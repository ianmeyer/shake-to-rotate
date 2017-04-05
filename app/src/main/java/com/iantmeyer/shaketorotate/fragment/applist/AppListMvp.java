package com.iantmeyer.shaketorotate.fragment.applist;

import com.iantmeyer.shaketorotate.data.AppItem;

import java.util.List;

interface AppListMvp {

    interface View {
        void setAppList(List<AppItem> appList);
    }

    interface Presenter {
        void onAppItemClick(AppItem appItem);
    }
}
