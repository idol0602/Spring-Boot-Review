# 🚀 Quick Start Guide

## Bắt Đầu Nhanh (5 phút)

### 1️⃣ Prerequisites
```bash
# Kiểm tra versions
java -version          # Java 17+
mvn -version          # Maven 3.8+
docker --version      # Docker + Docker Compose
```

### 2️⃣ Setup Database & Cache
```bash
# Khởi động PostgreSQL, Redis, PgAdmin
docker-compose up -d

# Verify services
docker ps
# PostgreSQL: 5432 (postgres/postgres)
# Redis: 6379
# PgAdmin: 5050 (admin@example.com/admin)
```

### 3️⃣ Configure Environment
```bash
# Copy template
cp .env.example .env

# Edit .env (mặc định đã đúng cho docker-compose)
# DB_URL=jdbc:postgresql://localhost:5432/springboot_review
# DB_USERNAME=postgres
# DB_PASSWORD=postgres
# REDIS_HOST=localhost
```

### 4️⃣ Run Application (Service-Based Architecture)
Vì dự án đã được chuyển sang kiến trúc Service-Based (chạy 4 ứng dụng song song), cách duy nhất và dễ nhất để khởi động toàn bộ là dùng Docker.

```bash
# Build mã nguồn và bật TẤT CẢ dịch vụ (Database, Cache, Gateway, User, Order, Product)
docker-compose up -d --build

# Kiểm tra trạng thái các container
docker-compose ps
```

Ứng dụng API Gateway sẽ đứng ra làm cổng chính đón request tại: **http://localhost:8080/api**

---

## 🔑 Test Accounts

```
Email: admin@example.com
Password: admin123
Role: ADMIN

Email: john@example.com
Password: user123
Role: USER
```

---

## 📡 API Test Examples

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'

# Response:
# {
#   "code": 200,
#   "message": "Login successful",
#   "data": {
#     "id": 1,
#     "email": "admin@example.com",
#     "fullName": "Admin User",
#     "role": "ADMIN",
#     "accessToken": "eyJhbGc...",
#     "tokenType": "Bearer"
#   },
#   "timestamp": "2026-04-02T10:30:45"
# }
```

### 2. Get Products (Public)
```bash
curl -X GET http://localhost:8080/api/products?pageNo=0&pageSize=10
```

### 3. Create Category (Admin Only)
```bash
TOKEN="<accessToken từ login>"

curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Category",
    "description": "Category description"
  }'
```

### 4. Create Order (User)
```bash
TOKEN="<accessToken từ login>"

# Thêm product vào cart
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'

# Tạo order từ cart
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingAddress": "123 Main St, City"
  }'
```

---

## 🗄️ Database Management

### Truy cập PgAdmin
- URL: http://localhost:5050
- Email: admin@example.com
- Password: admin

**Add Server:**
1. Right-click "Servers" → Register → Server
2. Name: `springboot-postgres`
3. Host: `postgres`
4. Port: `5432`
5. Username: `postgres`
6. Password: `postgres`
7. Click "Save"

### Xem Data
```sql
-- Trong PgAdmin Query Tool
SELECT * FROM users;
SELECT * FROM products;
SELECT * FROM orders;
```

---

## 🔍 View Logs

```bash
# Real-time logs
docker logs -f springboot-postgres
docker logs -f springboot-redis

# Application logs (khi chạy locally)
tail -f logs/application.log
```

---

## 🛑 Stop Services

```bash
# Stop containers
docker-compose down

# Stop application (Ctrl+C nếu chạy với mvn spring-boot:run)
```

---

## 📊 Important Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven dependencies |
| `application.properties` | Base configuration |
| `application-dev.properties` | Dev config |
| `application-prod.properties` | Prod config |
| `src/main/resources/db/migration/` | Database migrations |
| `.env.example` | Environment template |
| `docker-compose.yml` | Local infrastructure |
| `SETUP_GUIDE.md` | Detailed setup |
| `COMPLETION_REPORT.md` | Project summary |

---

## 🐛 Common Issues

### Issue: "Connection refused" to PostgreSQL
```bash
# Verify container running
docker ps | grep postgres

# If not, restart
docker-compose restart postgres
```

### Issue: "Rate limit exceeded"
- Max 100 requests/minute per IP
- Wait 60 seconds or change IP

### Issue: JWT token invalid
- Token expired? Login again
- Wrong secret? Check `jwt.secret` config
- Missing "Bearer" prefix? Use: `Authorization: Bearer <token>`

### Issue: CORS errors
- Currently CORS not configured
- Add to SecurityConfig if needed

---

## ✅ Verify Setup

```bash
# Check all services
curl http://localhost:8080/api/products

# Should return 200 with products list
# If 401: need to call /api/auth/login first for admin endpoints
```

---

## 📚 Next Steps

1. ✅ Read `SETUP_GUIDE.md` for full API documentation
2. ✅ Check `COMPLETION_REPORT.md` for project overview
3. ✅ Review entity/dto classes để hiểu data model
4. ✅ Explore service classes để hiểu business logic
5. ✅ Run tests: `./mvnw test`
6. ✅ Deploy to production (see SETUP_GUIDE.md)

---

## 💡 Tips

- Use Postman/Insomnia để test APIs comfortably
- Check logs trong `logs/application.log` khi debug
- Database migrations auto-run on startup
- Sample data inserted automatically via Flyway V2

---

**Happy Coding! 🎉**

