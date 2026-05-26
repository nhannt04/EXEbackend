#!/bin/bash
# Deploy script for HISTRA Backend to Render

echo "╔════════════════════════════════════════════╗"
echo "║  HISTRA Backend - Render Deploy Script    ║"
echo "╚════════════════════════════════════════════╝"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo -e "${RED}❌ Git is not installed. Please install Git first.${NC}"
    exit 1
fi

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}❌ pom.xml not found. Please run this script from the project root.${NC}"
    exit 1
fi

echo -e "${BLUE}📦 Step 1: Checking Git status...${NC}"
git status

echo -e "\n${BLUE}📝 Step 2: Adding files...${NC}"
git add .
echo -e "${GREEN}✅ Files added${NC}"

echo -e "\n${BLUE}💬 Step 3: Enter commit message${NC}"
read -p "Commit message (default: 'Deploy to Render'): " COMMIT_MSG
COMMIT_MSG=${COMMIT_MSG:-"Deploy to Render"}

git commit -m "$COMMIT_MSG"

echo -e "\n${BLUE}🚀 Step 4: Pushing to GitHub...${NC}"
git push origin main

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Code pushed successfully!${NC}"
    echo -e "\n${YELLOW}📋 Next steps:${NC}"
    echo "1. Go to https://dashboard.render.com"
    echo "2. Click 'New +' → 'Web Service'"
    echo "3. Select your GitHub repository"
    echo "4. Configure environment variables"
    echo "5. Click 'Create Web Service'"
    echo ""
    echo -e "${YELLOW}📚 Documentation:${NC}"
    echo "- Quick guide: QUICK_DEPLOY.md"
    echo "- Detailed guide: RENDER_DEPLOYMENT.md"
    echo "- Project info: PROJECT_OVERVIEW.md"
else
    echo -e "${RED}❌ Push failed. Check your GitHub connection.${NC}"
    exit 1
fi

