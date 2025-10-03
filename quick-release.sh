#!/bin/bash
# Quick release script for BaseAds library

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🚀 BaseAds Quick Release${NC}"
echo "========================="

# Show current version
CURRENT_VERSION=$(grep -E 'versionName\s*=' base-ads/build.gradle.kts | sed 's/.*versionName = "\(.*\)".*/\1/')
echo -e "${YELLOW}Current version: ${CURRENT_VERSION}${NC}"

echo -e "${BLUE}Choose release type:${NC}"
echo "1. Patch (e.g., 1.0.0 → 1.0.1)"
echo "2. Minor (e.g., 1.0.1 → 1.1.0)"  
echo "3. Major (e.g., 1.1.0 → 2.0.0)"
echo "4. Custom version"
echo "5. Build current version"
echo "6. Exit"

echo -e "${BLUE}Enter choice [1-6]:${NC}"
read CHOICE

case $CHOICE in
    1)
        # Patch version
        NEW_VERSION=$(echo $CURRENT_VERSION | awk -F. '{$NF = $NF + 1;} 1' | sed 's/ /./g')
        ;;
    2)
        # Minor version  
        NEW_VERSION=$(echo $CURRENT_VERSION | awk -F. '{$(NF-1) = $(NF-1) + 1; $NF = 0;} 1' | sed 's/ /./g')
        ;;
    3)
        # Major version
        NEW_VERSION=$(echo $CURRENT_VERSION | awk -F. '{$1 = $1 + 1; $2 = 0; $3 = 0;} 1' | sed 's/ /./g')
        ;;
    4)
        # Custom version
        echo -e "${BLUE}Enter new version:${NC}"
        read NEW_VERSION
        ;;
    5)
        # Build current version
        NEW_VERSION=$CURRENT_VERSION
        ;;
    6)
        echo -e "${YELLOW}Goodbye! 👋${NC}"
        exit 0
        ;;
    *)
        echo -e "${RED}Invalid choice${NC}"
        exit 1
        ;;
esac

if [ "$NEW_VERSION" != "$CURRENT_VERSION" ]; then
    echo -e "${BLUE}Updating version from ${CURRENT_VERSION} to ${NEW_VERSION}...${NC}"
    
    # Update version code
    CURRENT_CODE=$(grep -E 'versionCode\s*=' base-ads/build.gradle.kts | sed 's/.*versionCode = \(.*\).*/\1/')
    NEW_CODE=$((CURRENT_CODE + 1))
    
    # Update version in build.gradle.kts
    sed -i '' "s/versionName = \"$CURRENT_VERSION\"/versionName = \"$NEW_VERSION\"/g" base-ads/build.gradle.kts
    sed -i '' "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/g" base-ads/build.gradle.kts
    
    echo -e "${GREEN}✅ Version updated to ${NEW_VERSION} (code: ${NEW_CODE})${NC}"
fi

echo -e "${BLUE}🏗️ Building AAR for version ${NEW_VERSION}...${NC}"
./build-aar.sh

echo ""
echo -e "${GREEN}🎉 Release Complete!${NC}"
echo "========================="
echo -e "${GREEN}📦 Version: ${NEW_VERSION}${NC}"
echo -e "${GREEN}📁 Files: exported-aar/base-ads-v${NEW_VERSION}-*.aar${NC}"
echo -e "${GREEN}📤 Package: baseads-library.zip${NC}"
echo ""
echo -e "${BLUE}📋 Next steps:${NC}"
echo "• Test the AAR in a sample project"
echo "• Update documentation if needed"
echo "• Distribute to your team"
echo "• Commit version changes to git"
echo ""
echo -e "${BLUE}Git commands:${NC}"
echo "  git add base-ads/build.gradle.kts"
echo "  git commit -m \"Release v${NEW_VERSION}\""
echo "  git tag v${NEW_VERSION}"
echo ""
echo -e "${GREEN}Happy releasing! 🚀✨${NC}"