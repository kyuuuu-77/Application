package com.arduino.Application.ui.weight;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WeightViewModel extends ViewModel {

    private final MutableLiveData<String> weightNowLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> weightTpsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> looseWeightLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> weightBtnLiveData = new MutableLiveData<>();

    public LiveData<String> getWeightNowLiveData() {
        return weightNowLiveData;
    }

    public LiveData<String> getWeightTpsLiveData() {
        return weightTpsLiveData;
    }

    public LiveData<String> getLooseWeightLiveData() {
        return looseWeightLiveData;
    }

    public LiveData<String> getWeightBtnLiveData() {
        return weightBtnLiveData;
    }

    public void setWeightNow(String weight) {
        weightNowLiveData.setValue(weight);
    }

    public void setWeightTps(String tps) {
        weightTpsLiveData.setValue(tps);
    }

    public void setLooseWeight(String loose) {
        looseWeightLiveData.setValue(loose);
    }

    public void setWeightBtn(String btn) {
        weightBtnLiveData.setValue(btn);
    }
}