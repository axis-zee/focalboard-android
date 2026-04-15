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

# Wrap Android SDK aapt2 with QEMU for x86_64 binary on ARM64
RUN mv $ANDROID_HOME/build-tools/34.0.0/aapt2 $ANDROID_HOME/build-tools/34.0.0/aapt2.x86_64 && \
    echo '#!/bin/sh' > $ANDROID_HOME/build-tools/34.0.0/aapt2 && \
    echo 'exec /usr/bin/qemu-x86_64 $ANDROID_HOME/build-tools/34.0.0/aapt2.x86_64 "$@"' >> $ANDROID_HOME/build-tools/34.0.0/aapt2 && \
    chmod +x $ANDROID_HOME/build-tools/34.0.0/aapt2

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the APK
RUN chmod +x ./gradlew && \
    ./gradlew assembleDebug --no-daemon || true

# Wrap Maven AAPT2 with QEMU for x86_64 binary on ARM64
RUN find /root/.gradle/caches -name 'aapt2' -type f 2>/dev/null | while read aapt2_path; do \
    if file "$aapt2_path" | grep -q "x86_64"; then \
        dir=$(dirname "$aapt2_path"); \
        echo "Wrapping $aapt2_path with QEMU"; \
        mv "$aapt2_path" "$aapt2_path.x86_64"; \
        echo '#!/bin/sh' > "$aapt2_path"; \
        echo "exec /usr/bin/qemu-x86_64 $aapt2_path.x86_64 \"\$@\"" >> "$aapt2_path"; \
        chmod +x "$aapt2_path"; \
    fi; \
done

# Retry build with wrapped AAPT2
RUN ./gradlew assembleDebug --no-daemon --rerun-tasks

# Copy output
RUN cp app/build/outputs/apk/debug/app-debug.apk /app-debug.apk

CMD ["/bin/bash"]
