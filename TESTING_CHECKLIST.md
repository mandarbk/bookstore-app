# Comprehensive Testing Checklist

## Pre-Requisites Check

- [ ] Java 17+ installed: `java -version`
- [ ] Maven installed: `mvn -version`
- [ ] Docker running: `docker --version`
- [ ] Docker Compose installed: `docker-compose --version`
- [ ] Project location: `/home/mandarbk/spring-boot-book-store`

---

## Phase 1: Infrastructure Setup

### Database Initialization
- [ ] Start databases: `docker-compose up -d`
- [ ] Verify containers running: `docker-compose ps` (both mysql-primary and mysql-replica HEALTHY)
- [ ] Primary DB accessible: `mysql -h 127.0.0.1 -P 3306 -u bookuser -p bookstore` (password: bookpass123)
- [ ] Replica DB accessible: `mysql -h 127.0.0.1 -P 3307 -u bookuser -p bookstore`
- [ ] Bookstore database exists: `SHOW DATABASES;` → see 'bookstore'

### Build Process
- [ ] Build parent POM: `mvn clean install` from `/home/mandarbk/spring-boot-book-store`
- [ ] No build errors
- [ ] All 3 modules build successfully: book-api, auth-server, api-gateway
- [ ] Maven reports: BUILD SUCCESS

---

## Phase 2: Service Startup

### Auth Server Startup
- [ ] Start auth-server: `cd auth-server && mvn spring-boot:run`
- [ ] Logs show: "Started AuthServerApplication"
- [ ] Server listening on port 8082
- [ ] Health check passes: `curl http://localhost:8082/actuator/health`
- [ ] JWKS endpoint accessible: `curl http://localhost:8082/.well-known/jwks.json` (returns JSON with keys)

### Book API Startup
- [ ] Start book-api: `cd book-api && mvn spring-boot:run`
- [ ] Logs show: "Started BookApiApplication"
- [ ] Flyway migrations successful: "Successfully applied ... migrations"
- [ ] Database tables created (verify in MySQL):
  - [ ] `books` table exists
  - [ ] `orders` table exists
  - [ ] `customers` table exists
  - [ ] `order_items` table exists
- [ ] Sample data inserted: 5 books + 3 customers + 3 orders
- [ ] Server listening on port 8081
- [ ] Health check passes: `curl http://localhost:8081/actuator/health`

### API Gateway Startup
- [ ] Start api-gateway: `cd api-gateway && mvn spring-boot:run`
- [ ] Logs show: "Started ApiGatewayApplication"
- [ ] Server listening on port 8080
- [ ] Health check passes: `curl http://localhost:8080/actuator/health`
- [ ] Routes configured: `curl http://localhost:8080/actuator/gateway/routes | grep -c "books-service"`

---

## Phase 3: OAuth2 Authentication

### Token Generation
- [ ] Get token with client credentials:
  ```bash
  curl -X POST http://localhost:8082/oauth2/token \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -u bookstore-client:bookstore-secret \
    -d "grant_type=client_credentials&scope=read:books%20write:books"
  ```
- [ ] Response contains: `access_token`, `token_type: Bearer`, `expires_in`
- [ ] Token is JWT (format: `header.payload.signature`)
- [ ] Decode token payload and verify:
  - [ ] `scope` contains "read:books write:books"
  - [ ] `iss` (issuer) is "http://localhost:8082"
  - [ ] `exp` (expiration) is in future

### Invalid Credentials Test
- [ ] Wrong password fails: `curl -u bookstore-client:wrong-secret ...` → 401 Unauthorized
- [ ] Wrong client fails: `curl -u wrong-client:bookstore-secret ...` → 401 Unauthorized
- [ ] Missing scope parameter fails
- [ ] Invalid scope fails

---

## Phase 4: API Gateway Routing

### Route Configuration Verification
- [ ] List all routes: `curl http://localhost:8080/actuator/gateway/routes`
- [ ] Should see routes for:
  - [ ] `books-service` → `http://localhost:8081`
  - [ ] `orders-service` → `http://localhost:8081`
  - [ ] `auth-server` → `http://localhost:8082`

### Token Relay Verification
- [ ] Request with valid token passes through gateway
- [ ] Request without token gets 401: `curl http://localhost:8080/api/books`
- [ ] Invalid token gets 401: `curl -H "Authorization: Bearer invalid" http://localhost:8080/api/books`

---

## Phase 5: Book API - READ Operations (JDBC with Fallback)

### List All Books
- [ ] GET `/api/books` returns 200 OK
- [ ] Response is array with 5 sample books
- [ ] Each book has: id, title, author, isbn, price, inventory, createdAt, updatedAt
- [ ] Logs show: "Attempting to read from replica database"

### Get Single Book by ID
- [ ] GET `/api/books/1` returns 200 with book details
- [ ] Invalid ID returns 404: GET `/api/books/999`
- [ ] Verify book has all expected fields

### Search by ISBN
- [ ] GET `/api/books/isbn/978-0743273565` returns matching book
- [ ] Invalid ISBN returns 404

### Search by Author
- [ ] GET `/api/books/author/F.%20Scott%20Fitzgerald` returns matching books
- [ ] Unknown author returns empty array

### Search by Title
- [ ] GET `/api/books/search?title=Gatsby` returns matching books
- [ ] Partial match works: `?title=Great` returns "The Great Gatsby"
- [ ] Case insensitive search works

### Count Operations
- [ ] GET `/api/books/count` returns total count (should be 5)

---

## Phase 6: Book API - WRITE Operations (JPA)

### Create Book
- [ ] POST `/api/books` with new book data returns 201 CREATED
- [ ] Response includes: id (auto-generated), createdAt, updatedAt
- [ ] New book appears in database
- [ ] Required fields enforced:
  - [ ] Missing title: 400 Bad Request
  - [ ] Missing author: 400 Bad Request
  - [ ] Missing price: 400 Bad Request

### Sample Create Request:
```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "978-0132350884",
  "price": 49.99,
  "inventory": 8,
  "description": "A Handbook of Agile Software Craftsmanship"
}
```

### Update Book
- [ ] PUT `/api/books/1` updates book successfully
- [ ] Only provided fields are updated (partial update)
- [ ] Updated book reflects changes in database
- [ ] Invalid ID returns 404

### Update Inventory
- [ ] PUT `/api/books/1/inventory?quantity=10` increases inventory by 10
- [ ] Negative quantity works (decreases inventory)
- [ ] Logs show: "Updating inventory for book id"

### Delete Book
- [ ] DELETE `/api/books/1` returns 204 No Content
- [ ] Book is removed from database
- [ ] Subsequent GET returns 404
- [ ] Invalid ID returns 404

---

## Phase 7: Order API Operations

### List Orders (READ)
- [ ] GET `/api/orders` returns array with 3 sample orders
- [ ] Each order has: id, customerId, orderDate, totalPrice, status

### Get Order by ID (READ)
- [ ] GET `/api/orders/1` returns order details
- [ ] Invalid ID returns 404

### Get Orders by Customer (READ)
- [ ] GET `/api/orders/customer/1` returns orders for customer 1
- [ ] GET `/api/orders/customer/999` returns empty array

### Get Orders by Status (READ)
- [ ] GET `/api/orders/status/PENDING` returns pending orders
- [ ] GET `/api/orders/status/COMPLETED` returns completed orders

### Create Order (WRITE)
- [ ] POST `/api/orders` with order data returns 201 CREATED
- [ ] New order appears in database
- [ ] Auto-fills: orderDate (current time), status (PENDING)

### Update Order Status (WRITE)
- [ ] PUT `/api/orders/1/status?status=SHIPPED` updates status
- [ ] Status changes from PENDING to SHIPPED
- [ ] Invalid status: still saves (no enum validation in this implementation)

### Delete Order (WRITE)
- [ ] DELETE `/api/orders/1` returns 204 No Content
- [ ] Order is removed from database

---

## Phase 8: Fallback Mechanism Testing

### Prerequisite: Monitor Logs
Open a terminal and tail book-api logs:
```bash
cd book-api && mvn spring-boot:run | grep -i "replica\|fallback\|primary"
```

### Test 1: Normal Operation (Replica Available)
- [ ] Both MySQL containers running
- [ ] Make GET request: `curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/books`
- [ ] Request succeeds (200 OK)
- [ ] Logs show: "Attempting to read from replica database"
- [ ] Response time is normal (~100-500ms depending on network)

### Test 2: Replica Failure (Automatic Fallback)
- [ ] Stop replica: `docker pause mysql-replica`
- [ ] Wait 5 seconds
- [ ] Make GET request: `curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/books`
- [ ] Request still succeeds! (200 OK)
- [ ] Logs show:
  - [ ] First attempt: "Attempting to read from replica database"
  - [ ] Then: "Replica database failed, falling back to primary"
- [ ] Response time is slightly longer (due to connection timeout + retry)
- [ ] Data returned is identical

### Test 3: Replica Recovery (Resume Using Replica)
- [ ] Restart replica: `docker unpause mysql-replica`
- [ ] Wait 10 seconds for connection pool recovery
- [ ] Make GET request again
- [ ] Logs show: "Attempting to read from replica database"
- [ ] System automatically uses replica again

### Test 4: Write Operations Always Use Primary
- [ ] Stop replica: `docker pause mysql-replica`
- [ ] Create new book: POST `/api/books` with new data
- [ ] Request succeeds (201 CREATED)
- [ ] Book appears in database
- [ ] Logs show: "Creating new book" (no fallback logic for writes)

### Test 5: Both Databases Down (Error Handling)
- [ ] Stop primary: `docker pause mysql-primary`
- [ ] Make any request
- [ ] Request fails with 500 Internal Server Error (expected)
- [ ] Logs show connection errors
- [ ] Restart primary: `docker unpause mysql-primary`

---

## Phase 9: Security & Authorization

### Missing Authorization Header
- [ ] GET `/api/books` without Authorization header → 401 Unauthorized

### Invalid Token
- [ ] GET `/api/books` with invalid token → 401 Unauthorized
- [ ] GET `/api/books` with expired token → 401 Unauthorized

### Insufficient Scopes (Missing read:books)
- [ ] Get token with only `write:books` scope
- [ ] GET `/api/books` → 403 Forbidden

### Insufficient Scopes (Missing write:books)
- [ ] Get token with only `read:books` scope
- [ ] POST `/api/books` → 403 Forbidden

### Scope Validation
- [ ] GET endpoints require: `SCOPE_read:books` or `SCOPE_read:orders`
- [ ] POST/PUT/DELETE require: `SCOPE_write:books` or `SCOPE_write:orders`

---

## Phase 10: Data Validation & Error Handling

### Invalid Request Data
- [ ] Create book with invalid price (string instead of number) → 400 Bad Request
- [ ] Create order with negative total price → 400 Bad Request
- [ ] Create book with duplicate ISBN → potentially 400 or success (depends on business logic)

### Resource Not Found
- [ ] GET `/api/books/999999` → 404 Not Found
- [ ] PUT `/api/books/999999` → 404 Not Found
- [ ] DELETE `/api/books/999999` → 404 Not Found

### Method Not Allowed
- [ ] DELETE on `/api/books/search` → 405 Method Not Allowed
- [ ] PUT on `/api/books` (collection) → 405 Method Not Allowed

---

## Phase 11: CORS & Cross-Origin Requests

### CORS Headers
- [ ] OPTIONS request to `/api/books` returns 200
- [ ] Response includes: `Access-Control-Allow-Origin`, `Access-Control-Allow-Methods`
- [ ] Allowed methods include: GET, POST, PUT, DELETE, OPTIONS
- [ ] `Access-Control-Allow-Headers` includes: Authorization

### Test from Different Origin
- [ ] Open browser console and make AJAX request from different origin
- [ ] Request succeeds (CORS properly configured)

---

## Phase 12: Health & Metrics

### Actuator Endpoints
- [ ] Book API health: `curl http://localhost:8081/actuator/health` → UP
- [ ] Auth Server health: `curl http://localhost:8082/actuator/health` → UP
- [ ] API Gateway health: `curl http://localhost:8080/actuator/health` → UP

### Metrics
- [ ] View available metrics: `curl http://localhost:8080/actuator/metrics`
- [ ] Check HTTP requests metric: `curl http://localhost:8080/actuator/metrics/http.server.requests`

### Gateway Routes Info
- [ ] List routes: `curl http://localhost:8080/actuator/gateway/routes`
- [ ] Shows all configured routes with predicates and filters

---

## Phase 13: Performance Testing

### Concurrent Requests
- [ ] Send 10 concurrent GET requests:
  ```bash
  for i in {1..10}; do
    curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/books &
  done
  ```
- [ ] All requests complete successfully
- [ ] Response times are reasonable
- [ ] No connection pool exhaustion errors

### Large Dataset
- [ ] Create 100 books
- [ ] GET `/api/books` returns all books (pagination not implemented, may be slow)
- [ ] Search performance is acceptable

---

## Phase 14: Database Verification

### Schema Verification
```sql
-- Connect to primary DB
mysql -h 127.0.0.1 -P 3306 -u bookuser -p bookstore

-- Check tables
SHOW TABLES;
-- Should show: books, orders, customers, order_items

-- Check books table
DESC books;
SHOW INDEXES FROM books;
-- Should have indexes on title, author

-- Verify data
SELECT COUNT(*) FROM books; -- Should be ≥5
SELECT COUNT(*) FROM orders; -- Should be ≥3
SELECT COUNT(*) FROM customers; -- Should be ≥3
```

### Replica Synchronization
```sql
-- Connect to replica DB
mysql -h 127.0.0.1 -P 3307 -u bookuser -p bookstore

-- Check if data is identical
SELECT * FROM books;
SELECT * FROM orders;

-- Should have identical data as primary
```

---

## Phase 15: Cleanup & Teardown

### Stop Services
- [ ] Stop Book API (Ctrl+C in terminal)
- [ ] Stop Auth Server (Ctrl+C in terminal)
- [ ] Stop API Gateway (Ctrl+C in terminal)

### Stop Databases
- [ ] Run: `docker-compose down`
- [ ] Verify containers stopped: `docker-compose ps`

### Optional: Remove Volumes
- [ ] Run: `docker-compose down -v`
- [ ] This removes database data (fresh start next time)

### Clean Maven Build
- [ ] Run: `mvn clean`
- [ ] Removes target directories

---

## Test Results Summary

**Test Date:** _______________

| Category | Tests Passed | Tests Failed | Notes |
|----------|-------------|-------------|-------|
| Infrastructure | ___/3 | ___ | |
| Service Startup | ___/3 | ___ | |
| OAuth2 Auth | ___/4 | ___ | |
| API Gateway | ___/3 | ___ | |
| READ Operations | ___/6 | ___ | |
| WRITE Operations | ___/4 | ___ | |
| Orders API | ___/7 | ___ | |
| Fallback Testing | ___/5 | ___ | |
| Security | ___/5 | ___ | |
| Error Handling | ___/4 | ___ | |
| CORS | ___/2 | ___ | |
| Health & Metrics | ___/3 | ___ | |
| Performance | ___/2 | ___ | |
| Database | ___/2 | ___ | |
| **TOTAL** | **___/54** | **___** | |

---

## Known Issues & Workarounds

1. **Connection Timeout on First Request**
   - Cause: Connection pool initialization
   - Workaround: First request may take 1-2 seconds, subsequent requests are fast

2. **No Pagination Implemented**
   - Large result sets may be slow
   - Workaround: Implement pagination in future versions

3. **No Rate Limiting**
   - No protection against brute force
   - Workaround: Add rate limiting filter in gateway

4. **Eureka Disabled in Gateway**
   - Service discovery not needed for localhost testing
   - To enable: set `eureka.client.enabled: true` in gateway config

---

## Success Criteria

✅ All 54 tests pass
✅ Fallback mechanism works correctly
✅ OAuth2 security is enforced
✅ API Gateway routes requests properly
✅ JDBC reads from replica with automatic fallback
✅ JPA writes to primary database
✅ All CRUD operations work
✅ Health checks pass
✅ No error logs (except expected 401/403 for auth failures)

---

**Implementation Status: COMPLETE ✅**

All components implemented and ready for testing!
