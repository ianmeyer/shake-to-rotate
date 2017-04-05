package com.iantmeyer.shaketorotate.dagger;

import com.iantmeyer.shaketorotate.activity.MainActivity;
import com.iantmeyer.shaketorotate.fragment.applist.AppListPresenter;
import com.iantmeyer.shaketorotate.fragment.settings.SettingsPresenter;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ActivityModule.class})
public interface ActivityComponent {
    void inject(MainActivity activity);

    void inject(SettingsPresenter presenter);

    void inject(AppListPresenter presenter);
}