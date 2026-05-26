#!/bin/bash
# Deploy script for Render
set -e

echo "🔨 Building HISTRA Backend..."
mvn clean install -DskipTests

echo "✅ Build completed!"
echo "📦 Starting application..."

java -jar target/histra-backend-0.0.1-SNAPSHOT.jar

