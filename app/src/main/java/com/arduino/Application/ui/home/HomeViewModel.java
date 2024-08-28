package com.arduino.Application.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> bluetoothStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> homeTextLiveData = new MutableLiveData<>();

    // 블루투스 상태
    public LiveData<String> getBluetoothStatusLiveData() {
        return bluetoothStatusLiveData;
    }
    public void setBluetoothStatus(String status) {
        bluetoothStatusLiveData.setValue(status);
    }

    // 홈 텍스트
    public LiveData<String> getHomeTextLiveData() {
        return homeTextLiveData;
    }
    public void setHomeText(String text) {
        homeTextLiveData.setValue(text);
    }
}