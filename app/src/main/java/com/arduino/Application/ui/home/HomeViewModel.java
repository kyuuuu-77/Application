package com.arduino.Application.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> bluetoothStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> homeTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> btBtnLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> connectBtnLiveData = new MutableLiveData<>();

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

    // 블루투스 버튼 텍스트
    public LiveData<String> getBtBtnLiveData() {
        return btBtnLiveData;
    }
    public void setBtBtn(String text) {
        btBtnLiveData.setValue(text);
    }

    // 연결 버튼 텍스트
    public LiveData<String> getconnectBtnLiveData() {
        return connectBtnLiveData;
    }
    public void setConnectBtn(String text) {
        connectBtnLiveData.setValue(text);
    }
}