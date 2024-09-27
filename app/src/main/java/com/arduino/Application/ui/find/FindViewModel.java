package com.arduino.Application.ui.find;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FindViewModel extends ViewModel {

    private final MutableLiveData<Boolean> ignoreLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> alertTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> alertStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> distanceLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> alertBtnLiveData = new MutableLiveData<>();

    // 알림 버튼 상태
    // 무시 상태이면 true, 알림이 켜져 있으면 false
    public LiveData<Boolean> getIgnoreLiveData() {
        return ignoreLiveData;
    }

    public void setIgnoreText(boolean ignore) {
        ignoreLiveData.setValue(ignore);
    }

    // 도난방지 메인
    public LiveData<String> getAlertTextLiveData() {
        return alertTextLiveData;
    }

    public void setAlertText(String text) {
        alertTextLiveData.setValue(text);
    }

    // 도난방지 상태
    // 도난방지가 켜져 있으면 true, 아니면 false
    public LiveData<Boolean> getAlertStatusLiveData() {
        return alertStatusLiveData;
    }

    public void setAlertStatus(boolean status) {
        alertStatusLiveData.setValue(status);
    }

    // 캐리어 거리
    // 기본으로 -1, 매우 가까우면 0, 가까우면 1, 떨어져 있으면 2, 멀면 3
    public LiveData<Integer> getDistanceLiveData() {
        return distanceLiveData;
    }

    public void setDistance(int distance) {
        distanceLiveData.setValue(distance);
    }

    // 도난방지 버튼 상태
    // 사용불가 -1, 켜기 상태이면 0, 끄기 상태이면 1
    public LiveData<Integer> getAlertBtnLiveData() {
        return alertBtnLiveData;
    }

    public void setAlertBtn(int status) {
        alertBtnLiveData.setValue(status);
    }

}