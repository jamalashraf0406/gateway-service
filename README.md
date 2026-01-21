# Gateway Service

## Features
- Single Entry Point and add custom header to every request.
- Routes requests to the correct microservice
  - /auth/**    --> Auth Service
  - /order/**   --> Order Command service
- The gateway validates requests before they hit services.
  - Client → API Gateway → (validate) → Microservice
- API Gateway ensures Authentication and Authorization (roles, scopes)
  - Only ADMIN → POST /user/v1/create
- Rate Limiting for the request
  - POST /order → 10 req/sec 
  - GET /auth → 10 req/sec

## Tech Stacks
- Java 17
- Spring boot
- Redis
- Spring cloud gateway
- Spring security
- lombok
- resilience4j

## Request Flow 
```text 
Client
↓
API Gateway
├─ Global Filters
│    ├─ GatewayAuthHeaderFilter
│    ├─ Logging
│    └─ Header propagation
│
├─ Rate Limiter (Redis)
├─ Circuit Breaker (Resilience4j) - not implemented yet
│
├─ Security (JWT Validation)
│    └─ Fetch public key from Auth Service (JWKS)
│
↓
Microservices
├─ Auth Service
│    └─ /auth/**,
|    └─ /user/v1/create (Protected API)
|
└─ Order Service
    └─ /order/** (Protected API)
    
```
## Architecture & Design for scalability
- Stateless API Gateway
    1. No session data stored in gateway
    2. Authentication via JWT / OAuth2 tokens
    3. Enables horizontal scaling (Kubernetes HPA)
- Centralized Security Architecture
    1. Gateway handles:
        - Authentication (JWT validation)
        - Authorization (roles, scopes)
- Backend services trust gateway headers
- Rate Limiting 

## How to Run
- Checkout this code to you local
- Your system should have below tools
  - Java 17
  - Spring boot 3
  - Redis
  - lombok
  - Maven
- Then run the below command
  - mvn clean install
  - java -jar target/gateway-service-0.0.1-SNAPSHOT.jar
- server url: http://localhost:8083
- You can use below APIs to test, before making call make downstream services up running
  - POST /auth/login
    - curl --location 'http://localhost:8083/auth/login' \
      --header 'Content-Type: application/json' \
      --data '{
      "username":"jashraf07",
      "password":"password123"
      }'
  - POST /user/v1/create
    - curl --location 'http://localhost:8083/user/v1/create' \
      --header 'Content-Type: application/json' \
      --header 'Authorization: Bearer token from previous api call' \
      --data '{"username":"new-user08","password":"password123"}'
   
