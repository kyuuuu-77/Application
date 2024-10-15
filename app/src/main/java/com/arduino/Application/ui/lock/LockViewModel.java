package com.arduino.Application.ui.lock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LockViewModel extends ViewModel {

    private final MutableLiveData<Integer> remainTimeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> bagDropStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> connectStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> weightLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> timeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> bagDropBtnLiveData = new MutableLiveData<>();

    // 남은 시간
    public LiveData<Integer> getRemainTimeLiveData() {
        return remainTimeLiveData;
    }
    public void setRemainTime(Integer time) {
        remainTimeLiveData.setValue(time);
    }

    // 백드랍 모드 활성화 여부
    public LiveData<Boolean> getBagDropStatusLiveData() {
        return bagDropStatusLiveData;
    }
    public void setBagDropStatus(Boolean bagDrop) {
        bagDropStatusLiveData.setValue(bagDrop);
    }

    // 캐리어 연결 여부
    public LiveData<Boolean> getConnectStatusLiveData() {
        return connectStatusLiveData;
    }
    public void setConnectStatus(Boolean connect) {
        connectStatusLiveData.setValue(connect);
    }

    // 무게 측정 여부
    public LiveData<Double> getWeightLiveData() {
        return weightLiveData;
    }
    public void setWeight(Double weight) {
        weightLiveData.setValue(weight);
    }

    // 도착 예정시각
    public LiveData<Integer> getTimeLiveData() {
        return timeLiveData;
    }
    public void setTime(int time) {
        timeLiveData.setValue(time);
    }

    // 백드랍 모드 버튼 상태
    public LiveData<Boolean> getBagDropBtnLiveData() {
        return bagDropBtnLiveData;
    }
    public void setBagDropBtn(Boolean status) {
        bagDropBtnLiveData.setValue(status);
    }

}