#!/bin/bash
# Create GitHub Release with APK
# Run this after building the APK

set -e

cd /home/zaytun/.openclaw/workspace/focalboard-android

# Check if GITHUB_TOKEN is set
if [ -z "$GITHUB_TOKEN" ]; then
    echo "❌ GITHUB_TOKEN not set"
    echo ""
    echo "Option 1: Create a Personal Access Token (classic) at:"
    echo "  https://github.com/settings/tokens"
    echo "  - Scope: repo (full control of private repositories)"
    echo "  - Then export it: export GITHUB_TOKEN=your_token"
    echo ""
    echo "Option 2: Create release manually at:"
    echo "  https://github.com/axis-zee/focalboard-android/releases/new"
    echo "  - Tag: v0.1.0 (or next version)"
    echo "  - Upload: app/build/outputs/apk/debug/app-debug.apk"
    exit 1
fi

# Create release
echo "Creating GitHub release..."
RELEASE_RESPONSE=$(curl -s -X POST \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/axis-zee/focalboard-android/releases \
  -d '{
    "tag_name": "v0.1.0",
    "target_commitish": "main",
    "name": "v0.1.0",
    "body": "### v0.1.0 - Initial Release\n\n**New:**\n- Focalboard v2 API support with CSRF protection\n- Login authentication working\n- Secure cookie handling\n\n**Known Issues:**\n- Main screen shows placeholder (boards list coming next)\n\n**Install:**\n1. Enable Unknown sources\n2. Download and install APK\n3. Enter server URL, email, password",
    "draft": false,
    "prerelease": true
  }')

# Extract upload URL
UPLOAD_URL=$(echo "$RELEASE_RESPONSE" | grep -o '"upload_url":"[^"]*"' | cut -d'"' -f4 | sed 's/{?name,description}//')

if [ -z "$UPLOAD_URL" ]; then
    echo "❌ Failed to create release"
    echo "$RELEASE_RESPONSE"
    exit 1
fi

echo "✅ Release created"

# Upload APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK not found at $APK_PATH"
    echo "Run: ./gradlew assembleDebug"
    exit 1
fi

echo "Uploading APK..."
curl -s -X POST \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Content-Type: application/vnd.android.package-archive" \
  --data-binary @"$APK_PATH" \
  "${UPLOAD_URL}?name=app-debug.apk"

echo ""
echo "✅ Release published!"
echo "📱 Update via Obtainium: https://github.com/axis-zee/focalboard-android"
