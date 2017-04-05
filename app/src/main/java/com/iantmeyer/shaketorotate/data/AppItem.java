package com.iantmeyer.shaketorotate.data;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class AppItem implements Comparable<AppItem> {

    public final Drawable mDrawable;
    public final String mTitle;
    public final String mPackage;
    public boolean mChecked = false;

    AppItem(PackageManager pm, ResolveInfo resolveInfo) {
        mDrawable = resolveInfo.loadIcon(pm);
        mTitle = (String) resolveInfo.loadLabel(pm);
        mPackage = resolveInfo.activityInfo.packageName;
    }

    public void toggleChecked() {
        mChecked = !mChecked;
    }

    @Override
    public int compareTo(AppItem otherItem) {
        return mTitle.toLowerCase().compareTo(otherItem.mTitle.toLowerCase());
    }
}