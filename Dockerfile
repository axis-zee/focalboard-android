FROM eclipse-temurin:17-jdk

# Install dependencies
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    file \
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

# No AAPT2 wrapper needed - running on native amd64

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Clear Gradle cache completely
RUN rm -rf /root/.gradle/caches/transforms-* \
                  /root/.gradle/wrapper/dists \
                  /root/.gradle/caches/modules-* \
                  /root/.gradle/caches/jars-* \
                  2>/dev/null || true

# Build the APK - force use of SDK AAPT2
RUN chmod +x ./gradlew && \
    ./gradlew assembleDebug --no-daemon --warning-mode=all

# Copy output
RUN cp app/build/outputs/apk/debug/app-debug.apk /app-debug.apk

CMD ["/bin/bash"]
