# 3D Labyrinth Game 🎮

A modern 3D labyrinth game for Android with accelerometer controls, featuring coins, enemies, bombs, and math challenges!

## Features ✨

- **3D Graphics**: OpenGL ES 2.0 powered 3D maze environment
- **Accelerometer Control**: Tilt your device to navigate the ball through the maze
- **Game Elements**:
  - 🎯 **Coins**: Collect yellow coins for points (+10 points each)
  - 👾 **Enemies**: Avoid red moving enemies that patrol the maze
  - 💣 **Bombs**: Dodge black bombs scattered throughout the maze
  - 🧮 **Math Questions**: Solve math problems for bonus points (+20 points each)
- **Lives System**: Start with 3 lives, lose one when touching enemies or bombs
- **Pause/Resume**: Pause the game anytime with the pause button
- **High Score**: Track your best performance
- **Haptic Feedback**: Vibration feedback for collisions and interactions
- **Modern UI**: Beautiful gradient backgrounds and smooth animations

## How to Play 🎯

1. **Movement**: Tilt your Android device to roll the golden ball through the maze
2. **Objective**: Collect as many coins as possible while avoiding dangers
3. **Scoring**:
   - Coins: +10 points
   - Math Questions: +20 points
4. **Survival**: Avoid red enemies and black bombs - they cost you a life!
5. **Game Over**: When you lose all 3 lives, your final score is saved

## Technical Features 🔧

- **OpenGL ES 2.0**: Hardware-accelerated 3D graphics
- **Sensor Integration**: Real-time accelerometer input
- **Physics Engine**: Realistic ball physics with gravity, friction, and collision detection
- **Dynamic Lighting**: 3D shaded objects and surfaces
- **Optimized Rendering**: Efficient vertex buffers and shader programs
- **Memory Management**: Proper OpenGL resource cleanup

## Build Instructions 🛠️

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK API 24+ (Android 7.0)
- Device with accelerometer sensor

### Building the APK

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd Labyrinth3D
   ```

2. **Open in Android Studio**:
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the project folder

3. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

4. **Build Release APK**:
   ```bash
   ./gradlew assembleRelease
   ```

5. **Install on Device**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Command Line Build (Alternative)
```bash
# Clean and build
./gradlew clean
./gradlew assembleDebug

# For release build (requires signing configuration)
./gradlew assembleRelease
```

## Game Architecture 🏗️

### Core Components

- **GameEngine**: Main game logic, physics, and state management
- **GameRenderer**: OpenGL ES rendering pipeline
- **GameGLSurfaceView**: Custom OpenGL surface view with touch handling
- **GameObject**: Base class for all 3D game objects
- **Labyrinth**: 3D maze structure with walls and floor
- **Game Objects**:
  - `Ball`: Player-controlled sphere
  - `Coin`: Collectible items
  - `Enemy`: Moving hostile entities
  - `Bomb`: Static dangerous objects
  - `MathQuestion`: Interactive math challenges

### Physics System
- Accelerometer-based tilt controls
- Realistic ball physics with momentum
- Wall collision detection and response
- Friction and gravity simulation

## Permissions 📱

- `android.permission.VIBRATE`: For haptic feedback on collisions

## Compatibility 📋

- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Architecture**: ARM64, ARM32
- **Orientation**: Landscape mode (locked)
- **Requirements**: Accelerometer sensor

## Screenshots 📸

*Add screenshots of your game here*

## Future Enhancements 🚀

- Multiple maze levels with increasing difficulty
- Power-ups and special abilities
- Multiplayer support
- Leaderboards and achievements
- Sound effects and background music
- Customizable ball skins
- Time-based challenges

## Contributing 🤝

Feel free to contribute to this project by:
- Reporting bugs
- Suggesting new features
- Submitting pull requests
- Improving documentation

## License 📄

This project is open source. Feel free to use and modify as needed.

---

**Enjoy navigating the 3D labyrinth! 🎮✨**

