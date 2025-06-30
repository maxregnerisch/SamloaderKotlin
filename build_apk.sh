#!/bin/bash

# 3D Labyrinth Game - Build Script
# This script builds the Android APK for the 3D Labyrinth Game

echo "🎮 Building 3D Labyrinth Game APK..."
echo "=================================="

# Check if we're in the right directory
if [ ! -f "settings.gradle" ]; then
    echo "❌ Error: Please run this script from the project root directory"
    exit 1
fi

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build debug APK
echo "🔨 Building debug APK..."
./gradlew assembleDebug

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "📱 Debug APK location:"
    echo "   app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "🚀 To install on connected device:"
    echo "   adb install app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📋 APK Info:"
    ls -lh app/build/outputs/apk/debug/app-debug.apk 2>/dev/null || echo "   APK file not found"
else
    echo "❌ Build failed!"
    echo "Please check the error messages above."
    exit 1
fi

echo ""
echo "🎯 Game Features:"
echo "   • 3D OpenGL ES graphics"
echo "   • Accelerometer controls"
echo "   • Coins, enemies, bombs, math questions"
echo "   • Lives system and high scores"
echo "   • Pause/resume functionality"
echo ""
echo "Happy gaming! 🎮✨"

