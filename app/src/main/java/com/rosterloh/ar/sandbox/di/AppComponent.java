package com.rosterloh.ar.sandbox.di;

import com.rosterloh.ar.sandbox.ARApp;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        AppModule.class,
        BuildersModule.class})
public interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(ARApp application);
        AppComponent build();
    }
    void inject(ARApp app);
}
