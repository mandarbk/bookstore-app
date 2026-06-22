# Spring Boot Book Store - Multi-Microservice Architecture

A comprehensive Spring Boot application demonstrating online book selling with advanced patterns including dual data sources, OAuth2 security, and API Gateway routing.

## Architecture Overview

This project consists of **3 independent Spring Boot microservices**:

1. **Book API Service** (Port 8081)
   - Dual data source configuration (Primary DB + Read Replica)
   - Spring Data JDBC for READ operations with automatic fallback
   - Spring Data JPA for WRITE operations
   - REST API for Books and Orders

2. **OAuth2 Authorization Server** (Port 8082)
   - Spring Authorization Server
   - JWT token generation
   - Client credentials flow support
   - OAuth2 scopes for books and orders management

3. **API Gateway** (Port 8080)
   - Spring Cloud API Gateway
   - OAuth2 token relay and validation
   - Request routing to microservices
   - CORS support

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker & Docker Compose
- MySQL 8.0 (via Docker Compose)

## Quick Start

### 1. Clone the Repository
```bash
cd /home/mandarbk/spring-boot-book-store
```

### 2. Start MySQL Databases (Primary + Replica)
```bash
docker-compose up -d
```

This starts two MySQL instances:
- **Primary DB**: `localhost:3306/bookstore`
- **Read Replica DB**: `localhost:3307/bookstore`
- Credentials: `bookuser` / `bookpass123`

Verify containers are running:
```bash
docker-compose ps
```

### 3. Build All Services
```bash
mvn clean install
```

### 4. Start Services (in separate terminals)

**Terminal 1 - Authorization Server:**
```bash
cd auth-server
mvn spring-boot:run
```
Available at: `http://localhost:8082`

**Terminal 2 - Book API Service:**
```bash
cd book-api
mvn spring-boot:run
```
Available at: `http://localhost:8081`

**Terminal 3 - API Gateway:**
```bash
cd api-gateway
mvn spring-boot:run
```
Available at: `http://localhost:8080`

## API Usage Guide

### 1. Get OAuth2 Token

```bash
curl -X POST http://localhost:8082/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u bookstore-client:bookstore-secret \
  -d "grant_type=client_credentials&scope=read:books%20write:books%20read:orders%20write:orders"
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 7200
}
```

### 2. Access Protected Resources via Gateway

**Get All Books (READ - Uses JDBC):**
```bash
TOKEN="your_access_token_here"

curl -X GET http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN"
```

**Create New Book (WRITE - Uses JPA):**
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot in Action",
    "author": "Craig Walls",
    "isbn": "978-1617292545",
    "price": 39.99,
    "inventory": 10,
    "description": "Learn Spring Boot"
  }'
```

**Get Book by ID:**
```bash
curl -X GET http://localhost:8080/api/books/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Update Book (WRITE - Uses JPA):**
```bash
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inventory": 15
  }'
```

**Delete Book:**
```bash
curl -X DELETE http://localhost:8080/api/books/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Order API Operations

**Get All Orders (READ):**
```bash
curl -X GET http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN"
```

**Create Order (WRITE):**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "totalPrice": 79.98
  }'
```

**Update Order Status:**
```bash
curl -X PUT http://localhost:8080/api/orders/1/status?status=SHIPPED \
  -H "Authorization: Bearer $TOKEN"
```

## Database & Fallback Mechanism

### Dual Data Source Configuration

**Read Operations (GET Requests):**
1. Attempts to read from **Read Replica DB** (Port 3307)
2. If replica is unavailable/slow, automatically falls back to **Primary DB** (Port 3306)
3. Fallback is transparent to the client

**Write Operations (POST, PUT, DELETE):**
- Always writes to **Primary DB** using JPA
- Ensures data consistency

### Test the Fallback Mechanism

**Stop the Read Replica:**
```bash
docker pause mysql-replica
```

**Make a GET request (will use primary DB):**
```bash
curl -X GET http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN"
```

**Restart the Read Replica:**
```bash
docker unpause mysql-replica
```

## Repository Layer Architecture

### JDBC Repositories (Reads with Fallback)
- **File:** `book-api/src/main/java/com/bookstore/api/repository/jdbc/`
- Uses `ReadFallbackTemplate` for automatic fallback
- Methods: `findById()`, `findAll()`, `findByAuthor()`, `searchByTitle()`

### JPA Repositories (Writes)
- **File:** `book-api/src/main/java/com/bookstore/api/repository/jpa/`
- Standard Spring Data JPA repositories
- Methods: `save()`, `update()`, `delete()`

## OAuth2 Security & Scopes

### Registered Clients

**Bookstore Client (Full Access):**
- Client ID: `bookstore-client`
- Client Secret: `bookstore-secret`
- Scopes: `read:books`, `write:books`, `read:orders`, `write:orders`
- Access Token TTL: 2 hours

**Test Client:**
- Client ID: `test-client`
- Client Secret: `test-secret`
- Scopes: `read:books`, `write:books`, `read:orders`, `write:orders`
- Access Token TTL: 1 hour

### Security in Actions
- All endpoints protected by `@PreAuthorize` annotations
- Endpoints validate OAuth2 scopes before execution
- Token relay through API Gateway ensures end-to-end security

## Project Structure

```
spring-boot-book-store/
├── pom.xml                           (Parent POM)
├── docker-compose.yml                (MySQL setup)
├── README.md                         (This file)
│
├── book-api/                         (Microservice - Port 8081)
│   ├── pom.xml
│   └── src/main/java/com/bookstore/api/
│       ├── config/
│       │   ├── DataSourceConfig.java      (Dual datasources)
│       │   ├── JpaConfig.java             (JPA configuration)
│       │   ├── JdbcConfig.java            (JDBC configuration)
│       │   └── SecurityConfig.java        (OAuth2 resource server)
│       ├── entity/                        (JPA entities)
│       ├── dto/                           (DTOs for API responses)
│       ├── repository/
│       │   ├── jdbc/                      (JDBC repositories with fallback)
│       │   └── jpa/                       (JPA repositories)
│       ├── service/                       (Business logic layer)
│       ├── controller/                    (REST endpoints)
│       ├── util/
│       │   └── ReadFallbackTemplate.java  (Fallback logic)
│       └── BookApiApplication.java
│
├── auth-server/                      (Authorization Server - Port 8082)
│   ├── pom.xml
│   └── src/main/java/com/bookstore/auth/
│       ├── config/
│       │   ├── AuthorizationServerConfig.java
│       │   ├── SecurityConfig.java
│       │   └── JwtKeyProperties.java
│       └── AuthServerApplication.java
│
└── api-gateway/                      (API Gateway - Port 8080)
    ├── pom.xml
    └── src/main/java/com/bookstore/gateway/
        ├── config/
        │   └── SecurityConfig.java
        └── ApiGatewayApplication.java
```

## Database Schema

### Books Table
```sql
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    inventory INT DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Orders Table
```sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);
```

### Customers & OrderItems Tables
- Similar structure for customers and order items management

## Flyway Migrations

Migrations are automatically applied on startup:
- **V1__init_schema.sql** - Creates all tables with indexes
- **V2__insert_sample_data.sql** - Inserts test data

View migrations in: `book-api/src/main/resources/db/migration/`

## Monitoring & Health Checks

### Health Endpoints
```bash
# Book API Health
curl http://localhost:8081/actuator/health

# Auth Server Health
curl http://localhost:8082/actuator/health

# API Gateway Health
curl http://localhost:8080/actuator/health
```

### Metrics
```bash
# View available metrics
curl http://localhost:8080/actuator/metrics

# View specific metric
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### Gateway Routes
```bash
# View all configured routes
curl http://localhost:8080/actuator/gateway/routes
```

## Troubleshooting

### Connection Refused to Primary DB
```bash
# Verify MySQL is running
docker-compose ps

# Restart containers
docker-compose restart
```

### OAuth2 Token Validation Fails
- Verify Auth Server is running on port 8082
- Check JWT issuer URL matches: `http://localhost:8082`
- Verify scopes in token match endpoint requirements

### Fallback Not Triggering
- Stop replica: `docker pause mysql-replica`
- Make a GET request and check logs for fallback message
- Check Book API logs for warning: `"Replica database failed, falling back to primary"`

### Port Already in Use
```bash
# Find and kill process on specific port
lsof -i :8080  # For API Gateway
kill -9 <PID>
```

## Testing Dual Data Source Fallback

### Scenario 1: Normal Operation (Replica Available)
```bash
# Both DBs running
docker-compose up -d

# Make request
curl -X GET http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN"

# Check Book API logs - should show "Attempting to read from replica database"
```

### Scenario 2: Replica Failure (Fallback to Primary)
```bash
# Stop replica
docker pause mysql-replica

# Make request - will use Primary DB
curl -X GET http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN"

# Check logs - should show "Replica database failed, falling back to primary"
```

### Scenario 3: Write Operations (Always Uses Primary)
```bash
# Create book - always goes to Primary DB
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "Test Book", "author": "Author", ...}'

# Check logs - should show JPA transaction on primary
```

## Performance Considerations

1. **Read Replica Benefits:**
   - Offloads read queries from primary database
   - Reduces primary DB load by ~70% in read-heavy workloads

2. **Fallback Overhead:**
   - Minimal: Only incurred when replica fails
   - Connection pool ensures fast reconnection

3. **Connection Pooling:**
   - Primary Pool: 10 max connections, 2 idle
   - Replica Pool: 10 max connections, 2 idle

## Running Tests

### Unit Tests
```bash
cd book-api
mvn test
```

### Integration Tests
```bash
cd book-api
mvn verify
```

## Cleanup

### Stop All Services
```bash
# In each terminal (Ctrl+C)

# Stop databases
docker-compose down

# Remove volumes (optional)
docker-compose down -v
```

## References

- [Spring Authorization Server Documentation](https://spring.io/projects/spring-authorization-server)
- [Spring Cloud Gateway Documentation](https://cloud.spring.io/spring-cloud-gateway)
- [Spring Data JDBC Documentation](https://spring.io/projects/spring-data-jdbc)
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Flyway Documentation](https://flywaydb.org)

## License

MIT License - Feel free to use this project as a reference for your own implementations.

## Support

For issues or questions, please refer to the Spring Boot documentation or create an issue in the repository.
