# 🚀 HISTRA Backend - Hướng Dẫn Deploy lên Render

## ⚠️ BƯỚC 0: Xử Lý Bảo Mật (QUAN TRỌNG)

### Rotate Credentials (Tạo mật khẩu/keys mới)

**1. Database Neon - Tạo mật khẩu mới:**
```
1. Truy cập: https://console.neon.tech
2. Chọn project → Settings → Roles
3. Tạo role mới (ví dụ: neondb_render)
4. Copy connection string mới
5. Format: jdbc:postgresql://[host]/[db]?sslmode=require
```

**2. Cloudflare R2 - Tạo API tokens mới:**
```
1. Truy cập: https://dash.cloudflare.com → Account → API Tokens
2. Create Token → R2 API
3. Permissions: All R2 (specific bucket)
4. Copy credentials: Account ID, Access Key, Secret Key
```

**3. JWT Secret - Tạo key mới:**
```bash
# Sử dụng OpenSSL hoặc online generator
# Tối thiểu 256 bits (32 ký tự)
openssl rand -base64 32
```

---

## 📋 BƯỚC 1: Chuẩn Bị Code

### 1.1 Cập nhật Git
```bash
cd E:\EXEbackend

# Thêm các file mới
git add .env.example Procfile .gitignore

# Xác nhận application.yml đã update
git add src/main/resources/application.yml

git commit -m "Prepare for Render deployment - use environment variables"
git push origin main
```

### 1.2 Kiểm Tra Build Local (Tùy chọn)
```bash
# Build project
mvn clean package -DskipTests

# Kiểm tra JAR file được tạo
ls -la target/histra-backend-0.0.1-SNAPSHOT.jar
```

---

## 🔧 BƯỚC 2: Setup Render

### 2.1 Tạo Render Account
```
1. Truy cập: https://dashboard.render.com
2. Sign up với GitHub
3. Cho phép Render truy cập GitHub repositories
```

### 2.2 Tạo Web Service Mới
```
1. Click "New" → "Web Service"
2. Kết nối GitHub repository: EXEbackend
3. Chọn branch: main
4. Nhấn "Connect"
```

### 2.3 Cấu Hình Web Service

**Thông tin cơ bản:**
```
Name:           histra-backend
Environment:    Docker
Region:         Singapore (gần Việt Nam)
Branch:         main
Plan:           Free (hoặc Starter)
```

**Build Command:**
```
mvn clean install -DskipTests
```

**Start Command:**
```
java -jar target/histra-backend-0.0.1-SNAPSHOT.jar
```

---

## 🔐 BƯỚC 3: Thêm Environment Variables

Trong Render Dashboard → Web Service Settings → Environment:

```
DATABASE_URL = jdbc:postgresql://[host]:[port]/[db]?sslmode=require
DB_USERNAME = [new_username]
DB_PASSWORD = [new_password]

CLOUDFLARE_R2_ACCOUNT_ID = [your_account_id]
CLOUDFLARE_R2_ACCESS_KEY = [your_access_key]
CLOUDFLARE_R2_SECRET_KEY = [your_secret_key]
CLOUDFLARE_R2_BUCKET_NAME = travel
CLOUDFLARE_R2_ENDPOINT = https://[account_id].r2.cloudflarestorage.com
CLOUDFLARE_R2_CUSTOM_DOMAIN = https://cdn.example.com

JWT_SECRET = [your_new_jwt_secret]
```

⚠️ **Không** copy-paste credentials vào GitHub - chỉ set trong Render Dashboard!

---

## 🚀 BƯỚC 4: Deploy

### 4.1 Deploy Tự Động
```
1. Quay lại Render Web Service
2. Click "Create Web Service"
3. Render tự động build & deploy
4. Chờ ~5-10 phút cho build hoàn tất
```

### 4.2 Monitor Build Progress
```
1. Xem logs trong Render Dashboard
2. Chờ status: "Live"
3. Copy URL service: https://histra-backend-xxxx.onrender.com
```

---

## ✅ BƯỚC 5: Kiểm Tra Deployment

### 5.1 Test API Basic
```bash
# Health check
curl https://histra-backend-xxxx.onrender.com/actuator/health

# Hoặc test endpoint cụ thể (nếu có)
curl https://histra-backend-xxxx.onrender.com/api/spots
```

### 5.2 Kiểm Tra Logs
```
1. Render Dashboard → Web Service → Logs
2. Tìm lỗi liên quan đến:
   - Database connection
   - Flyway migrations
   - Cloudflare R2
```

### 5.3 Test Database Connection
```
Kiểm tra trong logs:
- "Flyway successfully validated X migrations"
- Không có lỗi PostgreSQL
```

---

## 🔄 BƯỚC 6: Auto-Deploy từ GitHub (Tùy chọn)

Mỗi khi bạn `git push` lên `main`, Render tự động:
1. Pull code mới
2. Build (`mvn clean install`)
3. Deploy lên production

---

## 📊 Thông Tin Hữu Ích

### Free Plan Limitations
- Ứng dụng tự động "sleep" sau 15 phút không hoạt động
- Cold start: 30-60 giây (lần đầu tiên)
- Nếu cần 24/7, upgrade lên Starter ($7/tháng)

### Performance Tips
- Database pool: Configured tối ưu cho Neon
- Flyway migrations: Tự động chạy lần đầu
- Cloudflare R2: Configuration sẵn sàng

### Monitoring
```
Render Dashboard → Web Service:
- Logs (real-time)
- Metrics (CPU, Memory)
- Auto-restart on crash
```

---

## 🐛 Troubleshooting

### Lỗi: "Build failed"
```
✓ Kiểm tra pom.xml syntax
✓ Kiểm tra Java version (17)
✓ Xem logs chi tiết trong Render
```

### Lỗi: "Database connection timeout"
```
✓ Kiểm tra DATABASE_URL format
✓ Kiểm tra DB_USERNAME, DB_PASSWORD
✓ Kiểm tra Neon IP whitelist (cho phép Render IPs)
```

### Lỗi: "Application crashes after deploy"
```
✓ Xem logs trong Render Dashboard
✓ Kiểm tra environment variables đúng
✓ Kiểm tra Flyway migrations không lỗi
```

### Lỗi: "Cloudflare R2 errors"
```
✓ Kiểm tra CLOUDFLARE_R2_ACCESS_KEY
✓ Kiểm tra CLOUDFLARE_R2_SECRET_KEY
✓ Kiểm tra bucket name: travel
```

---

## 📞 Support & Resources

- **Render Docs:** https://render.com/docs
- **Spring Boot Deployment:** https://spring.io/guides/gs/spring-boot/
- **Flyway Documentation:** https://flywaydb.org/documentation/
- **Neon Database:** https://neon.tech/docs

---

## ✨ Sau Khi Deploy Thành Công

1. **Test Endpoints:**
   - Login/Register
   - Get Spots
   - Upload Files (R2)
   - JWT Authentication

2. **Setup Domain (Tùy chọn):**
   - Thêm custom domain trong Render
   - Configure HTTPS/SSL (miễn phí)

3. **Monitoring:**
   - Setup email alerts
   - Monitor database usage
   - Check API response times

4. **Updates:**
   - Các push tiếp theo tự động deploy
   - Không cần manual intervention

---

**Good luck! 🚀 Nếu có lỗi, check logs trong Render Dashboard nhé!**

