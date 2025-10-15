#!/bin/bash

# Mediation Debug Script
# Kiểm tra adapter status và mediation logs

echo "🔍 Base Ads - Mediation Debug Tool"
echo "=================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo -e "${RED}❌ No device connected${NC}"
    echo "Please connect a device or emulator"
    exit 1
fi

echo -e "${GREEN}✅ Device connected${NC}"
echo ""

# Clear logcat
adb logcat -c

echo "Starting app and monitoring adapter status..."
echo "Press Ctrl+C to stop"
echo ""

# Monitor specific tags
adb logcat -s Initializer:I Initializer:E Initializer:W \
             Ads:D Ads:I Ads:E \
             MetaBidding:I IronSourceBidding:I VungleBidding:I \
             | grep -E "(Adapter|READY|NOT_READY|bidding|Mediation|initialized)" \
             | while read line; do
    
    # Color coding
    if echo "$line" | grep -q "READY"; then
        echo -e "${GREEN}$line${NC}"
    elif echo "$line" | grep -q "NOT_READY"; then
        echo -e "${RED}$line${NC}"
    elif echo "$line" | grep -q "initialized"; then
        echo -e "${BLUE}$line${NC}"
    elif echo "$line" | grep -q "failed"; then
        echo -e "${RED}$line${NC}"
    else
        echo "$line"
    fi
done
