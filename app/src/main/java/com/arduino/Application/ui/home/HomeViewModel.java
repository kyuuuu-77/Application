package com.arduino.Application.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> bluetoothStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> alertStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> homeTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> rssiLiveData = new MutableLiveData<>();

    public LiveData<String> getBluetoothStatusLiveData() {
        return bluetoothStatusLiveData;
    }

    public LiveData<String> getAlertStatusLiveData(){
        return alertStatusLiveData;
    }

    public LiveData<String> getHomeTextLiveData() {
        return homeTextLiveData;
    }

    public void setBluetoothStatus(String status) {
        bluetoothStatusLiveData.setValue(status);
    }

    public void setAlertStatus(String alert){
        alertStatusLiveData.setValue(alert);
    }

    public void setHomeText(String text) {
        homeTextLiveData.setValue(text);
    }
}