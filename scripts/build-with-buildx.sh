#!/bin/bash
# Build Focalboard Android APK using Docker Buildx with QEMU emulation

set -e

PROJECT_DIR=/home/zaytun/.openclaw/workspace/focalboard-android
OUTPUT_APK=$PROJECT_DIR/app-debug.apk

cd $PROJECT_DIR

# Create output directory
mkdir -p app/build/outputs/apk/debug

# Build with x86_64 emulation
echo "🔨 Building APK with Docker Buildx (x86_64 emulation)..."
docker buildx build \
    --platform linux/amd64 \
    --load \
    -t focalboard-android-apk \
    -f Dockerfile \
    .

# Extract APK from container
echo "📦 Extracting APK..."
docker create --name temp-apk-container focalboard-android-apk
docker cp temp-apk-container:/output/app-debug.apk $OUTPUT_APK
docker rm temp-apk-container

# Clean up
docker rmi focalboard-android-apk 2>/dev/null || true

# Show result
if [ -f "$OUTPUT_APK" ]; then
    echo ""
    echo "✅ Build successful!"
    echo "📦 APK location: $OUTPUT_APK"
    echo "📊 APK size: $(ls -lh $OUTPUT_APK | awk '{print $5}')"
    echo "📍 Also copied to: $PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
    
    # Copy to standard location
    cp $OUTPUT_APK $PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk
else
    echo ""
    echo "❌ Build failed - no APK found"
    exit 1
fi
