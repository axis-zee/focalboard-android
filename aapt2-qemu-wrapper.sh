#!/bin/sh
# QEMU wrapper for x86_64 AAPT2 on ARM64
exec /usr/bin/qemu-x86_64 "$1" "${@:2}"
