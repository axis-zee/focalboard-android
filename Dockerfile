# Dockerfile for building Focalboard Android APK
# Uses x86_64 architecture to avoid AAPT2 ARM64 issues
FROM gradle:8.2-jdk17 AS builder

# Install dependencies
RUN apt-get update && apt-get install -y wget unzip && rm -rf /var/lib/apt/lists/*

# Set up Android SDK
ENV ANDROID_HOME=/usr/local/android-sdk
RUN mkdir -p $ANDROID_HOME/cmdline-tools

# Download and install Android command-line tools
WORKDIR $ANDROID_HOME/cmdline-tools
RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O cmdline-tools.zip && \
    unzip -q cmdline-tools.zip && \
    mv cmdline-tools latest && \
    rm cmdline-tools.zip

ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin

# Accept licenses and install SDK components
RUN yes | sdkmanager --licenses 2>/dev/null || true
RUN sdkmanager 'platform-tools' 'platforms;android-34' 'build-tools;34.0.0'

# Copy project and build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew assembleDebug --no-daemon

# Output stage
FROM alpine:latest
RUN apk add --no-cache tar
WORKDIR /output
COPY --from=builder /workspace/app/build/outputs/apk/debug/app-debug.apk .

CMD ["tar", "czf", "-C", ".", "app-debug.apk"]
