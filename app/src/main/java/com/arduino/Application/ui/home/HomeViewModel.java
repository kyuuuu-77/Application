package com.arduino.Application.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Boolean> authenticateLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> connectBtnLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> bluetoothStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> homeTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> btBtnLiveData = new MutableLiveData<>();

    // 인증 상태
    public LiveData<Boolean> getAuthenticateLiveData() {
        return authenticateLiveData;
    }
    public void setAuthenticate(boolean auth) {
        authenticateLiveData.setValue(auth);
    }

    // 연결 버튼 상태
    public LiveData<Integer> getconnectBtnLiveData() {
        return connectBtnLiveData;
    }
    public void setConnectBtn(int status) {
        connectBtnLiveData.setValue(status);
    }

    // 블루투스 상태
    public LiveData<Integer> getBluetoothStatusLiveData() {
        return bluetoothStatusLiveData;
    }
    public void setBluetoothStatus(int status) {
        bluetoothStatusLiveData.setValue(status);
    }

    // 홈 텍스트
    public LiveData<String> getHomeTextLiveData() {
        return homeTextLiveData;
    }
    public void setHomeText(String text) {
        homeTextLiveData.setValue(text);
    }

    // 블루투스 버튼 상태
    public LiveData<Integer> getBtBtnLiveData() {
        return btBtnLiveData;
    }
    public void setBtBtn(int status) {
        btBtnLiveData.setValue(status);
    }
}