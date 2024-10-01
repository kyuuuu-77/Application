package com.arduino.Application.ui.info;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InfoViewModel extends ViewModel {

    private final MutableLiveData<Boolean> autoSearchLiveData = new MutableLiveData<>(true);     // 자동검색
    private final MutableLiveData<Integer> rssiLiveData = new MutableLiveData<>();          // RSSI 세기
    private final MutableLiveData<Integer> batteryLiveData = new MutableLiveData<>();       // 배터리 잔량
    private final MutableLiveData<Double> batteryVoltLiveData = new MutableLiveData<>();    // 배터리 전압
    private final MutableLiveData<Boolean> securityLiveData = new MutableLiveData<>();      // 도난방지
    private final MutableLiveData<Integer> bleStatusLiveData = new MutableLiveData<>();     // 블루투스 상태
    private final MutableLiveData<String> deviceNameLiveData = new MutableLiveData<>();     // 블루투스 디바이스 이름

    // 캐리어 자동 검색
    // 동작중이면 true, 아니면 false
    public LiveData<Boolean> getAutoSearchLiveData() {
        return autoSearchLiveData;
    }
    public void setAutoSearch(boolean search) {
        autoSearchLiveData.setValue(search);
    }

    // 신호 세기
    public LiveData<Integer> getRssiLiveData() {
        return rssiLiveData;
    }
    public void setRssi(int rssi) {
        rssiLiveData.setValue(rssi);
    }

    // 배터리 정보
    public LiveData<Integer> getBatteryLiveData() {
        return batteryLiveData;
    }
    public void setBattery(int batt) {
        batteryLiveData.setValue(batt);
    }

    // 배터리 전압 정보 텍스트
    public LiveData<Double> getbatteryVoltLiveData() {
        return batteryVoltLiveData;
    }
    public void setBatteryVolt(double voltage) {
        batteryVoltLiveData.setValue(voltage);
    }

    // 도난방지 상태
    // 사용하면 true, 사용 안하면 false
    public LiveData<Boolean> getSecurityLiveData() {
        return securityLiveData;
    }
    public void setSecurity(boolean security) {
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