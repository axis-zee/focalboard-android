#!/bin/bash
# Cron job for Focalboard Android progress reports
# Run every 30 minutes

cd /home/zaytun/.openclaw/workspace/focalboard-android
./scripts/progress-report.sh 2>&1 | tee -a /home/zaytun/.openclaw/workspace/focalboard-android/progress.log
