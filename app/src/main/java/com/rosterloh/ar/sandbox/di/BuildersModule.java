package com.rosterloh.ar.sandbox.di;

import com.rosterloh.ar.sandbox.ui.MainActivity;
import com.rosterloh.ar.sandbox.ui.MainModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class BuildersModule {
    @ContributesAndroidInjector(modules = MainModule.class)
    abstract MainActivity bindMainActivity();
}
