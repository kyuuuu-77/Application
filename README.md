# ✈️  Smart Suitcase
스마트 캐리어

## 💻 프로젝트 소개 (Introduction)
ESP32와 BLE를 통해 통신 및 제어 가능하고 다양한 기능이 사용 가능한 스마트 캐리어 애플리케이션을 개발하는 프로젝트입니다.

#### 🧷 사용된 모듈 및 센서
ESP32 WROOM 32 CH340 기반

무게 센서, TP4056 (충전 모듈), 0.96 inch 128 X 64 LCD, 1KΩ 저항, 3.7V 리튬폴리머 충전지, HX-711, AM-3AXIS(가속도), Buzzer (부저)

#### ⏰ 개발 기간
- 2024.04.02 ~ 2024.10.30 (기본 개발)
- 2024.10.31 ~ 2024.11.07 (디버깅 및 버그 수정)

## 💡 주요 기능
#### 앱바 (Toolbar)
- 자동 검색 설정
- 앱 정보 확인
#### 메인화면
- 블루투스(BLE) 연결
- 자동 검색 및 자동 연결
- 캐리어 연결 여부 확인
- 캐리어와 인증
#### 캐리어 찾기
- 캐리어 찾기 기능
- 캐리어와의 거리를 텍스트와 그래프로 표시
- 도난방지 경고 (오버레이 및 경고음으로 경고)
- 도난방지 알림 ON/OFF
#### 캐리어 잠금
- 캐리어 잠금 상태 확인
- 캐리어 잠금 및 잠금 해제
- 움직임이 감지된 경우 부저로 경고
#### 캐리어 무게 측정
- 타겟 무게 설정
- 무게 측정 및 초과여부 확인
- 무게 측정 설정 리셋
#### 백드랍 모드
- 도착 시각 설정
- 백드랍 모드 사용시 캐리어와 연결 해제
- 도착 예정시각 10분 전에 캐리어를 스캔 및 탐지하면 연결 시도 후 부저를 울려서 알림
#### 캐리어 정보 
- RSSI 신호세기 (dBm), 자동검색 사용여부, 도난방지 작동여부, 블루투스 연결 여부, 디바이스 이름 확인
- 배터리 잔량, 전압, 충전여부 확인
- 화면 갱신 버튼으로 배터리 잔량 수동 갱신
- 앱 설정 초기화

## 🔍 개발 환경  (Development Environment)
<img src="https://img.shields.io/badge/Android Studio%20-3DDC84?style=flat&logo=Android&logoColor=white"/>  <img src="https://img.shields.io/badge/Arduino%20-00878F?style=flat&logo=Arduino&logoColor=white"/>  <img src="https://img.shields.io/badge/C %20-A8B9CC?style=flat&logo=C&logoColor=white"/> <img src="https://img.shields.io/badge/Java %20-007396?style=flat&logo=Java&logoColor=white"/> <img src="https://img.shields.io/badge/Git %20-F05032?style=flat&logo=Git&logoColor=white"/> <img src="https://img.shields.io/badge/Github %20-181717?style=flat&logo=Github&logoColor=white"/>

## 📲 애플리케이션 SDK 버전 (Application Verison)
- `minSdkVersion` : 28
- `targetSdkVersion` : 34

###### *Android 9.0 Pie (API 28) ~ Android 14 (API 34)* 

#### 개발 버전
- `2024.10.31 16:16 (KST) 1.302` (Master branch 기준)<br>
- `2024.10.31 16:16 (KST) 1.302` (Development branch 기준)

------
📌 *updated 2024.10.31*
