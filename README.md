# Hexo

A modern multiplayer Tic-Tac-Toe game for Android, built with Jetpack Compose and Firebase.

## 🎮 Features

- **Online Multiplayer** - Create or join rooms to play with friends in real-time
- **Local Play** - Play offline against a friend on the same device
- **Clean UI** - Modern Material Design 3 interface built with Jetpack Compose
- **Real-time Sync** - Game state synchronized across devices using Firebase Firestore
- **Room Management** - Create custom game rooms with unique codes

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with clean architecture principles
- **Dependency Injection**: Hilt
- **Backend**: Firebase (Firestore + Analytics)
- **Navigation**: Jetpack Navigation Compose
- **Async**: Kotlin Coroutines + Flow
- **Build Tool**: Gradle 9.0.0
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)

## 📁 Project Structure

```
app/src/main/java/com/theseuntaylor/hexo/
├── core/
│   ├── theme/              # App theming (colors, typography, theme)
│   └── composables/        # Reusable UI components
├── data/
│   ├── model/              # Data models (Room, Player, etc.)
│   └── repository/         # Data layer (Firebase & Local repositories)
├── di/                     # Dependency injection modules
├── feature/                # Feature modules
│   ├── landing/            # Landing screen
│   ├── create_room/        # Room creation flow
│   ├── join_room/          # Join room flow
│   └── game/               # Game screen & logic
├── navigation/             # Navigation setup
├── HexoApplication.kt      # Application class
└── MainActivity.kt         # Main activity
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Git

### Setup

1. **Clone the repository**
   ```bash
   git clone git@github.com:theseuntaylor/Hexo.git
   cd Hexo
   ```

2. **Set up Firebase**
   - Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Enable Firestore Database and Analytics
   - Download `google-services.json` and place it in the `app/` directory

3. **Build the app**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on device/emulator**
   - Open the project in Android Studio
   - Click Run or use `./gradlew installDebug`

## 🤖 CI/CD Pipeline

This project uses **GitHub Actions** for automated deployment to the Google Play Store.

### How it works

When you push a version tag (e.g., `v1.2.0`):
1. The pipeline automatically increments `versionCode`
2. Builds a signed release AAB
3. Uploads it to the Play Store production track
4. Commits the version bump back to the repository

### Deploying a new version

```bash
git tag v1.2.0
git push origin v1.2.0
```

The pipeline handles everything else automatically.

### Required GitHub Secrets

The following secrets must be configured in **Settings > Secrets and variables > Actions**:

| Secret Name | Description |
|-------------|-------------|
| `KEYSTORE_BASE64` | Base64-encoded keystore file |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |
| `SERVICE_ACCOUNT_JSON` | Google Play service account JSON key |

## 🎯 How to Play

1. **Create a Room**: Generate a unique room code and share it with a friend
2. **Join a Room**: Enter a room code to join an existing game
3. **Play**: Take turns placing X's and O's on the 3x3 grid
4. **Win**: Get three in a row horizontally, vertically, or diagonally

## 📦 Dependencies

Key libraries used in this project:

- **Jetpack Compose BOM**: 2025.03.00
- **Kotlin BOM**: 2.1.10
- **Firebase BOM**: 33.12.0
- **Hilt**: 2.55
- **Navigation Compose**: 2.8.9
- **Coroutines**: 1.10.1
- **Coil**: 2.7.0 (image loading)
- **Retrofit**: 2.11.0
- **OkHttp**: 4.12.0

## 📄 License

This project is proprietary software. All rights reserved.

## 👤 Author

**Solomon George-Taylor**
- GitHub: [@theseuntaylor](https://github.com/theseuntaylor)

---

Built️ using Jetpack Compose
