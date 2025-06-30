# 3D Labyrinth Game - Installation Guide 🎮

This guide will help you build and install the 3D Labyrinth Game APK on your Android device.

## Quick Start 🚀

### Option 1: Automated Build Script
```bash
# Make the build script executable and run it
chmod +x build_apk.sh
./build_apk.sh
```

### Option 2: Manual Build
```bash
# Clean and build debug APK
./gradlew clean
./gradlew assembleDebug
```

## Prerequisites 📋

### Development Environment
- **Java Development Kit (JDK)**: Version 11 or higher
- **Android SDK**: API level 24+ (Android 7.0)
- **Android Studio** (recommended) or **Android SDK Command Line Tools**

### Device Requirements
- **Android Version**: 7.0 (API 24) or higher
- **Sensors**: Accelerometer (required for tilt controls)
- **Storage**: ~50MB free space
- **RAM**: 2GB+ recommended for smooth gameplay

## Detailed Build Instructions 🛠️

### Step 1: Environment Setup

#### Install Java (if not already installed)
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-11-jdk

# macOS (using Homebrew)
brew install openjdk@11

# Windows
# Download from Oracle or use OpenJDK
```

#### Install Android SDK
1. Download Android Studio from https://developer.android.com/studio
2. Install Android Studio and follow the setup wizard
3. Install SDK Platform for API 24+ through SDK Manager

#### Set Environment Variables
```bash
# Add to your ~/.bashrc or ~/.zshrc
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

### Step 2: Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd Labyrinth3D

# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug
```

### Step 3: Install APK

#### Method 1: ADB Install (Recommended)
```bash
# Enable Developer Options and USB Debugging on your device
# Connect device via USB
adb devices  # Verify device is connected
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### Method 2: Manual Install
1. Copy `app/build/outputs/apk/debug/app-debug.apk` to your device
2. Enable "Install from Unknown Sources" in device settings
3. Open the APK file and install

## Build Variants 📦

### Debug Build (Default)
- Includes debugging information
- Larger file size
- Suitable for testing and development

```bash
./gradlew assembleDebug
```

### Release Build
- Optimized and minified
- Smaller file size
- Requires signing configuration

```bash
./gradlew assembleRelease
```

## Troubleshooting 🔧

### Common Build Issues

#### Issue: "SDK location not found"
**Solution**: Set ANDROID_HOME environment variable
```bash
export ANDROID_HOME=/path/to/your/android/sdk
```

#### Issue: "Java version incompatible"
**Solution**: Use Java 11 or higher
```bash
java -version  # Check current version
# Install Java 11+ if needed
```

#### Issue: "Gradle build failed"
**Solution**: Clean and retry
```bash
./gradlew clean
./gradlew assembleDebug --stacktrace
```

#### Issue: "Device not found"
**Solution**: Enable USB debugging
1. Go to Settings > About Phone
2. Tap "Build Number" 7 times to enable Developer Options
3. Go to Settings > Developer Options
4. Enable "USB Debugging"

### Runtime Issues

#### Issue: "App crashes on startup"
**Possible causes**:
- Device doesn't have accelerometer sensor
- Insufficient RAM or storage
- Android version below 7.0

#### Issue: "Graphics not rendering properly"
**Possible causes**:
- Device doesn't support OpenGL ES 2.0
- GPU driver issues
- Try restarting the app

## APK Information 📱

### Generated APK Details
- **Location**: `app/build/outputs/apk/debug/app-debug.apk`
- **Size**: ~15-25 MB (debug version)
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Permissions**: VIBRATE (for haptic feedback)

### APK Verification
```bash
# Check APK information
aapt dump badging app/build/outputs/apk/debug/app-debug.apk

# Verify APK signature
jarsigner -verify -verbose -certs app/build/outputs/apk/debug/app-debug.apk
```

## Performance Optimization 🚀

### For Better Performance
- Close other apps before playing
- Ensure device has sufficient battery
- Play in a well-lit environment for better sensor accuracy
- Keep device temperature moderate

### Graphics Settings
The game automatically adjusts to your device's capabilities:
- **High-end devices**: Full 3D effects and smooth animations
- **Mid-range devices**: Optimized rendering with good performance
- **Low-end devices**: Simplified graphics for playability

## Development Setup 👨‍💻

### Android Studio Setup
1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the Labyrinth3D folder
4. Wait for Gradle sync to complete
5. Connect device or start emulator
6. Click "Run" button

### Code Structure
```
app/src/main/java/com/labyrinth3d/game/
├── MainActivity.kt          # Main menu
├── GameActivity.kt          # Game screen
├── GameEngine.kt           # Game logic
├── GameRenderer.kt         # OpenGL rendering
├── GameGLSurfaceView.kt    # OpenGL surface
├── GameObject.kt           # Game objects base class
└── Labyrinth.kt           # 3D maze structure
```

## Support 💬

If you encounter any issues:

1. **Check Prerequisites**: Ensure all requirements are met
2. **Clean Build**: Try `./gradlew clean` then rebuild
3. **Check Logs**: Use `adb logcat` to see runtime errors
4. **Device Compatibility**: Verify your device meets minimum requirements
5. **Update SDK**: Ensure Android SDK is up to date

## Next Steps 🎯

After successful installation:
1. Launch the game from your app drawer
2. Grant any requested permissions
3. Calibrate your device by holding it level
4. Start playing and enjoy the 3D labyrinth experience!

---

**Happy Gaming! 🎮✨**

For more information, see the main [README.md](README.md) file.

