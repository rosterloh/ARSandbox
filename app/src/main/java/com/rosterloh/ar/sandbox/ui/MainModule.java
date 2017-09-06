package com.rosterloh.ar.sandbox.ui;

import dagger.Module;
import dagger.Provides;

@Module
public class MainModule {
    @Provides
    MainViewModelFactory provideMainViewModelFactory() {
        return new MainViewModelFactory();
    }
}
