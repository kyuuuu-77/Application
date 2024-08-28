package com.arduino.Application.ui.find;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FindViewModel extends ViewModel {

    private final MutableLiveData<String> alertTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> alertStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> alertBtnLiveData = new MutableLiveData<>();

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

    // 도난방지 버튼 텍스트
    public LiveData<String> getAlertBtnLiveData() {
        return alertBtnLiveData;
    }
    public void setAlertBtn(String btn) {
        alertBtnLiveData.setValue(btn);
    }

}