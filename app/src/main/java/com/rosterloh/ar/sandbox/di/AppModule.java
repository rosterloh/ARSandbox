package com.rosterloh.ar.sandbox.di;

import android.content.Context;

import com.rosterloh.ar.sandbox.ARApp;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Provides
    Context provideContext(ARApp application) {
        return application.getApplicationContext();
    }
}
