package com.arduino.Application.ui.lock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LockViewModel extends ViewModel {

    private final MutableLiveData<Boolean> lockStatusLiveData = new MutableLiveData<>();

    // 잠금 여부
    public LiveData<Boolean> getLockStatusLiveData() {
        return lockStatusLiveData;
    }
    public void setLockStatus(Boolean status) {
        lockStatusLiveData.setValue(status);
    }

}