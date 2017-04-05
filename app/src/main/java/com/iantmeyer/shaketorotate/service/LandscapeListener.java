package com.iantmeyer.shaketorotate.service;

import android.content.Context;
import android.view.OrientationEventListener;

public class LandscapeListener extends OrientationEventListener {

    private static int ORIENTATION_UP = 0;
    private static int ORIENTATION_DOWN = 2;

    private int mOrientation;
    private Listener mListener;

    interface Listener {
        void onLandscapeChanged(int orientation);
    }

    public LandscapeListener(Context context, Listener listener) {
        super(context);
        mListener = listener;
    }

    LandscapeListener(Context context, int rate) {
        super(context, rate);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        int previousOrientation = mOrientation;

        double angle = ((180 + (double) orientation) % 360) / 90;
        mOrientation = (int) Math.round(angle);
        mOrientation = mOrientation % 4;

        if (mOrientation == ORIENTATION_UP || mOrientation == ORIENTATION_DOWN) {
            return;
        }
        if (previousOrientation == mOrientation) {
            return;
        }
        if (mListener == null) {
            return;
        }
        mListener.onLandscapeChanged(mOrientation);
    }
}