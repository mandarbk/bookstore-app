# Quick Reference - Testing the Application

## ✅ Implementation Complete

### Services Created
- ✅ **Book API** (Port 8081) - Dual data source with fallback
- ✅ **Auth Server** (Port 8082) - OAuth2 Authorization Server  
- ✅ **API Gateway** (Port 8080) - Request routing with token relay
- ✅ **MySQL Setup** - Docker Compose with primary + replica

### Features Implemented
- ✅ Spring Data JDBC for READ operations (with automatic fallback)
- ✅ Spring Data JPA for WRITE operations
- ✅ Dual DataSource configuration (Primary + Read Replica)
- ✅ OAuth2 with JWT tokens
- ✅ Spring Cloud API Gateway routing
- ✅ Flyway database migrations
- ✅ Comprehensive REST API for Books & Orders
- ✅ Security with @PreAuthorize annotations
- ✅ CORS configuration
- ✅ Health checks & Metrics

---

## 🚀 Getting Started (5 minutes)

### Step 1: Start Databases
```bash
cd /home/mandarbk/spring-boot-book-store
docker-compose up -d
```
Wait for containers to be healthy (check with `docker-compose ps`)

### Step 2: Build Project
```bash
mvn clean install
```

### Step 3: Start Auth Server (Terminal 1)
```bash
cd auth-server
mvn spring-boot:run
# Wait for: "Started AuthServerApplication"
```

### Step 4: Start Book API (Terminal 2)
```bash
cd book-api
mvn spring-boot:run
# Wait for: "Started BookApiApplication"
# Watch for Flyway migrations: "Successfully applied ... migrations"
```

### Step 5: Start API Gateway (Terminal 3)
```bash
cd api-gateway
mvn spring-boot:run
# Wait for: "Started ApiGatewayApplication"
```

---

## 🔐 Get Access Token

```bash
TOKEN=$(curl -s -X POST http://localhost:8082/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u bookstore-client:bookstore-secret \
  -d "grant_type=client_credentials&scope=read:books%20write:books" \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

echo "Token: $TOKEN"
```

---

## 📚 Test Book API

### List All Books (READ - Uses Replica with Fallback)
```bash
curl -X GET http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

### Get Single Book
```bash
curl -X GET http://localhost:8080/api/books/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Create Book (WRITE - Uses Primary DB)
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Kubernetes in Action",
    "author": "Marko Luksa",
    "isbn": "978-1617293726",
    "price": 45.99,
    "inventory": 5,
    "description": "Learn Kubernetes container orchestration"
  }'
```

### Update Book (WRITE - Uses Primary DB)
```bash
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"inventory": 20}'
```

### Delete Book
```bash
curl -X DELETE http://localhost:8080/api/books/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Search Books by Title
```bash
curl -X GET "http://localhost:8080/api/books/search?title=Gatsby" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📦 Test Order API

### List All Orders
```bash
curl -X GET http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN"
```

### Create Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "totalPrice": 50.00
  }'
```

### Update Order Status
```bash
curl -X PUT "http://localhost:8080/api/orders/1/status?status=SHIPPED" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔄 Test Fallback Mechanism

### Monitor Logs While Testing

**Terminal for Book API logs:**
```bash
cd book-api
mvn spring-boot:run | grep -i "replica\|fallback\|primary"
```

### Scenario 1: Normal Operation (Replica Works)
```bash
# Both DBs running - GET will use Replica
curl -X GET http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN"

# Logs should show: "Attempting to read from replica database"
```

### Scenario 2: Stop Replica (Automatic Fallback)
```bash
# Stop replica
docker pause mysql-replica

# Make GET request - will fallback to primary
curl -X GET http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN"

# Logs should show: "Replica database failed, falling back to primary"
```

### Scenario 3: Restart Replica (Resume Using Replica)
```bash
# Restart replica
docker unpause mysql-replica

# Wait 10 seconds for reconnection

# Make GET request - will use replica again
curl -X GET http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN"

# Logs should show: "Attempting to read from replica database"
```

---

## 🏥 Health Checks

```bash
# Book API Health
curl http://localhost:8081/actuator/health

# Auth Server Health
curl http://localhost:8082/actuator/health

# API Gateway Health
curl http://localhost:8080/actuator/health
```

---

## 📊 View Metrics

```bash
# Gateway metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/http.server.requests
```

---

## 🛑 Database Information

### Primary Database
- **Host:** localhost
- **Port:** 3306
- **Database:** bookstore
- **Username:** bookuser
- **Password:** bookpass123

### Read Replica
- **Host:** localhost
- **Port:** 3307
- **Database:** bookstore
- **Username:** bookuser
- **Password:** bookpass123

### MySQL Commands
```bash
# Connect to primary
mysql -h 127.0.0.1 -P 3306 -u bookuser -p bookstore

# Connect to replica
mysql -h 127.0.0.1 -P 3307 -u bookuser -p bookstore

# View all books
SELECT * FROM books;

# View all orders
SELECT * FROM orders;
```

---

## ❌ Cleanup

```bash
# Stop all services (Ctrl+C in each terminal)

# Stop databases
docker-compose down

# Remove volumes
docker-compose down -v

# Clean Maven build
mvn clean
```

---

## 📋 OAuth2 Credentials

### Bookstore Client
- **ID:** bookstore-client
- **Secret:** bookstore-secret
- **Scopes:** read:books, write:books, read:orders, write:orders

### Test Client
- **ID:** test-client
- **Secret:** test-secret
- **Scopes:** read:books, write:books, read:orders, write:orders

---

## 🔍 Key Files to Review

1. **Dual DataSource:** `book-api/src/main/java/com/bookstore/api/config/DataSourceConfig.java`
2. **Fallback Logic:** `book-api/src/main/java/com/bookstore/api/util/ReadFallbackTemplate.java`
3. **JDBC Repositories:** `book-api/src/main/java/com/bookstore/api/repository/jdbc/`
4. **JPA Repositories:** `book-api/src/main/java/com/bookstore/api/repository/jpa/`
5. **Auth Configuration:** `auth-server/src/main/java/com/bookstore/auth/config/`
6. **Gateway Routes:** `api-gateway/src/main/resources/application.yml`
7. **Database Schema:** `book-api/src/main/resources/db/migration/V1__init_schema.sql`

---

## 💡 Architecture Insights

### Read Operations (GET)
```
Client → API Gateway → Book API → ReadFallbackTemplate
                            ↓
                    Try Replica (Port 3307)
                            ↓
                    If failed → Primary (Port 3306)
```

### Write Operations (POST, PUT, DELETE)
```
Client → API Gateway → Book API → JPA Repository
                            ↓
                    Primary Database (Port 3306)
```

### OAuth2 Flow
```
Client → Auth Server → JWT Token
                           ↓
Client + Token → API Gateway → Book API (validates token)
                                    ↓
                            OAuth2 Resource Server
```

---

## ✨ Sample API Responses

### Get Book Response
```json
{
  "id": 1,
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "isbn": "978-0743273565",
  "price": 12.99,
  "inventory": 50,
  "description": "A classic American novel set in the Jazz Age",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Get Order Response
```json
{
  "id": 1,
  "customerId": 1,
  "orderDate": "2024-01-15T10:35:00",
  "totalPrice": 25.98,
  "status": "COMPLETED",
  "items": [
    {
      "id": 1,
      "orderId": 1,
      "bookId": 1,
      "quantity": 1,
      "price": 12.99
    }
  ]
}
```

---

## 🎯 Next Steps

1. ✅ Start the services (follow "Getting Started")
2. ✅ Get an OAuth2 token
3. ✅ Test CRUD operations on Books and Orders
4. ✅ Stop the replica and verify fallback works
5. ✅ Review the comprehensive README.md for detailed information
6. ✅ Explore the codebase to understand dual data sources implementation

**Happy Testing! 🚀**
