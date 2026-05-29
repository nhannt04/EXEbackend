# Travelist Backend - Render Deploy Script (PowerShell)
# Run this script from the project root directory

Write-Host "╔════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  Travelist Backend - Render Deploy Script  ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Check if git is installed
try {
    git --version | Out-Null
} catch {
    Write-Host "❌ Git is not installed. Please install Git first." -ForegroundColor Red
    exit 1
}

# Check if we're in the right directory
if (-not (Test-Path "pom.xml")) {
    Write-Host "❌ pom.xml not found. Please run this script from the project root." -ForegroundColor Red
    exit 1
}

# Step 1: Check Git Status
Write-Host "📦 Step 1: Checking Git status..." -ForegroundColor Blue
git status

# Step 2: Add Files
Write-Host "`n📝 Step 2: Adding files..." -ForegroundColor Blue
git add .
Write-Host "✅ Files added" -ForegroundColor Green

# Step 3: Commit
Write-Host "`n💬 Step 3: Commit message" -ForegroundColor Blue
$commitMsg = Read-Host "Enter commit message (default: 'Deploy to Render')"
if ([string]::IsNullOrWhiteSpace($commitMsg)) {
    $commitMsg = "Deploy to Render"
}

git commit -m "$commitMsg"

# Step 4: Push
Write-Host "`n🚀 Step 4: Pushing to GitHub..." -ForegroundColor Blue
git push origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Code pushed successfully!" -ForegroundColor Green

    Write-Host "`n📋 Next steps:" -ForegroundColor Yellow
    Write-Host "1. Go to https://dashboard.render.com"
    Write-Host "2. Click 'New +' → 'Web Service'"
    Write-Host "3. Select your GitHub repository"
    Write-Host "4. Configure environment variables"
    Write-Host "5. Click 'Create Web Service'"
    Write-Host ""

    Write-Host "📚 Documentation:" -ForegroundColor Yellow
    Write-Host "- Quick guide: QUICK_DEPLOY.md"
    Write-Host "- Detailed guide: RENDER_DEPLOYMENT.md"
    Write-Host "- Project info: PROJECT_OVERVIEW.md"
} else {
    Write-Host "❌ Push failed. Check your GitHub connection." -ForegroundColor Red
    exit 1
}

