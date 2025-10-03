#!/bin/bash
# Version updater for BaseAds library

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}📝 BaseAds Version Updater${NC}"
echo "=================================="

# Current version
CURRENT_VERSION=$(grep -E 'versionName\s*=' base-ads/build.gradle.kts | sed 's/.*versionName = "\(.*\)".*/\1/')
CURRENT_CODE=$(grep -E 'versionCode\s*=' base-ads/build.gradle.kts | sed 's/.*versionCode = \(.*\).*/\1/')

echo -e "${YELLOW}Current version: ${CURRENT_VERSION} (code: ${CURRENT_CODE})${NC}"

# Ask for new version
echo -e "${BLUE}Enter new version (e.g., 1.0.1):${NC}"
read NEW_VERSION

if [ -z "$NEW_VERSION" ]; then
    echo -e "${YELLOW}No version entered, keeping current version${NC}"
    exit 0
fi

# Auto increment version code
NEW_CODE=$((CURRENT_CODE + 1))

echo -e "${BLUE}New version will be: ${NEW_VERSION} (code: ${NEW_CODE})${NC}"
echo -e "${YELLOW}Continue? [y/N]:${NC}"
read CONFIRM

if [[ $CONFIRM =~ ^[Yy]$ ]]; then
    # Update version in build.gradle.kts
    sed -i '' "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/g" base-ads/build.gradle.kts
    sed -i '' "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/g" base-ads/build.gradle.kts
    
    echo -e "${GREEN}✅ Version updated successfully!${NC}"
    echo -e "${BLUE}Now run: ./build-aar.sh${NC}"
else
    echo -e "${YELLOW}Version update cancelled${NC}"
fi