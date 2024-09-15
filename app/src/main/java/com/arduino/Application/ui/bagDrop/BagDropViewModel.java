package com.arduino.Application.ui.bagDrop;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BagDropViewModel extends ViewModel {

    private final MutableLiveData<String> remainTimeTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> bagDropTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> connectTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> weightTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> timeTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> bagDropBtnTextLiveData = new MutableLiveData<>();

    // 남은 시간 텍스트
    public LiveData<String> getRemainTimeTextLiveData() {
        return remainTimeTextLiveData;
    }
    public void setRemainTimeText(String time) {
        remainTimeTextLiveData.setValue(time);
    }

    // 백드랍 모드 활성화 여부 텍스트
    public LiveData<String> getBagDropTextLiveData() {
        return bagDropTextLiveData;
    }
    public void setBagDropText(String text) {
        bagDropTextLiveData.setValue(text);
    }

    // 캐리어 연결 여부 텍스트
    public LiveData<String> getConnectTextLiveData() {
        return connectTextLiveData;
    }
    public void setConnectText(String connect) {
        connectTextLiveData.setValue(connect);
    }

    // 무게 측정 여부 텍스트
    public LiveData<String> getWeightTextLiveData() {
        return weightTextLiveData;
    }
    public void setWeightText(String weight) {
        weightTextLiveData.setValue(weight);
    }

    // 도착 예정시각 텍스트
    public LiveData<String> getTimeTextLiveData() {
        return timeTextLiveData;
    }
    public void setTimeText(String time) {
        timeTextLiveData.setValue(time);
    }

    // 백드랍 모드 버튼 텍스트
    public LiveData<String> getBagDropBtnTextLiveData() {
        return bagDropBtnTextLiveData;
    }
    public void setBagDropBtnText(String btnText) {
        bagDropBtnTextLiveData.setValue(btnText);
    }

}