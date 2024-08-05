package com.arduino.Application.ui.alert;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AlertViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AlertViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("여기에 도난방지 프로그램을 작성합니다.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}