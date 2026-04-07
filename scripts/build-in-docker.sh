#!/bin/bash
# Build Focalboard Android in Docker using x86_64 emulation
# This works around the AAPT2 x86 binary issue on ARM64

set -e

PROJECT_DIR=/home/zaytun/.openclaw/workspace/focalboard-android
OUTPUT_DIR=$PROJECT_DIR/app/build/outputs/apk

# Create output directory
mkdir -p $OUTPUT_DIR

# Use x86_64 architecture with QEMU emulation
docker run --rm \
    --platform linux/amd64 \
    -v $PROJECT_DIR:/workspace \
    -w /workspace \
    gradle:8.2-jdk17 \
    bash -c "
        # Install dependencies
        apt-get update && apt-get install -y unzip curl wget

        # Set up Android SDK
        export ANDROID_HOME=/usr/local/android-sdk
        mkdir -p \$ANDROID_HOME/cmdline-tools
        mkdir -p \$ANDROID_HOME/licenses

        # Download Android command-line tools (x86_64 version)
        cd \$ANDROID_HOME/cmdline-tools
        wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O cmdline-tools.zip
        unzip -q cmdline-tools.zip
        mv cmdline-tools latest
        rm cmdline-tools.zip

        export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin

        # Accept licenses
        yes | sdkmanager --licenses 2>/dev/null || true

        # Install required SDK components
        sdkmanager --batch 'platform-tools' 'platforms;android-34' 'build-tools;34.0.0'

        # Run Gradle build
        cd /workspace
        chmod +x gradlew
        ./gradlew assembleDebug --no-daemon
    "

# Check for APK
if [ -f "$OUTPUT_DIR/debug/app-debug.apk" ]; then
    echo ""
    echo "✅ Build successful!"
    echo "📦 APK location: $OUTPUT_DIR/debug/app-debug.apk"
    echo "📊 APK size: $(ls -lh $OUTPUT_DIR/debug/app-debug.apk | awk '{print $5}')"
else
    echo ""
    echo "❌ Build failed - no APK found"
    exit 1
fi
