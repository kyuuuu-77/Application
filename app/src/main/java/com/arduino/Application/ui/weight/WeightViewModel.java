package com.arduino.Application.ui.weight;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WeightViewModel extends ViewModel {

    private final MutableLiveData<Double> weightNowLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> weightSetLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> weightInfoLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> weightBtnLiveData = new MutableLiveData<>();

    // 측정된 무게 값
    public LiveData<Double> getWeightNowLiveData() {
        return weightNowLiveData;
    }

    public void setWeightNow(Double weight) {
        weightNowLiveData.setValue(weight);
    }

    // 무게 설정 상태
    public LiveData<Integer> getWeightSetLiveData() {
        return weightSetLiveData;
    }

    public void setWeightSet(int target) {
        weightSetLiveData.setValue(target);
    }

    // 무게 초과 여부
    // 측정하지 않았으면 -1, 초과하지 않았으면 0, 32kg을 초과했으면 999, 그 외에는 초과한 무게
    public LiveData<Double> getWeightInfoLiveData() {
        return weightInfoLiveData;
    }

    public void setWeightInfo(double over) {
        weightInfoLiveData.setValue(over);
    }

    // 측정 버튼 상태
    // 측정할 수 없으면 0, 측정에 실패했으면 -1, 측정할 수 있으면 1, 다시 측정하려면 2
    public LiveData<Integer> getWeightBtnLiveData() {
        return weightBtnLiveData;
    }

    public void setWeightBtn(int status) {
        weightBtnLiveData.setValue(status);
    }

}