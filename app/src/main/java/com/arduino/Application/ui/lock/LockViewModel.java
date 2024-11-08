package com.arduino.Application.ui.lock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LockViewModel extends ViewModel {

    private final MutableLiveData<Integer> lockStatusLiveData = new MutableLiveData<>();

    // 잠금 여부
    public LiveData<Integer> getLockStatusLiveData() {
        return lockStatusLiveData;
    }

    public void setLockStatus(Integer status) {
        lockStatusLiveData.setValue(status);
    }

}