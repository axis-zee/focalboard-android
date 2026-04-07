#!/bin/bash
# Build Focalboard Android in Docker (x86_64 container on ARM64 host)
# This works around the AAPT2 x86 binary issue on ARM64

set -e

PROJECT_DIR=/home/zaytun/.openclaw/workspace/focalboard-android
OUTPUT_DIR=$PROJECT_DIR/app/build/outputs/apk

# Create output directory
mkdir -p $OUTPUT_DIR

# Create build script for Docker
cat > /tmp/build-android.sh << 'DOCKERSCRIPT'
#!/bin/bash
set -e

# Install dependencies
apt-get update && apt-get install -y unzip curl

# Set up Android SDK
export ANDROID_HOME=/usr/local/android-sdk
mkdir -p $ANDROID_HOME/cmdline-tools/latest
mkdir -p $ANDROID_HOME/licenses

# Download and install Android command-line tools
cd $ANDROID_HOME/cmdline-tools
curl -sL https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -o cmdline-tools.zip
unzip -q cmdline-tools.zip
mv cmdline-tools latest
rm cmdline-tools.zip

export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin

# Accept licenses
yes | sdkmanager --licenses || true

# Install required SDK components
sdkmanager --batch 'platform-tools' 'platforms;android-34' 'build-tools;34.0.0'

# Run Gradle build
cd /workspace
chmod +x gradlew
./gradlew assembleDebug
DOCKERSCRIPT

chmod +x /tmp/build-android.sh

# Build in Docker
docker run --rm \
    -v $PROJECT_DIR:/workspace \
    -v /tmp/build-android.sh:/build.sh \
    -w /workspace \
    gradle:8.2-jdk17 \
    /build.sh

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
