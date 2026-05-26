# 📋 DEPLOY CHECKLIST - HISTRA Backend trên Render

## ✅ TRƯỚC KHI DEPLOY

### Bảo Mật & Credentials
- [ ] **ROTATE DATABASE CREDENTIALS:**
  - [ ] Tạo user mới trên Neon Database
  - [ ] Copy connection string mới
  - [ ] Xóa old credentials từ mọi nơi
  
- [ ] **ROTATE CLOUDFLARE R2 CREDENTIALS:**
  - [ ] Tạo API token mới
  - [ ] Lưu Account ID, Access Key, Secret Key
  - [ ] Xóa old credentials
  
- [ ] **GENERATE NEW JWT SECRET:**
  - [ ] Tạo secret mới (32+ ký tự)
  - [ ] Không share công khai

### Code Changes
- [ ] `application.yml`: ✅ Đã update (dùng environment variables)
- [ ] `Procfile`: ✅ Đã tạo
- [ ] `.env.example`: ✅ Đã tạo
- [ ] `.gitignore`: ✅ Đã tạo
- [ ] `DEPLOY_RENDER.md`: ✅ Đã tạo
- [ ] `start.sh`: ✅ Đã tạo

### Git Repository
- [ ] Commit tất cả changes: `git add . && git commit -m "Prepare for Render deployment"`
- [ ] Push lên GitHub: `git push origin main`
- [ ] Kiểm tra GitHub repository public/private status

---

## 🔧 QUÁ TRÌNH DEPLOY TRÊN RENDER

### Bước 1: Render Setup
- [ ] Truy cập https://dashboard.render.com
- [ ] Login với GitHub account
- [ ] Cho phép Render truy cập GitHub repos
- [ ] Click "New" → "Web Service"
- [ ] Chọn repository: **EXEbackend**

### Bước 2: Cấu Hình Service
- [ ] **Name:** `histra-backend`
- [ ] **Environment:** `Docker` (hoặc Native)
- [ ] **Region:** `Singapore`
- [ ] **Branch:** `main`
- [ ] **Build Command:** `mvn clean install -DskipTests`
- [ ] **Start Command:** `java -jar target/histra-backend-0.0.1-SNAPSHOT.jar`
- [ ] **Plan:** `Free` (hoặc Starter nếu cần)

### Bước 3: Environment Variables
Thêm tất cả biến trong Render Dashboard:

```
DATABASE_URL = jdbc:postgresql://[host]:[port]/[db]?sslmode=require
DB_USERNAME = [new_username]
DB_PASSWORD = [new_password]
CLOUDFLARE_R2_ACCOUNT_ID = [account_id]
CLOUDFLARE_R2_ACCESS_KEY = [access_key]
CLOUDFLARE_R2_SECRET_KEY = [secret_key]
CLOUDFLARE_R2_BUCKET_NAME = travel
CLOUDFLARE_R2_ENDPOINT = https://[account_id].r2.cloudflarestorage.com
CLOUDFLARE_R2_CUSTOM_DOMAIN = [optional]
JWT_SECRET = [new_jwt_secret]
```

### Bước 4: Deploy
- [ ] Click "Create Web Service"
- [ ] Chờ build hoàn tất (5-10 phút)
- [ ] Status chuyển sang "Live"
- [ ] Copy service URL: `https://histra-backend-xxxx.onrender.com`

---

## ✨ SAU KHI DEPLOY

### Testing
- [ ] Kiểm tra logs trong Render Dashboard
- [ ] Test health endpoint: `curl https://histra-backend-xxxx.onrender.com/actuator/health`
- [ ] Test main API endpoints
- [ ] Kiểm tra database migration chạy thành công
- [ ] Test Cloudflare R2 upload (nếu có endpoint)

### Monitoring
- [ ] Setup log monitoring
- [ ] Check metrics (CPU, Memory, Disk)
- [ ] Verify auto-restart on crash is enabled
- [ ] Setup email notifications (nếu cần)

### Optional: Custom Domain
- [ ] Mua domain (GoDaddy, Namecheap, etc.)
- [ ] Thêm custom domain trong Render
- [ ] Setup DNS records
- [ ] Verify HTTPS/SSL

---

## 🆘 TROUBLESHOOTING

Nếu gặp lỗi:

### Build Failure
```
→ Check logs trong Render Dashboard
→ Kiểm tra pom.xml syntax
→ Verify Java version 17
→ Check internet connection Render
```

### Database Connection Errors
```
→ Verify DATABASE_URL format
→ Check DB_USERNAME, DB_PASSWORD
→ Verify Neon database running
→ Check IP whitelist (Neon dashboard)
```

### Application Crashes
```
→ View Render logs for errors
→ Check all environment variables set
→ Verify Flyway migrations working
→ Check disk space availability
```

### Cloudflare R2 Issues
```
→ Verify credentials correct
→ Check bucket name: "travel"
→ Verify API token permissions
→ Test locally với credentials
```

---

## 📚 USEFUL COMMANDS

```bash
# Build locally để test
mvn clean package -DskipTests

# Run locally (cần set environment variables)
java -jar target/histra-backend-0.0.1-SNAPSHOT.jar

# Test API
curl https://histra-backend-xxxx.onrender.com/api/health

# View recent git changes
git log --oneline -5

# Force rebuild (từ Render Dashboard)
→ Settings → Deploys → click "Deploy"
```

---

## 📞 Important Resources

| Resource | Link |
|----------|------|
| Render Docs | https://render.com/docs |
| Render Troubleshooting | https://render.com/docs/troubleshooting |
| Spring Boot Docs | https://spring.io/guides/gs/spring-boot/ |
| Flyway Docs | https://flywaydb.org/documentation/ |
| Neon Database | https://neon.tech/docs |
| Cloudflare R2 | https://developers.cloudflare.com/r2/ |

---

## 🎯 Final Reminders

1. **🔐 Never commit credentials** to GitHub
2. **✅ Always test locally** before pushing
3. **📝 Keep `.env.example` updated** for team reference
4. **🔄 Auto-deploy works** with every `git push` to main
5. **⏰ Cold starts** take 30-60 seconds (free plan)
6. **💤 Free plan** auto-sleeps after 15 min inactivity

---

**Status:** Ready for Deployment ✅  
**Last Updated:** May 27, 2026  
**Prepared by:** GitHub Copilot

👉 **Next Step:** Follow DEPLOY_RENDER.md hướng dẫn chi tiết!

