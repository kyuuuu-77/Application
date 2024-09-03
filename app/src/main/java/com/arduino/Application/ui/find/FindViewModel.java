package com.arduino.Application.ui.find;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FindViewModel extends ViewModel {

    private final MutableLiveData<String> alertTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> alertStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> distanceLiveData = new MutableLiveData<>();

    // 도난방지 텍스트
    public LiveData<String> getAlertTextLiveData() {
        return alertTextLiveData;
    }
    public void setAlertText(String text) {
        alertTextLiveData.setValue(text);
    }

    // 도난방지 상태 텍스트
    public LiveData<String> getAlertStatusLiveData() {
        return alertStatusLiveData;
    }
    public void setAlertStatus(String status) {
        alertStatusLiveData.setValue(status);
    }

    // 캐리어 거리 텍스트
    public LiveData<String> getDistanceLiveData() {
        return distanceLiveData;
    }
    public void setDistance(String bag_distance) {
        distanceLiveData.setValue(bag_distance);
    }

}