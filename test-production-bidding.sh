#!/bin/bash

# Quick Test: Production Mode Bidding

echo "🎯 Testing Production Mode Bidding"
echo "===================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}⚠️  WARNING: This will serve REAL ads${NC}"
echo -e "${YELLOW}⚠️  Do NOT click on ads excessively${NC}"
echo -e "${YELLOW}⚠️  Policy violation can result in AdMob suspension${NC}"
echo ""
read -p "Continue? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    echo "Aborted."
    exit 1
fi

echo ""
echo "Step 1: Enabling production mode..."
echo "------------------------------------"

# Check if BaseAdsApplication has production mode enabled
if grep -q "initializeProductionMode" app/src/main/java/com/tinhtx/baseads/BaseAdsApplication.kt; then
    echo -e "${GREEN}✅ Production mode code exists${NC}"
else
    echo -e "${RED}❌ Production mode not found in BaseAdsApplication${NC}"
    exit 1
fi

echo ""
echo "Step 2: Uncommenting production mode override..."
echo "-------------------------------------------------"

# Uncomment the manual override
sed -i.bak 's|// adsInitializer.initializeProductionMode|adsInitializer.initializeProductionMode|g' \
    app/src/main/java/com/tinhtx/baseads/BaseAdsApplication.kt

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Production mode enabled${NC}"
else
    echo -e "${RED}❌ Failed to enable production mode${NC}"
    exit 1
fi

echo ""
echo "Step 3: Building APK..."
echo "----------------------"

./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Build successful${NC}"
else
    echo -e "${RED}❌ Build failed${NC}"
    # Restore backup
    mv app/src/main/java/com/tinhtx/baseads/BaseAdsApplication.kt.bak \
       app/src/main/java/com/tinhtx/baseads/BaseAdsApplication.kt
    exit 1
fi

echo ""
echo "Step 4: Installing APK..."
echo "------------------------"

adb install -r app/build/outputs/apk/debug/app-debug.apk

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Installation successful${NC}"
else
    echo -e "${RED}❌ Installation failed${NC}"
    exit 1
fi

echo ""
echo "Step 5: Starting app and monitoring logs..."
echo "-------------------------------------------"

# Launch app
adb shell am start -n com.tinhtx.baseads/.MainActivity

sleep 3

echo ""
echo "Checking adapter status..."
echo ""

# Monitor for adapter status
timeout 10 adb logcat -s Initializer:I Initializer:W | grep -E "(READY|NOT_READY|PRODUCTION MODE)" &

sleep 12

echo ""
echo -e "${GREEN}════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ Setup Complete!${NC}"
echo -e "${GREEN}════════════════════════════════════════${NC}"
echo ""
echo "Next steps:"
echo "1. ✅ Load a few ads (banner/interstitial)"
echo "2. ⏰ Wait 1-2 hours"
echo "3. 📊 Check AdMob Reports → Mediation"
echo "4. ✅ Verify eCPM values appear (not '-')"
echo "5. ✅ Verify match rates > 0%"
echo ""
echo -e "${YELLOW}⚠️  Remember: Don't click on ads excessively!${NC}"
echo ""
echo "To monitor live bidding:"
echo "  ./debug-mediation.sh"
echo ""
echo "To restore test mode:"
echo "  git checkout app/src/main/java/com/tinhtx/baseads/BaseAdsApplication.kt"
echo ""
