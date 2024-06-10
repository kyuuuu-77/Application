package com.arduino.Application.ui.weight;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WeightViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public WeightViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("여기에 무게 정보를 표시합니다.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}