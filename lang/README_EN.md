# ✈️  Smart Suitcase

Language :
[한국어](/README.md) | [日本語](/lang/README_JA.md)

## 💻 Outline
This project develop smart carrier application that can connect and control suitcase with various functions.

#### 🧷 Used modules and sensors
ESP32 WROOM 32 CH340 Microcontroller

weight sensor, TP4056 (charge Module), 0.96 inch 128 X 64 LCD, 1KΩ resistance, 3.7V lithium polymer battery, HX-711, AM-3AXIS(acceleration), Buzzer (buzzer)

#### ⏰ Development term
- 2024.04.02 ~ 2024.10.30 (main development)
- 2024.10.31 ~ 2024.11.07 (debugging and bug fix)

## 💡 Application Functions
#### App bar (Toolbar)
- Automatic search setting
- Check application information
#### Main Screen
- Bluetooth(BLE) connection
- Automatic search and connection
- Suitcase connection status
- Suitcase authentication
#### Find Suitcase
- Find suitcase feature
- Display distance to suitcase in text and graph
- Safety alert (overlay and alert sound)
- Toggle safety alert on/off
#### Lock Suitcase
- Check lock status
- Lock and unlock suitcase
- Sound alert if movement is detected
#### Suitcase Weight Measurement
- Set target weight
- Display current weight and overweight status
- Reset weight measurement settings
#### Backdrop Mode
- Set arrival time
- Disconnect from suitcase when backdrop mode is enabled
- Start suitcase scan 10 minutes before the arrival time and sound buzzer upon successful connection
#### Suitcase Information
- Check RSSI signal strength (dBm), automatic search status, safety status, suitcase connection status, and device name
- View battery level, voltage, and charging status
- Refresh battery status with the update button
- Reset application settings

## 🔍 Development Environment
<img src="https://img.shields.io/badge/Android Studio%20-3DDC84?style=flat&logo=Android&logoColor=white"/>  <img src="https://img.shields.io/badge/Arduino%20-00878F?style=flat&logo=Arduino&logoColor=white"/>  <img src="https://img.shields.io/badge/C %20-A8B9CC?style=flat&logo=C&logoColor=white"/> <img src="https://img.shields.io/badge/Java %20-007396?style=flat&logo=Java&logoColor=white"/> <img src="https://img.shields.io/badge/Git %20-F05032?style=flat&logo=Git&logoColor=white"/> <img src="https://img.shields.io/badge/Github %20-181717?style=flat&logo=Github&logoColor=white"/>

## 📲 Application SDK Verison
- `minSdkVersion` : 28
- `targetSdkVersion` : 34

###### *Android 9.0 Pie (API 28) ~ Android 14 (API 34)* 

#### Development Version
- `2024.10.31 16:16 (KST) 1.302` (Master branch 기준)<br>
- `2024.11.05 0:22 (KST) 1.31` (Development branch 기준)

------
📌 *updated 2024.11.05*
