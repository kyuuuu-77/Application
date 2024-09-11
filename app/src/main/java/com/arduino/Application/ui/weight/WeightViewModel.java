package com.arduino.Application.ui.weight;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WeightViewModel extends ViewModel {

    private final MutableLiveData<String> weightNowLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> weightSetLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> weightInfoLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> weightBtnLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> airlineLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> baggageLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> weightSelectedLiveData = new MutableLiveData<>();

    // 측정된 무게 텍스트
    public LiveData<String> getWeightNowLiveData() {
        return weightNowLiveData;
    }
    public void setWeightNow(String weight) {
        weightNowLiveData.setValue(weight);
    }

    // 무게 설정 텍스트
    public LiveData<String> getWeightSetLiveData() {
        return weightSetLiveData;
    }
    public void setWeightSet(String tps) {
        weightSetLiveData.setValue(tps);
    }

    // 무게 정보 텍스트
    public LiveData<String> getWeightInfoLiveData() {
        return weightInfoLiveData;
    }
    public void setWeightInfo(String info) {
        weightInfoLiveData.setValue(info);
    }

    // 측정 버튼 텍스트
    public LiveData<String> getWeightBtnLiveData() {
        return weightBtnLiveData;
    }
    public void setWeightBtn(String btn) {
        weightBtnLiveData.setValue(btn);
    }

    // 수화물 유형 텍스트
    public LiveData<String> getBaggageLiveData() {return baggageLiveData;}
    public void setBaggage(String bag) {baggageLiveData.setValue(bag);}

    // 선택된 무게 텍스트
    public LiveData<String> getWeightSelectedLiveData() {return weightSelectedLiveData;}
    public void setWeightSelected(String weight) {weightSelectedLiveData.setValue(weight);}

}