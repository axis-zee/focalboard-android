FROM eclipse-temurin:17-jdk

# Install dependencies including QEMU for x86_64 binary compatibility
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    file \
    qemu-user-static \
    && rm -rf /var/lib/apt/lists/*

# Set up Android SDK
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/34.0.0

RUN mkdir -p $ANDROID_HOME/cmdline-tools
RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d $ANDROID_HOME/cmdline-tools && \
    mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# Accept licenses
RUN yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses > /dev/null 2>&1 || true

# Install required SDK components
RUN $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Remove any cached AAPT2 references to force Maven AAPT2
RUN rm -rf /usr/lib/android-sdk 2>/dev/null || true

# Create QEMU wrapper for x86_64 AAPT2
RUN mkdir -p /usr/local/bin && \
    echo '#!/bin/sh' > /usr/local/bin/aapt2-wrapper && \
    echo 'exec /usr/bin/qemu-x86_64 "$@"' >> /usr/local/bin/aapt2-wrapper && \
    chmod +x /usr/local/bin/aapt2-wrapper

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the APK
RUN chmod +x ./gradlew && \
    ./gradlew assembleDebug --no-daemon

# Copy output
RUN cp app/build/outputs/apk/debug/app-debug.apk /app-debug.apk

CMD ["/bin/bash"]
