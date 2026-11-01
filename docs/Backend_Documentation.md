# 🎥 LiveStream Platform — Microservices Architecture

> A production-grade live streaming platform built with Spring Boot, Java 25, and modern cloud-native patterns. Designed as a resume project demonstrating microservices, event-driven architecture, and full CI/CD deployment.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [Build Order & Phases](#build-order--phases)
- [Services](#services)
- [Data Models & Entities](#data-models--entities)
- [Inter-Service Communication](#inter-service-communication)
- [Infrastructure & DevOps](#infrastructure--devops)
- [CI/CD Pipeline](#cicd-pipeline)
- [Spring Initializer Dependencies (Per Service)](#spring-initializer-dependencies-per-service)

---

## Architecture Overview

```
                        ┌─────────────────────────────────┐
                        │         API Gateway             │
                        │   (Spring Cloud Gateway)        │
                        └────────────┬────────────────────┘
                                     │
          ┌──────────────────────────┼─────────────────────────┐
          │                          │                         │
  ┌───────▼──────┐          ┌────────▼───────┐         ┌───────▼──────┐
  │ User Service │          │ Stream Service │         │ Chat Service │
  └──────────────┘          └────────────────┘         └──────────────┘
          │                          │                         │
  ┌───────▼──────┐          ┌────────▼───────┐         ┌───────▼──────┐
  │  Auth Service│          │ Media/Transcode│         │Notification  │
  └──────────────┘          └────────────────┘         │  Service     │
                                     │                 └──────────────┘
                            ┌────────▼───────┐
                            │ CDN / Storage  │
                            └────────────────┘

  ─────────────────────────────────────────────────────────────────
  Infrastructure: Kafka (Events) │ Redis (Cache) │ PostgreSQL (DB)
  ─────────────────────────────────────────────────────────────────
  Service Discovery: Eureka │ Config: Spring Cloud Config
  ─────────────────────────────────────────────────────────────────
  Observability: Prometheus + Grafana │ Zipkin (Tracing) │ ELK Stack
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.x |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Config Management | Spring Cloud Config Server |
| Message Broker | Apache Kafka |
| Databases | PostgreSQL (primary), MongoDB (chat/logs) |
| Cache | Redis |
| Media Storage | MinIO (self-hosted S3-compatible) or AWS S3 |
| Video Transcoding | FFmpeg (via process calls) |
| Streaming Protocol | HLS (HTTP Live Streaming) via FFmpeg |
| WebSocket | Spring WebSocket + STOMP |
| Security | Spring Security + JWT + OAuth2 |
| Containerization | Docker + Docker Compose |
| Orchestration | Kubernetes (K8s) |
| CI/CD | GitHub Actions |
| Observability | Prometheus, Grafana, Zipkin, Logback |
| Documentation | Springdoc OpenAPI (Swagger UI) |

---

## Build Order & Phases

Follow these phases strictly. Each phase builds on the last. Do not skip ahead — a stable foundation matters more than a fast but broken setup.

---

### Phase 1 — Infrastructure & Scaffolding

**Goal:** Get service discovery, config, and gateway running before writing any business logic.

**Step 1: Config Server**
Start here. Every other service will pull its configuration from this server. Without it, you'd hardcode configs everywhere.

**Step 2: Eureka Service Registry**
All services register here. The Gateway and other services use it to find each other dynamically. This is the backbone of your microservices discovery.

**Step 3: API Gateway**
The single entry point for all client traffic. Handles routing, rate limiting, and will later handle JWT validation centrally. Set up basic routes (even empty ones) pointing to services you'll build next.

At the end of Phase 1, you should be able to start all three infrastructure services, see them register, and route a test request through the gateway.

---

### Phase 2 — Identity & Users

**Goal:** Build the foundation that every other service depends on — authentication and user identity.

**Step 4: User Service**
Handles user registration, profiles, and channel management. Every other service will query or reference user data.

**Step 5: Auth Service**
Issues and validates JWT tokens. Integrates with User Service. The Gateway will later use this to validate incoming tokens before forwarding requests.

At the end of Phase 2, a user should be able to register, log in, and receive a JWT token. You should be able to make an authenticated request through the gateway.

---

### Phase 3 — Core Streaming

**Goal:** The main product feature — live video streaming.

**Step 6: Stream Service**
Manages stream lifecycle: create, start, stop, update stream metadata. This does NOT handle the actual video bytes — it handles the stream sessions (metadata, state, viewer count, etc.).

**Step 7: Media/Ingest Service**
Receives the raw RTMP video feed from a streaming client (e.g., OBS), pipes it through FFmpeg to transcode into HLS segments, and writes the .m3u8 playlist + .ts segments to MinIO/S3. This is the most complex service — build it last in this phase.

At the end of Phase 3, a user should be able to start a stream, push video via OBS/RTMP, and another client should be able to watch via an HLS URL.

---

### Phase 4 — Engagement Features

**Goal:** Add the social and interactive layer on top of the streaming core.

**Step 8: Chat Service**
Real-time chat using WebSocket (STOMP over WebSocket). Connects to a stream room. Stores chat history in MongoDB. Publishes chat events to Kafka for downstream services (e.g., moderation, notifications).

**Step 9: Notification Service**
Listens to Kafka topics (stream started, new follower, etc.) and sends notifications. Start with in-app notifications; optionally extend to email later.

**Step 10: Follow/Social Service**
Manages follower/following relationships between users. When a followed streamer goes live, this service triggers a Kafka event that Notification Service picks up.

---

### Phase 5 — Search & Discovery

**Step 11: Search Service**
Indexes streams, channels, and categories. Allows users to discover live and past streams. Use Elasticsearch or PostgreSQL full-text search to start.

---

### Phase 6 — Observability & Hardening

**Step 12: Add distributed tracing** (Zipkin/Micrometer Tracing) across all services.

**Step 13: Add Prometheus metrics** and Grafana dashboards.

**Step 14: Centralized logging** with ELK Stack or just structured JSON logs to stdout.

**Step 15: Circuit breakers** using Resilience4j in Gateway and any service making synchronous calls.

---

### Phase 7 — CI/CD & Deployment

**Step 16: Dockerize** every service with multi-stage Dockerfiles.

**Step 17: Docker Compose** for local full-stack development.

**Step 18: GitHub Actions pipelines** (build → test → Docker push → deploy).

**Step 19: Kubernetes manifests** — Deployments, Services, Ingress, ConfigMaps, Secrets, HPA.

---

## Services

### 1. Config Server
Central configuration server. All other services bootstrap from here.

**Responsibilities:**
- Serve `application.yml` configs per service and per environment (dev, prod)
- Store configs in a Git repository (or classpath for simplicity)
- Support config refresh via Spring Cloud Bus + Kafka

**Port:** `8888`

---

### 2. Eureka Discovery Server
Service registry where all microservices self-register.

**Responsibilities:**
- Accept heartbeats and registrations from all services
- Provide service location to API Gateway and inter-service calls
- Dashboard for live service health visualization

**Port:** `8761`

---

### 3. API Gateway
The sole entry point for all external client traffic.

**Responsibilities:**
- Route requests to downstream services using service names (not hardcoded URLs)
- JWT authentication filter — validate token before forwarding
- Rate limiting per user/IP
- Request logging and correlation ID injection
- CORS handling

**Port:** `8080`

---

### 4. User Service
Manages user accounts and public channel profiles.

**Responsibilities:**
- User registration and profile management
- Channel creation (every user gets a channel)
- Profile image upload (to MinIO)
- Expose user info as an API for other services (internal endpoints)

**Port:** `8081`
**Database:** PostgreSQL (`users_db`)

---

### 5. Auth Service
Handles all authentication and authorization concerns.

**Responsibilities:**
- Login endpoint — validate credentials, issue JWT access token + refresh token
- Refresh token endpoint
- Token validation endpoint (called by Gateway filter)
- OAuth2 integration (Google login) — optional, add later
- Password reset flow

**Port:** `8082`
**Database:** PostgreSQL (`auth_db`) — stores refresh tokens

---

### 6. Stream Service
Manages stream session lifecycle and metadata.

**Responsibilities:**
- Create a stream session (generate stream key for RTMP)
- Start / stop stream (update state)
- Store stream metadata (title, category, thumbnail)
- Track live viewer count (via Redis)
- Publish events to Kafka: `stream.started`, `stream.ended`
- Query currently live streams

**Port:** `8083`
**Database:** PostgreSQL (`streams_db`), Redis (viewer count cache)

---

### 7. Media / Ingest Service
Handles raw video ingestion and transcoding.

**Responsibilities:**
- Accept RTMP stream from OBS or any RTMP client (via Nginx-RTMP or a Java RTMP library)
- Validate stream key against Stream Service
- Invoke FFmpeg to transcode to HLS (multiple quality levels: 1080p, 720p, 480p)
- Write HLS segments (.ts files) and playlist (.m3u8) to MinIO/S3
- Notify Stream Service when ingest begins/ends

**Port:** `8084`
**Storage:** MinIO / S3

---

### 8. Chat Service
Real-time WebSocket-based chat per stream room.

**Responsibilities:**
- WebSocket endpoint using STOMP
- Message broadcasting to all room subscribers
- Persist chat messages to MongoDB
- Publish notable events to Kafka (e.g., moderation triggers)
- Support basic moderation: timeout, ban (via Stream Service's moderator role)

**Port:** `8085`
**Database:** MongoDB (`chat_db`)

---

### 9. Notification Service
Asynchronous notification delivery.

**Responsibilities:**
- Subscribe to Kafka topics: `stream.started`, `user.followed`, etc.
- Create in-app notification records
- Deliver email notifications (via Spring Mail + SMTP)
- Mark notifications as read

**Port:** `8086`
**Database:** PostgreSQL (`notifications_db`)

---

### 10. Follow / Social Service
Manages social graph between users.

**Responsibilities:**
- Follow / unfollow a channel
- Get followers list, following list
- Check if a user follows another (queried by other services)
- Publish `user.followed` event to Kafka

**Port:** `8087`
**Database:** PostgreSQL (`social_db`)

---

### 11. Search Service
Enables content and user discovery.

**Responsibilities:**
- Index streams, channels, and categories
- Full-text search across titles, descriptions, usernames
- Filter by live/VOD, category, language
- Consume Kafka events to keep index updated

**Port:** `8088`
**Database:** Elasticsearch or PostgreSQL FTS

---

## Data Models & Entities

### User Service

**User**
```
id              UUID        PK
username        VARCHAR     UNIQUE, NOT NULL
email           VARCHAR     UNIQUE, NOT NULL
passwordHash    VARCHAR     NOT NULL
displayName     VARCHAR
bio             TEXT
avatarUrl       VARCHAR
role            ENUM        [USER, MODERATOR, ADMIN]
createdAt       TIMESTAMP
updatedAt       TIMESTAMP
```

**Channel**
```
id              UUID        PK
userId          UUID        FK → User.id, UNIQUE
channelName     VARCHAR     UNIQUE
description     TEXT
bannerUrl       VARCHAR
isPartner       BOOLEAN     DEFAULT false
totalFollowers  INT         DEFAULT 0
createdAt       TIMESTAMP
```

---

### Auth Service

**RefreshToken**
```
id              UUID        PK
userId          UUID        NOT NULL (references User in User Service)
tokenHash       VARCHAR     NOT NULL
expiresAt       TIMESTAMP
revokedAt       TIMESTAMP   NULLABLE
createdAt       TIMESTAMP
```

---

### Stream Service

**Stream**
```
id              UUID        PK
channelId       UUID        FK → Channel.id
title           VARCHAR     NOT NULL
description     TEXT
category        VARCHAR
language        VARCHAR
thumbnailUrl    VARCHAR
streamKey       VARCHAR     UNIQUE, NOT NULL
status          ENUM        [IDLE, LIVE, ENDED]
viewerCount     INT         DEFAULT 0 (Redis cache, synced to DB on end)
startedAt       TIMESTAMP   NULLABLE
endedAt         TIMESTAMP   NULLABLE
createdAt       TIMESTAMP
```

**StreamCategory**
```
id              UUID        PK
name            VARCHAR     UNIQUE
slug            VARCHAR     UNIQUE
iconUrl         VARCHAR
description     TEXT
```

---

### Chat Service (MongoDB)

**ChatMessage**
```
_id             ObjectId    PK
streamId        String      (Stream.id)
userId          String      (User.id)
username        String      (denormalized for performance)
content         String
type            Enum        [TEXT, EMOTE, SYSTEM]
sentAt          Date
deletedAt       Date        Nullable
```

**ChatBan**
```
_id             ObjectId
streamId        String
bannedUserId    String
bannedByUserId  String
reason          String
expiresAt       Date        Nullable (null = permanent)
createdAt       Date
```

---

### Notification Service

**Notification**
```
id              UUID        PK
recipientId     UUID        NOT NULL (User.id)
type            ENUM        [STREAM_LIVE, NEW_FOLLOWER, SYSTEM]
title           VARCHAR
body            TEXT
referenceId     UUID        NULLABLE (stream id, user id, etc.)
isRead          BOOLEAN     DEFAULT false
createdAt       TIMESTAMP
```

---

### Follow / Social Service

**Follow**
```
id              UUID        PK
followerId      UUID        NOT NULL (User.id — the one who follows)
followeeId      UUID        NOT NULL (User.id — the channel being followed)
createdAt       TIMESTAMP
UNIQUE(followerId, followeeId)
```

---

### Media / Ingest Service (No persistent DB — stateless)

Tracks active ingest sessions in Redis only:

**IngestSession (Redis key: `ingest:{streamKey}`)**
```
streamId        String
channelId       String
status          String      [ACTIVE, PROCESSING]
hlsBaseUrl      String
startedAt       String (ISO timestamp)
TTL             3600 seconds
```

---

## Inter-Service Communication

### Synchronous (REST via Feign / WebClient)

| Caller | Callee | Purpose |
|---|---|---|
| API Gateway | Auth Service | Validate JWT token on every request |
| Auth Service | User Service | Verify user credentials on login |
| Media Service | Stream Service | Validate stream key on RTMP connect |
| Chat Service | User Service | Enrich chat message with display name |
| Follow Service | User Service | Validate user exists before follow |

**Rule:** Only use synchronous calls when you need a real-time response to return to the client. Keep these minimal. Always add a circuit breaker (Resilience4j) around each Feign client.

---

### Asynchronous (Kafka Topics)

| Topic | Producer | Consumers | Payload Summary |
|---|---|---|---|
| `stream.started` | Stream Service | Notification Service, Search Service | streamId, channelId, title |
| `stream.ended` | Stream Service | Search Service | streamId, viewerCount, duration |
| `user.followed` | Follow Service | Notification Service | followerId, followeeId |
| `user.registered` | User Service | Notification Service | userId, email |
| `chat.message.flagged` | Chat Service | (future: moderation) | messageId, content |

**Rule:** Use Kafka for anything where the producer does not need an immediate response. This decouples services and prevents cascading failures.

---

## Infrastructure & DevOps

### Local Development Stack (Docker Compose)

Run all infrastructure locally with Docker Compose before deploying anything to the cloud:

- PostgreSQL (one instance, multiple databases)
- MongoDB
- Redis
- Apache Kafka + Zookeeper
- MinIO (S3-compatible)
- Zipkin
- Prometheus + Grafana

All Spring Boot services run locally (outside Docker) during development. Only infrastructure runs in Docker locally.

---

### Kubernetes Deployment (Production)

Each service gets:
- A `Deployment` (with replica count and resource limits)
- A `Service` (ClusterIP for internal, LoadBalancer/Ingress for external)
- A `ConfigMap` (non-secret config)
- A `Secret` (DB passwords, JWT secret, API keys)
- A `HorizontalPodAutoscaler` (scale on CPU/custom metrics)

Ingress routes:
- `/api/**` → API Gateway
- `/hls/**` → Media/CDN Service or MinIO directly
- `/ws/**` → Chat Service (WebSocket upgrade)

---

## CI/CD Pipeline

### GitHub Actions Workflow (per service)

```
Trigger: Push to main or PR opened
│
├── Job 1: Build & Test
│     ├── Checkout code
│     ├── Set up Java 25
│     ├── Run: ./mvnw verify
│     └── Upload test reports
│
├── Job 2: Docker Build & Push (on main only)
│     ├── Build multi-stage Docker image
│     ├── Tag as :latest and :{git-sha}
│     └── Push to Docker Hub / GitHub Container Registry
│
└── Job 3: Deploy to Kubernetes (on main only)
      ├── Update K8s Deployment image tag
      └── kubectl rollout restart deployment/{service-name}
```

### Multi-Stage Dockerfile Pattern (all services)

```
Stage 1 — Builder: Use Eclipse Temurin JDK 25 image, compile and package the JAR
Stage 2 — Runtime: Use slim JRE 25 image, copy JAR from builder, expose port, run
```

This keeps final images small (no compiler, no source code).

---

## Spring Initializer Dependencies (Per Service)

### Config Server

- Spring Boot Actuator
- Config Server (`spring-cloud-config-server`)

---

### Eureka Discovery Server

- Spring Boot Actuator
- Eureka Server (`spring-cloud-starter-netflix-eureka-server`)

---

### API Gateway

- Gateway (`spring-cloud-starter-gateway`)
- Eureka Discovery Client (`spring-cloud-starter-netflix-eureka-client`)
- Config Client (`spring-cloud-starter-config`)
- Spring Boot Actuator
- Resilience4j (`spring-cloud-starter-circuitbreaker-resilience4j`)
- Spring Security (for JWT filter)

---

### User Service

- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Spring Boot Actuator
- Eureka Discovery Client
- Config Client
- Spring Security
- Springdoc OpenAPI (Swagger)
- Spring Kafka
- Lombok
- MapStruct (for DTO mapping)
- Validation (`spring-boot-starter-validation`)

---

### Auth Service

- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Spring Security
- OAuth2 Resource Server (`spring-boot-starter-oauth2-resource-server`)
- Spring Boot Actuator
- Eureka Discovery Client
- Config Client
- OpenFeign (`spring-cloud-starter-openfeign`) — to call User Service
- Lombok
- Validation

> Add `java-jwt` (Auth0) or `nimbus-jose-jwt` as a JWT library manually in `pom.xml` — not available in Spring Initializer directly.

---

### Stream Service

- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Spring Data Redis (Lettuce)
- Spring Kafka
- Spring Boot Actuator
- Eureka Discovery Client
- Config Client
- Spring Security (JWT resource server)
- Springdoc OpenAPI
- Lombok
- Validation

---

### Media / Ingest Service

- Spring Web
- Spring Data Redis
- Spring Kafka
- Spring Boot Actuator
- Eureka Discovery Client
- Config Client
- OpenFeign (to call Stream Service)
- Lombok

> Add MinIO Java SDK manually in `pom.xml` — not in Spring Initializer.
> FFmpeg is invoked as a system process; no dependency needed, but FFmpeg must be installed in the Docker image.

---

### Chat Service

- Spring Web
- WebSocket (`spring-boot-starter-websocket`)
- Spring Data MongoDB
- Spring Kafka
- Spring Boot Actuator
- Eureka Discovery Client
- Config Client
- Spring Security
- OpenFeign (to call User Service)
- Lombok

---

### Notification Service

- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Spring Kafka
- Spring Boot Actuator
- Spring Mail (`spring-boot-starter-mail`)
- Eureka Discovery Client
- Config Client
- Lombok
- Springdoc OpenAPI

---

### Follow / Social Service

- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Spring Kafka
- Spring Boot Actuator
- Eureka Discovery Client
- Config Client
- OpenFeign
- Spring Security
- Lombok
- Validation

---

### Search Service

- Spring Web
- Spring Data Elasticsearch (`spring-boot-starter-data-elasticsearch`) OR Spring Data JPA with PostgreSQL
- Spring Kafka
- Spring Boot Actuator
- Eureka Discovery Client
- Config Client
- Lombok
- Springdoc OpenAPI

---

## Key Patterns to Apply Consistently

**DTO Pattern:** Never expose JPA entities directly in REST responses. Always map to DTOs. Use MapStruct to generate mapping code.

**Event-Driven Updates:** When data changes in one service, publish a Kafka event. Other services update their own local cache or denormalized copies. Do not call synchronously for non-critical updates.

**Saga Pattern (eventual consistency):** For multi-step operations that span services (e.g., user deletion — delete streams, remove followers, revoke tokens), use the saga pattern via Kafka events rather than distributed transactions.

**Correlation IDs:** Inject a `X-Correlation-Id` header in the Gateway and propagate it through every downstream service and log entry. This makes tracing requests across services possible.

**Health Checks:** Every service exposes `/actuator/health` and `/actuator/info`. Gateway and K8s liveness/readiness probes use these.

**Externalized Secrets:** Never commit passwords, JWT secrets, or API keys to Git. Use environment variables in Docker, Kubernetes Secrets in K8s, and Spring Cloud Config with encrypted values.

---

## Project Repository Structure

```
livestream-platform/
├── infrastructure/
│   ├── config-server/
│   ├── eureka-server/
│   └── api-gateway/
├── services/
│   ├── user-service/
│   ├── auth-service/
│   ├── stream-service/
│   ├── media-service/
│   ├── chat-service/
│   ├── notification-service/
│   ├── follow-service/
│   └── search-service/
├── k8s/
│   ├── infrastructure/
│   └── services/
├── docker-compose.yml          ← Local infra (Kafka, Redis, Postgres, etc.)
├── docker-compose.services.yml ← Optional: run all services in Docker locally
└── .github/
    └── workflows/
        └── {service}-ci.yml    ← One workflow per service
```

---

*Built with Spring Boot 4 · Java 25 · Microservices · Event-Driven · Cloud-Native*
