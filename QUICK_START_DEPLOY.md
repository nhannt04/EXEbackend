# 🚀 HISTRA Backend - QUICK START Deploy to Render

## ✅ Công Việc Đã Hoàn Thành

Tôi đã chuẩn bị tất cả các file cần thiết:

### Files được tạo/update:
```
✅ src/main/resources/application.yml
   → Loại bỏ hardcoded credentials
   → Chỉ dùng environment variables

✅ Procfile
   → Render sẽ dùng để start application

✅ .env.example
   → Template cho environment variables

✅ .gitignore
   → Ngăn commit secret files

✅ DEPLOY_RENDER.md
   → Hướng dẫn chi tiết (Vietnamese)

✅ DEPLOY_CHECKLIST.md
   → Danh sách kiểm tra (Vietnamese)

✅ start.sh
   → Deploy script (nếu cần)
```

---

## 🎯 3 Bước Cuối Cùng Để Deploy

### BƯỚC 1️⃣: GIT COMMIT & PUSH

```bash
# Mở terminal, chuyển vào thư mục project
cd E:\EXEbackend

# Xem những file thay đổi
git status

# Thêm tất cả file mới
git add .

# Commit với message
git commit -m "Prepare for Render deployment - use environment variables for security"

# Push lên GitHub
git push origin main
```

✅ **Xác nhận:** Mở GitHub, kiểm tra `main` branch có các file mới này không

---

### BƯỚC 2️⃣: TẠO RENDER ACCOUNT & WEB SERVICE

**2.1 - Tạo Render Account:**
```
1. Truy cập: https://dashboard.render.com
2. Click "Sign up with GitHub"
3. Approve permissions
```

**2.2 - Tạo Web Service:**
```
1. Click "New" → "Web Service"
2. Click "Connect account" (kết nối GitHub)
3. Tìm & chọn repository: "EXEbackend"
4. Click "Connect"
```

**2.3 - Cấu hình Service:**
```
Tên:                histra-backend
Environment:        Docker
Branch:             main
Build Command:      mvn clean install -DskipTests
Start Command:      java -jar target/histra-backend-0.0.1-SNAPSHOT.jar
Region:             Singapore
Plan:               Free (nếu test) hoặc Starter ($7/mo)
```

---

### BƯỚC 3️⃣: THÊM ENVIRONMENT VARIABLES

**ĐẶC BIỆT QUAN TRỌNG: Bạn cần chuẩn bị credentials mới**

#### 3.1 - Database (Neon)

Tạo user mới trên Neon:
```
1. Truy cập: https://console.neon.tech
2. Chọn project → Settings → Roles
3. Tạo user mới (ví dụ: render_user)
4. Copy connection string
   Format: jdbc:postgresql://host:5432/neondb?sslmode=require
```

#### 3.2 - Cloudflare R2

Tạo API token mới:
```
1. Truy cập: https://dash.cloudflare.com
2. Account → API Tokens → Create Token
3. R2 API - Permissions: R2 (specific bucket)
4. Copy: Account ID, Access Key, Secret Key
```

#### 3.3 - JWT Secret

Tạo secret mới:
```bash
# Sử dụng OpenSSL
openssl rand -base64 32

# Hoặc online: https://www.random.org/strings/
# Tối thiểu 32 ký tự random
```

#### 3.4 - Thêm vào Render Dashboard

Trong Render → Web Service → Environment:

```
DATABASE_URL=jdbc:postgresql://[host]:5432/neondb?sslmode=require
DB_USERNAME=render_user
DB_PASSWORD=[new_password_from_neon]

CLOUDFLARE_R2_ACCOUNT_ID=[account_id]
CLOUDFLARE_R2_ACCESS_KEY=[access_key]
CLOUDFLARE_R2_SECRET_KEY=[secret_key]
CLOUDFLARE_R2_BUCKET_NAME=travel
CLOUDFLARE_R2_ENDPOINT=https://[account_id].r2.cloudflarestorage.com
CLOUDFLARE_R2_CUSTOM_DOMAIN=https://cdn.example.com

JWT_SECRET=[new_random_32_char_key]
```

✅ **Kiểm tra:** Tất cả 10 variables phải được set

---

## 🎬 DEPLOY!

Sau khi hoàn thành 3 bước trên:

```
1. Render Dashboard → Web Service: histra-backend
2. Click "Create Web Service" (nếu chưa)
3. Hoặc click "Deploy" (nếu có sẵn)
4. Chờ build hoàn tất (~5-10 phút)
5. Status thay đổi thành "Live" ✅
6. Copy URL: https://histra-backend-xxxx.onrender.com
```

---

## ✨ TEST DEPLOYMENT

Sau khi deploy thành công:

```bash
# Test health
curl https://histra-backend-xxxx.onrender.com/actuator/health

# Hoặc sử dụng Postman/Insomnia
# Test API endpoints của bạn
```

**Kiểm tra Logs:**
```
Render Dashboard → Logs → Tìm:
✅ "Flyway successfully validated X migrations"
✅ "Started HistraBackendApplication"
❌ Không có lỗi database/cloudflare
```

---

## 🆘 CẦN HELP?

Nếu deploy không thành công:

1. **Check logs** trong Render Dashboard
2. **Verify environment variables** có đúng không
3. **Test database** connection locally:
   ```bash
   # Tạo .env file local
   # Set DATABASE_URL, DB_USERNAME, DB_PASSWORD
   # Run: mvn spring-boot:run
   ```
4. **Kiểm tra** Cloudflare R2 credentials

---

## 📚 TÀI LIỆU CHI TIẾT

- **Full Deploy Guide:** `DEPLOY_RENDER.md`
- **Checklist:** `DEPLOY_CHECKLIST.md`
- **Environment Template:** `.env.example`
- **Render Docs:** https://render.com/docs

---

## 🎉 Sau Khi Deploy

✅ Auto-deploy hoạt động - mỗi `git push` tự động deploy  
✅ Logs theo dõi trong Render Dashboard  
✅ Setup custom domain (tùy chọn)  
✅ Monitor metrics (CPU, Memory)  
✅ Setup alerts (nếu cần)  

---

**Status: READY TO DEPLOY ✅**

👉 **Tiếp theo:** Thực hiện 3 bước ở trên!

Questions? Check `DEPLOY_RENDER.md` hoặc `DEPLOY_CHECKLIST.md`

