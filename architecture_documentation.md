# Streaming Platform: Exhaustive Developer & Architecture Guide

This document is the absolute, most exhaustive technical blueprint of the live streaming platform. It covers the entire microservices architecture, internal database schemas, inter-service communications, streaming infrastructure, real-time WebSocket implementations, and deployment strategies. Use this as your primary study material for engineering interviews.

---

## 1. Executive Summary & Overview

The platform is a fully distributed, scalable live-streaming application built on the **Spring Boot Microservices** ecosystem. 

**Core Philosophies:**
- **Zero-Trust Edge:** The API Gateway acts as an iron-clad barrier. No internal services are directly exposed to the internet.
- **Database-per-Service:** Each microservice has its own isolated PostgreSQL database schema.
- **Event-Driven & Webhook Architecture:** Heavy reliance on MediaMTX webhooks for stream state management.
- **Configuration as Code:** All environment configurations are centrally managed in a dedicated Git repository.

---

## 2. Technology Stack & Dependencies

- **Core:** Java 25, Spring Boot 4.1.0, Spring Cloud 2025.1.2
- **Infrastructure Core:**
  - **API Gateway:** Spring Cloud Gateway
  - **Service Registry:** Spring Cloud Netflix Eureka
  - **Configuration Management:** Spring Cloud Config Server (Git-backed)
- **Data Persistence:** PostgreSQL (with `spring-boot-starter-data-jpa`)
- **Database Migrations:** Flyway (`flyway-database-postgresql`)
- **Security:** Spring Security, OAuth2 Resource Server, JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- **Inter-Service Communication:** Spring Cloud OpenFeign
- **Real-Time Messaging:** Spring WebSocket (STOMP)
- **Streaming Core:** MediaMTX (RTMP/HLS Server)
- **Video Transcoding:** FFmpeg (OS Process managed via Java)
- **Boilerplate Reduction:** Lombok

---

## 3. Infrastructure & Deployment Architecture

### 3.1 The Configuration Server (Git-Backed)
Instead of having 10 different `application.yml` files scattered across repositories, the platform uses a **Spring Cloud Config Server** (running on port `8888`).
- The Config Server points to a central Git repository: `KaranPushes/Streaming-Config-Repo`.
- When a microservice (e.g., `feed-service`) boots up, it fetches its configuration (`feed-service.yml`) from the Config Server.
- This centralizes secrets (like the `jwt.secret-key`), database URLs, and Flyway configurations.

### 3.2 Service Discovery (Eureka)
- **Eureka Server** runs on `http://localhost:8761`.
- Every microservice acts as a Eureka Client. On startup, they register their host and dynamic port with Eureka.
- **Why this matters:** When the API Gateway routes a request to `/users/**`, it doesn't need to know the User Service's IP address. It asks Eureka for `USER-SERVICE` and routes traffic accordingly using load-balanced URLs (`lb://USER-SERVICE`).

### 3.3 The API Gateway Routing Rules
The Gateway (`api-gateway.yml`) defines the exact edge-routing configuration:
- `Path=/auth/**` -> `lb://AUTH-SERVICE` (Port 8082)
- `Path=/users/**` -> `lb://USER-SERVICE` (Port 8081)
- `Path=/channels/**` -> `lb://CHANNEL-SERVICE` (Port 8083)
- `Path=/follows/**` -> `lb://FOLLOW-SERVICE` (Port 8084)
- `Path=/media/**` -> `lb://MEDIA-SERVICE` (Port 8085)
- `Path=/streams/**` -> `lb://STREAM-SERVICE` (Port 8086)
- `Path=/api/chat/**` -> `lb://CHAT-SERVICE` (Port 8088)
- `Path=/api/notifications/**` -> `lb://NOTIFICATION-SERVICE` (Port 8089)
- `Path=/api/feed/**` -> `lb://FEED-SERVICE` (Port 8091)

---

## 4. Service Deep Dives & Database Schemas

### 4.1 Auth Service (`port: 8082`)
- **Responsibilities:** User registration, authentication, JWT issuing, password resets, email verification, and session management.
- **Database (`streaming_auth`):**
  - `auth_users`: `id`, `password_hash`, `email_verified`, `last_login_at`, `failed_login_attempts`, `account_locked_until`, `role`.
  - `user_sessions`: `id`, `user_id`, `refresh_token_hash`, `expires_at`, `revoked`, `device_name`, `browser`, `operating_system`, `ip_address`.
  - `password_reset_tokens`, `email_verification_tokens`.
- **Note:** Refresh tokens are securely hashed (`refresh_token_hash`) in the database, preventing session hijacking if the DB is compromised.

### 4.2 User Service (`port: 8081`)
- **Responsibilities:** Public and private user profile data.
- **Database (`streaming_users`):**
  - `users`: `id`, `username`, `display_name`, `email`, `status`, `bio`, `location`, `website_url`, `avatar_media_id`, `banner_media_id`.

### 4.3 Channel Service (`port: 8083`)
- **Responsibilities:** Broadcaster identity management.
- **Database (`streaming_channels`):**
  - `channels`: `id`, `owner_user_id`, `slug`, `display_name`, `description`, `avatar_url`, `banner_url`, `status`.

### 4.4 Follow Service (`port: 8084`)
- **Responsibilities:** The social graph.
- **Database (`streaming_follows`):**
  - `follows`: `id`, `user_id`, `channel_id`, `created_at`. (Unique constraint on user_id + channel_id).

### 4.5 Media Service (`port: 8085`)
- **Responsibilities:** Handles multipart file uploads (avatars, thumbnails). Uses an `ObjectStorageService` abstraction to save files to disk/cloud storage.
- **Database (`streaming_media`):**
  - `media`: `id`, `owner_user_id`, `media_type`, `original_file_name`, `storage_key`, `url`, `content_type`, `file_size`, `status`.

### 4.6 Stream Service (`port: 8086`)
- **Responsibilities:** Managing the lifecycle of a broadcast.
- **Database (`streaming_streams`):**
  - `streams`: `id`, `channel_id`, `title`, `description`, `category`, `thumbnail_url`, `status` (SCHEDULED, LIVE, ENDED), `scheduled_at`, `started_at`, `ended_at`.
- **Inter-Service Dependencies:** Calls `ChannelService` (to verify channel ownership), `TranscoderService` (to start/stop FFmpeg).

### 4.7 Transcoder Service (`port: 8087`)
- **Responsibilities:** A stateless worker. Exposes `/internal/transcoders/start` and `/stop`. Spawns FFmpeg OS processes via `ProcessBuilder` to convert RTMP streams into adaptive bitrate HLS.
- **Database:** None (Stateless).

### 4.8 Chat Service (`port: 8088`)
- **Responsibilities:** Real-time STOMP WebSocket communication, chat history, and moderation.
- **Database (`streaming_chat`):**
  - `chat_messages`: `id`, `stream_id`, `user_id`, `message`, `deleted`.
  - `chat_moderators`: `id`, `stream_id`, `user_id`.
  - `chat_moderations`: `id`, `stream_id`, `user_id`, `type`, `expires_at`.
- **Logic:** Implements a `ChatRateLimiter` to prevent spam, and checks `chat_moderations` to see if a user is banned before accepting a `/app/chat.send` WebSocket frame.

### 4.9 Notification Service (`port: 8089`)
- **Responsibilities:** Real-time alerts via STOMP WebSocket and maintaining an inbox.
- **Database (`streaming_notification`):**
  - `notifications`: `id`, `user_id`, `type`, `title`, `message`, `reference_id`, `read`.

### 4.10 Feed Service (`port: 8091`)
- **Responsibilities:** Aggregator for the frontend (Home Page, Search, Categories). Prevents the frontend from making complex joins across microservices (API Composition Pattern).
- **Database (`streaming_feed`):** Used primarily for caching or materialized views.
- **Inter-Service Dependencies:** Calls `StreamService` and `ChannelService`.

---

## 5. Security & Authentication Architecture

### 5.1 JWT Implementation
- **Creation:** The `auth-service` generates an HMAC-SHA256 signed JWT upon login.
- **Claims:** The JWT contains the `sub` (userId) and `roles` (e.g., `ROLE_USER`).
- **Validation (Decentralized):** Every service utilizes Spring Security's `oauth2ResourceServer`. Because all services fetch the `jwt.secret-key` from the Config Server, they can validate the token's cryptographic signature locally without needing to ping the Auth Service. This vastly improves performance.

### 5.2 Internal API Security (`/internal/**`)
- To prevent external actors from bypassing business logic, all endpoints prefixed with `/internal/**` are **hard-blocked by the API Gateway**.
- When microservices communicate with each other using Spring Cloud OpenFeign (e.g., Stream Service calling Transcoder Service), they pass a specialized JWT that grants the `ROLE_SERVICE` authority. 
- The target service verifies the token and strictly demands `.hasRole("SERVICE")` for any `/internal/**` path.

---

## 6. Live Streaming Infrastructure (In-Depth Sequence)

The video pipeline relies on **MediaMTX** (a highly efficient RTMP/HLS server) and the backend **Stream Service**.

1. **Ingest & Authentication:**
   - Broadcaster streams to `rtmp://<server>:1935/live/<stream-key>`.
   - MediaMTX triggers a webhook, sending a POST request to `http://api-gateway:8080/internal/streams/authenticate`.
   - The Stream Service validates the `stream-key`. If valid, it returns HTTP 200, and MediaMTX accepts the stream.
2. **Lifecycle Webhooks:**
   - MediaMTX fires `/internal/streams/{streamKey}/publish` when data actually starts flowing.
   - The Stream Service updates the DB status to `LIVE`.
3. **Transcoding Initiation:**
   - Upon receiving the publish webhook, the Stream Service synchronously calls the Transcoder Service via Feign: `POST /internal/transcoders/start`.
   - The Transcoder Service launches an FFmpeg instance targeting the MediaMTX stream, outputting multiple resolutions (1080p, 720p).
4. **Playback:**
   - MediaMTX serves an HLS playlist (e.g., `http://<server>:8090/live/<stream-key>/index.m3u8`).
   - The frontend reads the `playbackUrl` from the Stream Service and feeds it to an HLS-compatible video player.
5. **Termination:**
   - When the broadcaster disconnects, MediaMTX fires `/internal/streams/{streamKey}/unpublish`.
   - The Stream Service updates the DB to `ENDED` and tells the Transcoder Service to kill the FFmpeg process.

---

## 7. Real-Time Interactions (WebSockets & STOMP)

The platform avoids raw WebSockets in favor of **STOMP** (Simple Text Oriented Messaging Protocol).

- **Connection Endpoint:** `/ws/chat` or `/ws/notifications`.
- **Authentication:** `StompAuthInterceptor` hooks into the initial `CONNECT` frame, extracts the `Authorization` header, and validates the JWT before establishing the session.
- **Channels (Chat):**
  - Clients subscribe to `/topic/streams/{streamId}/chat`.
  - Clients send messages to the application destination `/app/chat.send`.
  - The `StreamChatController` handles the message, saves it to PostgreSQL, and broadcasts it to the `/topic` queue.
- **Channels (Notifications):**
  - Clients subscribe to `/topic/users/{userId}/notifications`.
  - The internal `NotificationEventService` listens for system events (e.g., `NewFollowerEvent`), saves the notification to the DB, and pushes it to the specific user's topic.

---

## 8. Event-Driven Workflows & Notifications

How does a complex, cross-service action happen? Take the **Follow Action**:

1. User A hits `POST /follows/channels/{channelId}`.
2. API Gateway validates the JWT and routes to `Follow Service`.
3. `Follow Service` saves the follow record in `streaming_follows`.
4. `Follow Service` needs to notify the channel owner. It uses Feign to call `POST /internal/notifications/events/follower` on the `Notification Service`.
5. `Notification Service` creates a notification in `streaming_notification` and pushes a STOMP message to `/topic/users/{channelOwnerId}/notifications`.
6. User B (the owner) sees a real-time alert pop up in their browser.

---

## 9. Error Handling & Resiliency

- **Global Exception Handling:** Every service implements `@ControllerAdvice` to catch exceptions (e.g., `ChannelNotFoundException`) and return standardized JSON error responses.
- **Database Migrations:** Every service uses **Flyway**. During application startup, Spring Boot automatically executes any pending migrations (`V1__initial_schema.sql`, etc.) against the PostgreSQL database. This ensures database schemas are strictly version-controlled alongside the application code.
- **Rate Limiting:** Implemented at the application layer (e.g., `ChatRateLimiter`) to prevent abuse of the WebSocket connections. Future iterations would move this to the API Gateway using Redis for global rate limiting.
