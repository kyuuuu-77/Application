# ✈️  Smart Suitcase
스마트 캐리어

## 💻 프로젝트 소개 (Introduction)
esp32와 안드로이드 앱을 연동하여 다양한 기능이 사용 가능한 스마트 캐리어 애플리케이션 입니다.

사용된 모듈 및 센서 (esp32)
무게 센서, TP4056 (충전 모듈), 0.91 inch 128 X 32 LCD, 1KΩ 저항, 3.7V 리튬폴리머 충전지, HX711, 등등...

제작 기간
2024.04.02 ~ 2024.10.31 (예정)

## 💡 주요 기능
#### 메인화면 
- 블루투스(BLE) 연결
- 디바이스 선택
- 자동 검색 및 자동 연결
- 앱 정보 확인
#### 캐리어 찾기
- 캐리어 찾기 기능
- 캐리어와의 거리를 텍스트와 그래프로 표시
- 도난방지 경고 (오버레이)
- 도난방지 알림 ON/OFF
#### 캐리어 무게 측정
- 무게 설정 및 값 저장
- 무게 측정 및 초과여부 확인
#### 백드랍 모드
- 도착 시각 설정
- 백드랍 모드 사용시 캐리어와 연결 해제
- 도착 예정시각에 캐리어를 스캔 및 탐지하면 알람을 울려서 알림
#### 캐리어 정보 
- RSSI 신호세기 (dBm), 자동검색 사용여부, 도난방지 작동여부, 블루투스 연결 여부 확인
- 배터리 잔량 및 전압 확인

## 🔍 개발 환경  (Development Environment)
<img src="https://img.shields.io/badge/Android Studio%20-3DDC84?style=flat&logo=Android&logoColor=white"/>  <img src="https://img.shields.io/badge/Arduino%20-00878F?style=flat&logo=Arduino&logoColor=white"/>  <img src="https://img.shields.io/badge/C %20-A8B9CC?style=flat&logo=C&logoColor=white"/> <img src="https://img.shields.io/badge/Java %20-007396?style=flat&logo=Java&logoColor=white"/> <img src="https://img.shields.io/badge/Git %20-F05032?style=flat&logo=Git&logoColor=white"/> <img src="https://img.shields.io/badge/Github %20-181717?style=flat&logo=Github&logoColor=white"/>

## 📲 애플리케이션 SDK 버전 (Application Verison)
- `minSdkVersion` : 28
- `targetSdkVersion` : 34
Android 9.0 Pie (API 28) ~ Android 14 (API 34) 지원하여 대부분 기기와 호환

애플리케이션 요구 권한
알림 권한
POST_NOTIFICATIONS,
SYSTEM_ALERT_WINDOW
블루투스 권한
BLUETOOTH_CONNECT,
BLUETOOTH_SCAN,
BLUETOOTH_ADMIN,
위치 권한
ACCESS_COARSE_LOCATION,
ACCESS_FINE_LOCATION
오버레이 권한



📌 updated 2024.10.01 

