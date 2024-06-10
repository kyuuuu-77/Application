package com.arduino.Application.ui.find;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FindViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public FindViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("여기에 캐리어 위치를 확인하는 프로그램을 작성합니다.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}