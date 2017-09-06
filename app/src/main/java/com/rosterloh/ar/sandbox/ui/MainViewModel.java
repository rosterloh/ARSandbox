package com.rosterloh.ar.sandbox.ui;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

class MainViewModel extends ViewModel {

    private final MutableLiveData<String> message = new MutableLiveData<>();


    MainViewModel() {
    }

    MutableLiveData<String> getMessage() {
        return message;
    }
}
