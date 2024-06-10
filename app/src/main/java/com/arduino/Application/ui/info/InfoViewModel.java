package com.arduino.Application.ui.info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InfoViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public InfoViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("여기에 정보를 표시합니다.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}