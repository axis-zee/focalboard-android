#!/bin/bash
# Gradle wrapper script
# This is a simplified version - use the real gradlew from Android Studio

GRADLE_USER_HOME=${GRADLE_USER_HOME:-"$HOME/.gradle"}
GRADLE_VERSION=8.2

echo "Gradle wrapper would run here..."
echo "For building, use Android Studio or run:"
echo "  ./gradlew assembleDebug"
echo ""
echo "To get the full gradlew, run in Android Studio:"
echo "  Tools > Android > Sync Project with Gradle Files"
