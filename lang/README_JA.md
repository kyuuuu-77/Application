# ✈️  Smart Suitcase
スマートスーツケース

Language :
[한국어](/README.md) | [English](/lang/README_EN.md)

## 💻 プロジェクト紹介
ESP32とBLEを使って通信制御を行い、多機能なスマートスーツケースアプリケーションを開発するプロジェクトです。

#### 🧷 使用モジュールとセンサー
マイコン：ESP32 WROOM 32 CH340

Weightセンサー、TP4056（充電モジュール、0.96 inch 128 X 64 LCD、1KΩ 抵抗、3.7V リチウムポリマー充電池、HX-711、AM-3AXIS(加速度)、Buzzer (ブザー)

#### ⏰ 開発期間
- 2024.04.02 ~ 2024.10.30 (メイン開発)
- 2024.10.31 ~ 2024.11.07 (デバッグ及びバグ修正)

## 💡 アプリケーションの機能 (Application Function)
#### ツールバー (Toolbar)
- 自動検索の設定
- アプリケーション情報の確認
#### メイン画面
- Bluetooth(BLE)接続
- 自動検索と自動接続
- スーツケース接続確認
- スーツケースとの認証
#### スーツケース探し
- スーツケース探し機能
- スーツケースとの距離をテキストとグラフで表示
- セーフティー警告（オーバーレイと警告音）
- セーフティーアラートオン・オフ
#### スーツケースロック
- ロック状態の確認
- スーツケースのロックとアンロック
- 動きが検出された場合、ブザーで警告
#### スーツケース重量測定
- ターゲット重量の設定
- 測定で現在重量と超過重量を表示
- 測定設定のリセット
#### バックドロップモード
- 到着時刻の設定
- バックドロップモードがオンになるとスーツケースと接続解除
- 到着予定時刻10分前からスーツケースをスキャン、接続に成功したらブザーが鳴る
#### スーツケース情報
- RSSI信号強度（dBm）、自動検索状態、セーフティー状態、スーツケース接続状態、デバイス名前確認
- バッテリー残量、電圧、充電状態の確認
- 画面更新ボタンでバッテリー状態を更新
- アプリ設定のリセット

## 🔍 開発環境 (Development Environment)
<img src="https://img.shields.io/badge/Android Studio%20-3DDC84?style=flat&logo=Android&logoColor=white"/>  <img src="https://img.shields.io/badge/Arduino%20-00878F?style=flat&logo=Arduino&logoColor=white"/>  <img src="https://img.shields.io/badge/C %20-A8B9CC?style=flat&logo=C&logoColor=white"/> <img src="https://img.shields.io/badge/Java %20-007396?style=flat&logo=Java&logoColor=white"/> <img src="https://img.shields.io/badge/Git %20-F05032?style=flat&logo=Git&logoColor=white"/> <img src="https://img.shields.io/badge/Github %20-181717?style=flat&logo=Github&logoColor=white"/>

## 📲 アプリケーションのSDKバージョン (Application Verison)
- `minSdkVersion` : 28
- `targetSdkVersion` : 34

###### *Android 9.0 Pie (API 28) ~ Android 14 (API 34)* 

#### 開発バージョン
- `2024.10.31 16:16 (KST) 1.302` (Master branch 基準)<br>
- `2024.11.05 0:22 (KST) 1.31` (Development branch 基準)

------
📌 *更新：2024年11月05日月曜日*
