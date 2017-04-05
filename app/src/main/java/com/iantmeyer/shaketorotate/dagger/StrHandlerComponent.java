package com.iantmeyer.shaketorotate.dagger;

import com.iantmeyer.shaketorotate.service.StrHandler;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {StrHandlerModule.class})
public interface StrHandlerComponent {
    void inject(StrHandler strHandler);
}