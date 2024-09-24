package com.arduino.Application.ui.info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InfoViewModel extends ViewModel {

    private final MutableLiveData<String> batteryTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> batteryVoltLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> deviceNameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> rssiLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> autoSearchLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> securityLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> infoTextLiveData = new MutableLiveData<>();

    // 배터리 정보 텍스트
    public LiveData<String> getbatteryTextLiveData() {
        return batteryTextLiveData;
    }
    public void setBatteryText(String batt) {
        batteryTextLiveData.setValue(batt);
    }

    // 배터리 전압 정보 텍스트
    public LiveData<String> getbatteryVoltLiveData() {
        return batteryVoltLiveData;
    }
    public void setBatteryVolt(String volt) {
        batteryVoltLiveData.setValue(volt);
    }


    // 디바이스 이름
    public LiveData<String> getdeviceNameLiveData() {
        return deviceNameLiveData;
    }
    public void setdeviceName(String name) {
        deviceNameLiveData.setValue(name);
    }

    // RSSI 센서값
    public LiveData<String> getRssiLiveData() {
        return rssiLiveData;
    }
    public void setRssi(String rssi) {
        rssiLiveData.setValue(rssi);
    }

    // 캐리어 자동 검색
    public LiveData<String> getAutoSearchLiveData() {
        return autoSearchLiveData;
    }
    public void setAutoSearch(String search) {
        autoSearchLiveData.setValue(search);
    }

    // 도난방지 상태
    public LiveData<String> getSecurityLiveData() {
        return securityLiveData;
    }
    public void setSecurity(String security) {
        securityLiveData.setValue(security);
    }

    // Info 데이터 표시
    public LiveData<String> getInfoTextLiveData() {
        return infoTextLiveData;
    }
    public void setInfoText(String text) {
        infoTextLiveData.setValue(text);
    }

}