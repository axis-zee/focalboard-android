#!/bin/bash
# Build script for Focalboard Android
# Sets up environment and runs Gradle build

set -e

# Set up environment
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-arm64
export ANDROID_HOME=/home/zaytun/Android/Sdk
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH

# Change to project directory
cd /home/zaytun/.openclaw/workspace/focalboard-android

# Run the build command passed as argument, or default to assembleDebug
if [ -z "$1" ]; then
    echo "🔨 Building debug APK..."
    ./gradlew assembleDebug
else
    echo "🔨 Running: $@"
    ./gradlew "$@"
fi

# Show output location
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo "✅ Build successful!"
    echo "📦 APK location: $(pwd)/app/build/outputs/apk/debug/app-debug.apk"
    echo "📊 APK size: $(ls -lh app/build/outputs/apk/debug/app-debug.apk | awk '{print $5}')"
fi
