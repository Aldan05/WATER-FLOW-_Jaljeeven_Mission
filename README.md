# Pure Flow 💧

**Pure Flow** is a professional-grade Android application designed for real-time water quality monitoring and analysis. It combines smart hardware integration with cloud-based data tracking to ensure your drinking water is always safe.

## 🚀 Key Features

### 📡 Smart Hardware Integration (ESP32)
- **Live TDS Sensing**: Connects directly to an ESP32-based TDS sensor via WiFi.
- **Auto-Fetch Mode**: Automatically retrieves and updates Total Dissolved Solids (TDS) levels every 5 seconds.
- **WiFi Setup**: Quick access to connect your phone to the `TDS_ESP32` hotspot.

### 📊 Advanced Data Visualization
- **Multi-Chart Dashboard**: High-contrast charts including Pie, Line, Bar, Scatter, and Radar graphs.
- **Parameter Tracking**: Monitor pH, Chlorine, Silver Ion, and TDS levels over time.
- **Health Insights**: Automated safety alerts and health warnings based on detected levels.

### 📜 Water Quality History
- **Record Logging**: Save every test result to a secure backend.
- **Historical Analysis**: A clean, high-contrast list of all past records with safety status badges (SAFE/UNSAFE).
- **Offline Persistence**: View your data even without an active internet connection.

### 🤖 AI Chat Assistant
- **Pure Flow Assistant**: A live AI chat system to answer basic queries about water safety.
- **Treatment Advice**: Ask about how to prevent or treat high acidity, high chlorine, or high TDS levels.
- **Real-time Interaction**: Features a visual typing indicator for a natural conversational experience.

### 🔐 Security & Support
- **Firebase Authentication**: Secure email-based login and registration.
- **Government Contact**: Integrated quick-dial for official water authorities (Emergency: 1916).
- **Technical Support**: Direct email link to our technical support team.

---

## 🛠️ Technical Stack

- **Platform**: Android
- **Language**: Java
- **Backend**: Firebase (Authentication, Realtime Database, Analytics)
- **Networking**: OkHttp (for local ESP32 communication)
- **Visualization**: MPAndroidChart
- **Hardware**: ESP32 with TDS Sensor

---

## 🏗️ Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- ESP32 hardware with the provided firmware
- Firebase project setup with `google-services.json`

### Installation
1. Clone the repository.
2. Place your `google-services.json` in the `app/` directory.
3. Build and run the app on an Android device (minSdk 33).
4. For live sensing, connect your device to the `TDS_ESP32` WiFi network.

---

## 🛡️ Safety Limits Used
The app evaluates water safety based on the following professional ranges:
- **pH**: 1.0 - 14.0 (Target: 6.5 - 8.5)
- **Chlorine**: 1.0 - 12.0 ppm
- **Silver Ion**: 1.0 - 10.0 ppm
- **TDS**: < 500 ppm

---

## 📝 License
This project is for monitoring and educational purposes. Always consult official water authorities for critical health decisions.
