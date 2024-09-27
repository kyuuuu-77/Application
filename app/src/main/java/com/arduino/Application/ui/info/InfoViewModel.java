package com.arduino.Application.ui.info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InfoViewModel extends ViewModel {

    private final MutableLiveData<Boolean> autoSearchLiveData = new MutableLiveData<>(true);     // 자동검색
    private final MutableLiveData<String> rssiLiveData = new MutableLiveData<>();           // RSSI 세기
    private final MutableLiveData<String> batteryTextLiveData = new MutableLiveData<>();    // 배터리 잔량
    private final MutableLiveData<String> batteryVoltLiveData = new MutableLiveData<>();    // 배터리 전압
    private final MutableLiveData<String> securityLiveData = new MutableLiveData<>();       // 도난방지
    private final MutableLiveData<Integer> bleStatusLiveData = new MutableLiveData<>();       // 블루투스 상태
    private final MutableLiveData<String> deviceNameLiveData = new MutableLiveData<>();     // 블루투스 디바이스 이름

    // 캐리어 자동 검색
    // 동작중이면 1, 아니면 0
    public LiveData<Boolean> getAutoSearchLiveData() {
        return autoSearchLiveData;
    }
    public void setAutoSearch(boolean search) {
        autoSearchLiveData.setValue(search);
    }

    // 신호 세기
    public LiveData<String> getRssiLiveData() {
        return rssiLiveData;
    }
    public void setRssi(String rssi) {
        rssiLiveData.setValue(rssi);
    }

    // 배터리 정보
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

    // 도난방지 상태
    public LiveData<String> getSecurityLiveData() {
        return securityLiveData;
    }
    public void setSecurity(String security) {
        securityLiveData.setValue(security);
    }

    // 블루투스 상태
    // 사용불가면 -1, 꺼졌으면 0, 켜졌으면 1, 연결은 되었으나 통신에 문제가 있으면 2, 연결 되었으면 9
    public LiveData<Integer> getbleStatusLiveData() {
        return bleStatusLiveData;
    }
    public void setBleStatus(int status) {
        bleStatusLiveData.setValue(status);
    }

    // 디바이스 이름
    public LiveData<String> getdeviceNameLiveData() {
        return deviceNameLiveData;
    }
    public void setdeviceName(String name) {
        deviceNameLiveData.setValue(name);
    }

}