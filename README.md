# Chargify

<div align="center">
  <img src="https://raw.githubusercontent.com/ahmmedrejowan/Chargify/main/files/logo.png" alt="Chargify Logo" width="120" height="120">

<h3>Advanced Battery Monitor & Charging Manager</h3>

  <p>
    A modern, feature-rich Android app for real-time battery monitoring, charging history tracking, and smart battery alarms.
  </p>

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Latest-blue.svg)](https://developer.android.com/jetpack/compose)
</div>

---

## Features

- **Real-time Monitoring** - Live battery level, temperature, voltage, and current tracking
- **Charging History** - Track all charging sessions with detailed statistics
- **Smart Alarms** - Customizable alerts for full charge, low battery, and custom thresholds
- **Screen Time** - Monitor app usage and screen time statistics
- **Live Charts** - Visual graphs for current, temperature, voltage, and power trends
- **Session Stats** - ETA to full charge, time remaining, and charging speed detection
- **Battery Tips** - Expert recommendations for battery health and longevity
- **Material 3 Design** - Modern UI with dark mode and dynamic colors
- **100% Offline** - Complete privacy, no internet required

---

## Download

![GitHub Release](https://img.shields.io/github/v/release/ahmmedrejowan/Chargify)

You can download the latest APK from here

<a href="https://github.com/ahmmedrejowan/Chargify/releases/download/1.0.0/Chargify_1.0.0.apk">
<img src="https://raw.githubusercontent.com/ahmmedrejowan/Chargify/main/files/get.png" width="224px" align="center"/>
</a>

Check out the [releases](https://github.com/ahmmedrejowan/Chargify/releases) section for more details.

---

## Screenshots

| Shots                                                                                           | Shots                                                                                           | Shots                                                                                           |
|-------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| ![Screenshot 1](https://raw.githubusercontent.com/ahmmedrejowan/Chargify/main/files/shot1.jpg) | ![Screenshot 2](https://raw.githubusercontent.com/ahmmedrejowan/Chargify/main/files/shot2.jpg) | ![Screenshot 3](https://raw.githubusercontent.com/ahmmedrejowan/Chargify/main/files/shot3.jpg) |
| ![Screenshot 4](https://raw.githubusercontent.com/ahmmedrejowan/Chargify/main/files/shot4.jpg) | ![Screenshot 5](https://raw.githubusercontent.com/ahmmedrejowan/Chargify/main/files/shot5.jpg) | ![Screenshot 6](https://raw.githubusercontent.com/ahmmedrejowan/Chargify/main/files/shot6.jpg) |

---

## Architecture

Chargify follows **MVVM** architecture with **Repository** pattern:

```
app/
├── data/                      # Data layer
│   ├── local/                 # Room database
│   ├── model/                 # Data models
│   ├── preferences/           # SharedPreferences wrappers
│   └── repository/            # Repository implementations
│
├── domain/                    # Domain layer
│   └── util/                  # Business logic utilities
│
├── presentation/              # Presentation layer (UI)
│   ├── components/            # Reusable Compose components
│   ├── navigation/            # Navigation graph
│   ├── screens/               # UI screens
│   │   ├── main/              # Main screen with sections
│   │   ├── onboarding/        # Onboarding flow
│   │   └── tools/             # Tool screens
│   ├── theme/                 # Material 3 theming
│   └── viewmodel/             # ViewModels
│
├── di/                        # Dependency injection
├── notification/              # Notification handling
├── receiver/                  # Broadcast receivers
└── worker/                    # WorkManager workers
```

### Tech Stack

- **UI Framework**: Jetpack Compose (100% Compose UI)
- **Language**: Kotlin (100%)
- **Architecture**: MVVM + Repository Pattern
- **Dependency Injection**: Koin
- **Database**: Room
- **Async**: Kotlin Coroutines + StateFlow
- **Navigation**: Jetpack Navigation Compose
- **Background Work**: WorkManager
- **Charts**: MPAndroidChart Reworked
- **Logging**: Timber

---

## Requirements

- **Minimum SDK**: API 24 (Android 7.0 Nougat)
- **Target SDK**: API 36 (Android 15)
- **Compile SDK**: API 36
- **Gradle**: 8.13
- **AGP**: 8.13.2
- **Kotlin**: 2.3.10
- **Java**: 17

### Permissions

- `RECEIVE_BOOT_COMPLETED` - Start monitoring after device restart
- `POST_NOTIFICATIONS` - Show battery alarms
- `PACKAGE_USAGE_STATS` - Screen time statistics (optional)

**Note:** This app is 100% offline and does not require internet permission.

---

## Build & Run

To build and run the project, follow these steps:

1. Clone the repository:
   ```bash
   git clone https://github.com/ahmmedrejowan/Chargify.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Connect your Android device or start an emulator.
5. Click on the "Run" button in Android Studio to build and run the app.

---

## Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

**Note**: Test coverage is currently being developed and will be added in future releases.

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Development Guidelines

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Follow the existing code style and architecture
4. Add tests for new features (when test infrastructure is ready)
5. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
6. Push to the branch (`git push origin feature/AmazingFeature`)
7. Open a Pull Request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused
- Follow MVVM architecture principles

---

## License

```
Copyright (C) 2026 K M Rejowan Ahmmed

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public
License along with this program. If not,
see <https://www.gnu.org/licenses/>.
```

> [!WARNING]
> **This is a copyleft license.** Chargify is licensed under GPL v3.0, which means:
> - You can freely use, modify, and distribute this software
> - Any derivative works **must also be licensed under GPL v3.0**
> - You **must disclose your source code** if you distribute modified versions
> - You **cannot distribute proprietary/closed-source versions** of this software
>
> If you need different licensing terms, please contact the author.

---

## Author

**K M Rejowan Ahmmed**

- GitHub: [@ahmmedrejowan](https://github.com/ahmmedrejowan)
- Email: [ahmmadrejowan@gmail.com](mailto:ahmmadrejowan@gmail.com)

---

## Acknowledgments

- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - Charting library
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern Android UI toolkit
- [Material Design 3](https://m3.material.io/) - Design system
- [Koin](https://insert-koin.io/) - Dependency injection framework

---

## Changelog

### v1.0.0 (2026-02-20) - Initial Release

- Real-time battery monitoring with live charts
- Charging history tracking with session statistics
- Smart battery alarms (full charge, low battery, custom)
- Screen time and app usage analytics
- Battery tips and recommendations
- Material 3 design with dark mode and dynamic colors
- 100% offline functionality

---

