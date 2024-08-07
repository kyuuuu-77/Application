package com.arduino.Application.ui.info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InfoViewModel extends ViewModel {

    private final MutableLiveData<String> rssiLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> infoTextLiveData = new MutableLiveData<>();

    public LiveData<String> getRssiLiveData() {
        return rssiLiveData;
    }

    public LiveData<String> getInfoTextLiveData() {
        return infoTextLiveData;
    }

    public void setRssi(String rssi) {
        rssiLiveData.setValue(rssi);
    }

    public void setInfoText(String text) {
        infoTextLiveData.setValue(text);
    }
}