package com.iantmeyer.shaketorotate.fragment.settings;

import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iantmeyer.shaketorotate.R;
import com.iantmeyer.shaketorotate.activity.MainActivity;
import com.iantmeyer.shaketorotate.data.SettingsManager;
import com.iantmeyer.shaketorotate.fragment.applist.AppListFragment;

import static com.iantmeyer.shaketorotate.data.SettingsManager.SETTINGS_LAUNCH_APP_LIST;
import static com.iantmeyer.shaketorotate.data.SettingsManager.SETTINGS_LICENSES;
import static com.iantmeyer.shaketorotate.data.SettingsManager.SETTINGS_OPEN_GOOGLE_PLAY;
import static com.iantmeyer.shaketorotate.data.SettingsManager.SETTINGS_SHAKE_TO_ROTATE;
import static com.iantmeyer.shaketorotate.data.SettingsManager.SETTINGS_VIBRATE;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener, SettingsMvp.View {

    private static final String TAG = "SettingsFragment";

    private Context mContext;
    private boolean mInitialized = false;
    private boolean mRequestingPermission = false;
    private boolean mRequestingUsagePermission = false;

    private SettingsMvp.Presenter mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getBaseContext();

        mPresenter = new SettingsPresenter(this);

        addPreferencesFromResource(R.xml.pref_main);
        setHasOptionsMenu(true);

        findPreference(SETTINGS_SHAKE_TO_ROTATE).setOnPreferenceChangeListener(this);
        findPreference(SETTINGS_VIBRATE).setOnPreferenceChangeListener(this);
        findPreference(SETTINGS_LAUNCH_APP_LIST).setOnPreferenceClickListener(this);
        findPreference(SETTINGS_OPEN_GOOGLE_PLAY).setOnPreferenceClickListener(this);
        findPreference(SETTINGS_LICENSES).setOnPreferenceClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        String activityTitle = getResources().getString(R.string.app_name);

        MainActivity settingsActivity = (MainActivity) getActivity();
        settingsActivity.getSupportActionBar().setTitle(activityTitle);
        settingsActivity.hideBackButton();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mRequestingUsagePermission) {
            mRequestingUsagePermission = false;
            if (mPresenter.isUsageStatsEnabled()) {
                gotoAppListActivity();

            }
        }
        if (!mInitialized) {
            mPresenter.initializeService();
            mInitialized = true;
        }
        if (mRequestingPermission) {
            mRequestingPermission = false;
            if (!needsSettingsPermission()) {
                ((SwitchPreference) findPreference(SETTINGS_SHAKE_TO_ROTATE)).setChecked(true);
                mPresenter.startService();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference.getKey().equals(SETTINGS_SHAKE_TO_ROTATE)) {
            if (needsSettingsPermission()) {
                requestSettingsPermission();
                return false;
            }
            mPresenter.onShakeToRotateChanged((boolean) value);
        } else if (preference.getKey().equals(SETTINGS_VIBRATE)) {
            mPresenter.onVibrateChanged((boolean) value);
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(SETTINGS_LAUNCH_APP_LIST)) {
            SettingsManager settingsManager = new SettingsManager(mContext);
            if (settingsManager.isUsageStatsEnabled()) {
                gotoAppListActivity();
                return true;
            } else {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//                intent.updateData(Uri.parse("package:" + getActivity().getPackageName()));
                mRequestingUsagePermission = true;
                startActivity(intent);
                return false;
            }

        } else if (preference.getKey().equals(SETTINGS_OPEN_GOOGLE_PLAY)) {
            openAppStore();
            return true;

        } else if (preference.getKey().equals(SETTINGS_LICENSES)) {
            showLicenseDialog();
            return true;

        } else {
            return false;
        }
    }

    private void gotoAppListActivity() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        int resId = ((ViewGroup) getView().getParent()).getId();
        ft.replace(resId, new AppListFragment());
        ft.addToBackStack(null);
        ft.commit();
    }

    private void openAppStore() {
        String packageName = mContext.getPackageName();
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        );
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)
                    )
            );
        }
    }

    private boolean needsSettingsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(mContext)) {
                return true;
            }
        }
        return false;
    }

    private void requestSettingsPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mRequestingPermission = true;
            getActivity().startActivity(intent);
        }
    }

    private void showLicenseDialog() {
        String licenseText = "";
        licenseText += getResources().getString(R.string.license_pre_apache);
        licenseText += "\n\n";
        licenseText += "- " + getResources().getString(R.string.license_eventbus);
        licenseText += "\n\n";
        licenseText += "- " + getResources().getString(R.string.license_seismic);
        licenseText += "\n\n";
        licenseText += getResources().getString(R.string.license_apache);

        SpannableString spanText = new SpannableString(licenseText);

        Linkify.addLinks(spanText, Linkify.ALL);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setMessage(spanText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTextSize(12);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}