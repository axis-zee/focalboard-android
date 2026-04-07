#!/bin/bash
# Setup Android SDK command-line tools for building the Focalboard Android app
# This script is idempotent - safe to run multiple times

set -e

ANDROID_HOME=/home/zaytun/Android/Sdk
ANDROID_CMDLINE_TOOLS_VERSION="11076708"

echo "📱 Setting up Android SDK..."

# Create directory structure
mkdir -p "$ANDROID_HOME/cmdline-tools"
mkdir -p "$ANDROID_HOME/licenses"

# Download command-line tools if not present
if [ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
    echo "⬇️  Downloading Android command-line tools..."
    cd "$ANDROID_HOME/cmdline-tools"
    curl -sL "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS_VERSION}_latest.zip" -o cmdline-tools.zip
    unzip -q cmdline-tools.zip
    mv cmdline-tools latest
    rm cmdline-tools.zip
else
    echo "✅ Android command-line tools already installed"
fi

export ANDROID_HOME
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

# Accept licenses
echo "📝 Accepting Android SDK licenses..."
yes | sdkmanager --licenses || true

# Install required SDK components
echo "📦 Installing SDK components..."
sdkmanager --install "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo "✅ Android SDK setup complete!"
echo "   SDK location: $ANDROID_HOME"
echo "   Add to ~/.bashrc:"
echo "     export ANDROID_HOME=$ANDROID_HOME"
echo "     export PATH=\$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin"
