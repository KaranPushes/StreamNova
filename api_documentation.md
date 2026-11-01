# Streaming Platform Backend API

## 1. Overview
This document provides the API specifications for the live streaming platform backend (v0.14.0). It covers the public Gateway-exposed REST APIs, WebSocket connections for Chat and Notifications, and internal/infrastructure APIs.

## 2. Architecture
The backend is a Java/Spring Boot microservices architecture.
- **Frontend / Browser** communicates exclusively with the **API Gateway** on port `8080`.
- The **API Gateway** routes requests to the appropriate backend microservices and handles base security.
- Internal services communicate with each other using a `SERVICE` role JWT, typically resolved via **Eureka** (service discovery) on port `8761`.
- Live streaming data flows from the Broadcaster (OBS) via RTMP to **MediaMTX** (port `8090`), which triggers the transcoding pipeline (**FFmpeg**) for adaptive bitrates (1080p, 720p, 480p). MediaMTX generates HLS streams for the Browser/Player and triggers webhooks to the backend for stream lifecycle management.

## 3. Base URLs
- **API Gateway (Frontend Target):** `http://localhost:8080`
- **HLS Playback (MediaMTX):** `http://localhost:8090`
- **WebSocket (Chat via Gateway):** `ws://localhost:8080/api/chat` (routed to `8088`)
- **WebSocket (Notifications via Gateway):** `ws://localhost:8080/api/notifications` (routed to `8089`)

## 4. Authentication
Most APIs require a JWT access token sent in the `Authorization` header.
**Format:** `Authorization: Bearer <JWT>`
Service-to-service communication uses service JWTs and should never be used by the frontend.

---

## 5. Auth APIs
**Service:** Auth Service (`/auth/**`)

- **POST /auth/register**
  - **Public/Auth:** Public
  - **Purpose:** Register a new user.
  - **Request Body:** `{ "email": "user@example.com", "password": "password123", "username": "user123", "displayName": "User Name" }`
  - **Response:** `RegisterResponse` (userId, message)
- **POST /auth/login**
  - **Public/Auth:** Public
  - **Purpose:** Authenticate and obtain JWTs.
  - **Request Body:** `{ "email": "user@example.com", "password": "password123" }`
  - **Response:** `LoginResponse` (accessToken, refreshToken, tokenType, expiresIn)
- **GET /auth/me**
  - **Public/Auth:** Authenticated
  - **Purpose:** Get the current authenticated user's principal data.
- **POST /auth/refresh**
  - **Public/Auth:** Public (requires refresh token)
  - **Purpose:** Get a new access token.
  - **Request Body:** `{ "refreshToken": "..." }`
  - **Response:** `RefreshTokenResponse`
- **POST /auth/logout**
  - **Public/Auth:** Authenticated
  - **Purpose:** Revoke the refresh token.
  - **Request Body:** `{ "refreshToken": "..." }`
- **POST /auth/verify-email**
  - **Public/Auth:** Public
  - **Purpose:** Verify email address using the token.
  - **Request Body:** `{ "token": "..." }`
- **POST /auth/resend-verification**
  - **Public/Auth:** Public
  - **Purpose:** Resend email verification.
  - **Request Body:** `{ "email": "user@example.com" }`
- **POST /auth/change-password**
  - **Public/Auth:** Authenticated
  - **Purpose:** Change the current user's password.
  - **Request Body:** `{ "currentPassword": "...", "newPassword": "..." }`
- **POST /auth/forgot-password**
  - **Public/Auth:** Public
  - **Purpose:** Request a password reset email.
  - **Request Body:** `{ "email": "user@example.com" }`
- **POST /auth/reset-password**
  - **Public/Auth:** Public
  - **Purpose:** Reset password using a token.
  - **Request Body:** `{ "token": "...", "newPassword": "..." }`
- **GET /auth/sessions**
  - **Public/Auth:** Authenticated
  - **Purpose:** Get active sessions for the user.
  - **Response:** List of `SessionSummary`
- **DELETE /auth/sessions/{sessionId}**
  - **Public/Auth:** Authenticated
  - **Purpose:** Revoke a specific session.
- **DELETE /auth/sessions/others**
  - **Public/Auth:** Authenticated
  - **Purpose:** Revoke all other active sessions.
- **DELETE /auth/sessions**
  - **Public/Auth:** Authenticated
  - **Purpose:** Revoke all sessions.

---

## 6. User APIs
**Service:** User Service (`/users/**`)

- **GET /users/me**
  - **Public/Auth:** Authenticated
  - **Purpose:** Get current user profile.
- **GET /users/profile/{username}**
  - **Public/Auth:** Public
  - **Purpose:** Get a user's public profile.
- **PATCH /users/me/profile**
  - **Public/Auth:** Authenticated
  - **Purpose:** Update user profile (username, displayName, bio, websiteUrl, location).
- **PATCH /users/me/avatar**
  - **Public/Auth:** Authenticated
  - **Purpose:** Update user avatar from an uploaded media ID.
  - **Request Body:** `{ "mediaId": "UUID" }`
- **DELETE /users/me/avatar**
  - **Public/Auth:** Authenticated
  - **Purpose:** Delete avatar.
- **PATCH /users/me/banner**
  - **Public/Auth:** Authenticated
  - **Purpose:** Update user banner from an uploaded media ID.
  - **Request Body:** `{ "mediaId": "UUID" }`
- **DELETE /users/me/banner**
  - **Public/Auth:** Authenticated
  - **Purpose:** Delete banner.
- **GET /users/availability**
  - **Public/Auth:** Public
  - **Purpose:** Check if a username is available.
  - **Query Params:** `username`
- **GET /users/me/profile-completion**
  - **Public/Auth:** Authenticated
  - **Purpose:** Get profile completion percentage.
- **GET /users/search**
  - **Public/Auth:** Public
  - **Purpose:** Search for users.
  - **Query Params:** `q`
- **DELETE /users/me**
  - **Public/Auth:** Authenticated
  - **Purpose:** Deactivate the user account.

---

## 7. Channel APIs
**Service:** Channel Service (`/channels/**`)

- **POST /channels**
  - **Public/Auth:** Authenticated
  - **Purpose:** Create a channel for the current user.
  - **Request Body:** `{ "slug": "my-channel", "displayName": "My Channel" }`
- **GET /channels/me**
  - **Public/Auth:** Authenticated
  - **Purpose:** Get the current user's channel.
- **PATCH /channels/me**
  - **Public/Auth:** Authenticated
  - **Purpose:** Update the current user's channel.
- **DELETE /channels/me**
  - **Public/Auth:** Authenticated
  - **Purpose:** Deactivate the channel.
- **GET /channels/availability**
  - **Public/Auth:** Public
  - **Purpose:** Check if a channel slug is available.
  - **Query Params:** `slug`
- **GET /channels/profile/{slug}**
  - **Public/Auth:** Public
  - **Purpose:** Get a channel's public profile.
- **POST /channels/me/reactivate**
  - **Public/Auth:** Authenticated
  - **Purpose:** Reactivate a deactivated channel.
- **PATCH /channels/me/avatar**
  - **Public/Auth:** Authenticated
  - **Purpose:** Set the channel's avatar.
- **DELETE /channels/me/avatar**
  - **Public/Auth:** Authenticated
  - **Purpose:** Delete the channel's avatar.
- **PATCH /channels/me/banner**
  - **Public/Auth:** Authenticated
  - **Purpose:** Set the channel's banner.
- **DELETE /channels/me/banner**
  - **Public/Auth:** Authenticated
  - **Purpose:** Delete the channel's banner.
- **GET /channels/search**
  - **Public/Auth:** Public
  - **Purpose:** Search for channels.
  - **Query Params:** `q`, `page`, `size`

---

## 8. Follow APIs
**Service:** Follow Service (`/follows/**`)

- **POST /follows/channels/{channelId}**
  - **Public/Auth:** Authenticated
  - **Purpose:** Follow a channel.
- **DELETE /follows/channels/{channelId}**
  - **Public/Auth:** Authenticated
  - **Purpose:** Unfollow a channel.
- **GET /follows/channels/{channelId}/status**
  - **Public/Auth:** Authenticated
  - **Purpose:** Check if the current user follows the channel.
- **GET /follows/channels/{channelId}/count**
  - **Public/Auth:** Public
  - **Purpose:** Get a channel's follower count.
- **GET /follows/me**
  - **Public/Auth:** Authenticated
  - **Purpose:** Get a list of channels the current user follows.
  - **Query Params:** `page`, `size`

---

## 9. Media APIs
**Service:** Media Service (`/media/**`)

- **POST /media/avatar**
  - **Public/Auth:** Authenticated
  - **Purpose:** Upload an avatar image. Returns a `mediaId` to use in User/Channel APIs.
  - **Request Type:** `multipart/form-data` with `file`
- **POST /media/banner**
  - **Public/Auth:** Authenticated
  - **Purpose:** Upload a banner image.
  - **Request Type:** `multipart/form-data` with `file`
- **POST /media/stream-thumbnail**
  - **Public/Auth:** Authenticated
  - **Purpose:** Upload a stream thumbnail.
  - **Request Type:** `multipart/form-data` with `file`
- **DELETE /media/{id}**
  - **Public/Auth:** Authenticated
  - **Purpose:** Delete an uploaded media file.
- **GET /media/files/{id}**
  - **Public/Auth:** Public
  - **Purpose:** Serve the media file directly (image viewing).

---

## 10. Stream APIs
**Service:** Stream Service (`/streams/**`)

- **POST /streams**
  - **Public/Auth:** Authenticated
  - **Purpose:** Create a new stream.
  - **Request Body:** `{ "title": "...", "description": "...", "category": "...", "thumbnailId": "UUID", "scheduledAt": "ISO Date" }`
- **PATCH /streams/{streamId}**
  - **Public/Auth:** Authenticated
  - **Purpose:** Update stream details.
- **POST /streams/{streamId}/schedule**
  - **Public/Auth:** Authenticated
  - **Purpose:** Schedule a stream.
- **GET /streams/{streamId}/can-moderate/{userId}**
  - **Public/Auth:** Authenticated
  - **Purpose:** Check if a user can moderate the stream.
- **POST /streams/{streamId}/start**
  - **Public/Auth:** Authenticated
  - **Purpose:** Start the stream (transition to LIVE).
- **POST /streams/{streamId}/end**
  - **Public/Auth:** Authenticated
  - **Purpose:** End the live stream.
- **GET /streams/{streamId}**
  - **Public/Auth:** Public
  - **Purpose:** Get stream details (including playback URL if LIVE).
- **GET /streams/channel/{channelId}**
  - **Public/Auth:** Public
  - **Purpose:** Get a channel's stream history.
- **GET /streams/live**
  - **Public/Auth:** Public
  - **Purpose:** Get live streams directly (usually prefer Feed APIs).

---

## 11. Chat APIs
**Service:** Chat Service (`/api/chat/**` & `/ws/chat`)

**REST APIs:**
- **GET /api/chat/streams/{streamId}/messages**
  - **Public/Auth:** Authenticated
  - **Purpose:** Get recent chat history for a stream.
- **DELETE /api/chat/messages/{messageId}**
  - **Public/Auth:** Authenticated
  - **Purpose:** Delete your own message.
- **POST /api/chat/streams/{streamId}/moderators/{userId}**
  - **Public/Auth:** Authenticated (Stream Owner)
  - **Purpose:** Add a moderator.
- **DELETE /api/chat/streams/{streamId}/moderators/{userId}**
  - **Public/Auth:** Authenticated (Stream Owner)
  - **Purpose:** Remove a moderator.
- **DELETE /api/chat/streams/{streamId}/messages/{messageId}**
  - **Public/Auth:** Authenticated (Moderator/Owner)
  - **Purpose:** Delete a user's message as a moderator.
- **POST /api/chat/streams/{streamId}/moderation**
  - **Public/Auth:** Authenticated (Moderator/Owner)
  - **Purpose:** Ban or mute a user.
  - **Request Body:** `{ "userId": "UUID", "action": "MUTE|BAN", "expiresAt": "ISO Date" }`
- **DELETE /api/chat/streams/{streamId}/moderation/{userId}/{action}**
  - **Public/Auth:** Authenticated (Moderator/Owner)
  - **Purpose:** Remove a ban or mute.

**WebSocket API (`ws://localhost:8080/ws/chat`):**
- **Connection:** STOMP over WebSocket. Requires valid JWT in `Authorization` header during connect (handled by `stompAuthInterceptor`).
- **Send Message:** Destination `/app/chat.send`. Payload: `{ "streamId": "UUID", "message": "hello" }`.
- **Subscribe to Chat:** Destination `/topic/streams/{streamId}/chat`.
- **Subscribe to Deletions:** Destination `/topic/streams/{streamId}/chat.deleted`.
- **Subscribe to Errors:** Destination `/topic/streams/{streamId}/chat.errors` & `/topic/chat.errors`.

---

## 12. Notification APIs
**Service:** Notification Service (`/api/notifications/**` & `/ws/notifications`)

**REST APIs:**
- **GET /api/notifications**
  - **Public/Auth:** Authenticated
  - **Purpose:** Get a paginated list of notifications.
  - **Query Params:** `page`, `size`, `unreadOnly`
- **GET /api/notifications/unread-count**
  - **Public/Auth:** Authenticated
  - **Purpose:** Get the count of unread notifications.
- **PATCH /api/notifications/{notificationId}/read**
  - **Public/Auth:** Authenticated
  - **Purpose:** Mark a specific notification as read.
- **PATCH /api/notifications/read-all**
  - **Public/Auth:** Authenticated
  - **Purpose:** Mark all notifications as read.

**WebSocket API (`ws://localhost:8080/ws/notifications`):**
- **Connection:** STOMP over WebSocket. Requires JWT.
- **Subscribe to Notifications:** Destination `/topic/users/{userId}/notifications`.

---

## 13. Feed / Discovery APIs
**Service:** Feed Service (`/api/feed/**`)

- **GET /api/feed/home**
  - **Public/Auth:** Public
  - **Purpose:** Get the general home feed.
- **GET /api/feed/live**
  - **Public/Auth:** Public
  - **Purpose:** Get live streams.
  - **Query Params:** `category`, `q`, `page`, `size`, `sort`
- **GET /api/feed/channels**
  - **Public/Auth:** Public
  - **Purpose:** Search channels via the feed service.
  - **Query Params:** `q`, `page`, `size`
- **GET /api/feed/categories**
  - **Public/Auth:** Public
  - **Purpose:** Get active stream categories and counts.
- **GET /api/feed/categories/{category}**
  - **Public/Auth:** Public
  - **Purpose:** Get live streams in a specific category.
- **GET /api/feed/personalized**
  - **Public/Auth:** Authenticated
  - **Purpose:** Get a personalized feed based on user preferences/follows.
- **GET /api/feed/streams/{streamId}**
  - **Public/Auth:** Public
  - **Purpose:** Get stream details for the feed.
- **GET /api/feed/search**
  - **Public/Auth:** Public
  - **Purpose:** Unified search (returns channels, streams, and categories).
  - **Query Params:** `q`, `page`, `size`

---

## 14. Internal APIs
**Backend/internal only — frontend must not call these.**
Blocked by the Gateway via `Path=/internal/**`. Used for service-to-service communication.

- **User Service:** `/internal/users`, `/internal/users/{id}`, `/internal/users/{id}/status`
- **Channel Service:** `/internal/channels/{id}`, `/internal/channels/{id}/status`, `/internal/channels/by-owner`, `/internal/channels/search`, `/internal/channels/active`, `/internal/channels/by-ids`, `/internal/channels/{id}/owner/{userId}`
- **Follow Service:** `/internal/channels/{channelId}/followers`, `/internal/follows/user/{userId}`
- **Media Service:** `/internal/media/{id}`
- **Stream Service:** `/internal/streams/live`, `/internal/streams/live/by-channels`, `/internal/streams/live/channel-counts`, `/internal/streams/categories`, `/internal/streams/{id}`
- **Notification Service:** `/internal/notifications/events/follower`, `/internal/notifications/events/stream-live`, `/internal/notifications/events/stream-scheduled`, `/internal/notifications/events/security`
- **Transcoder Service:** `/internal/transcoders/start`, `/internal/transcoders/stop`

---

## 15. MediaMTX / Streaming Callbacks
**Infrastructure APIs, not frontend APIs.**
MediaMTX hits these endpoints via webhooks to authenticate RTMP pushes and manage the stream lifecycle.
- **POST /internal/streams/authenticate**
  - Hit by MediaMTX during RTMP publish (`on_publish`). Validates the stream key.
- **POST /internal/streams/{streamKey}/publish**
  - Notifies the Stream Service that the stream has officially started streaming data.
- **POST /internal/streams/{streamKey}/unpublish**
  - Notifies the Stream Service that the stream data has stopped.

---

## 16. Error Handling
- Standard Spring Boot error responses generally include a timestamp, status, error, and message.
- Exceptions throw appropriate HTTP status codes (e.g., 400 for validation errors, 401 for unauthorized, 403 for forbidden, 404 for not found).
- Chat WebSocket errors are sent to `/topic/chat.errors` and `/topic/streams/{streamId}/chat.errors` with `{ "code": "...", "message": "..." }`.

---

## 17. Pagination
Endpoints returning lists generally utilize a unified `PageResponse<T>` structure:
```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

---

## 18. API Security Matrix

| Endpoint | Method | Public/Auth | Role | Purpose |
|----------|--------|-------------|------|---------|
| `/auth/login` | POST | Public | - | Login |
| `/auth/register` | POST | Public | - | Registration |
| `/users/profile/**` | GET | Public | - | View public profiles |
| `/channels/profile/**` | GET | Public | - | View public channels |
| `/streams/live` | GET | Public | - | View public live streams |
| `/api/feed/home` | GET | Public | - | View general feed |
| `/media/files/**` | GET | Public | - | Access public media |
| `/users/me` | GET | Auth | USER | View own profile |
| `/channels/me` | GET/PATCH | Auth | USER | Manage own channel |
| `/streams` | POST | Auth | USER | Create a stream |
| `/internal/**` | ALL | Internal | SERVICE | Service-to-Service comms |

---

## 19. Frontend API Cheat Sheet

| Frontend Feature | API Endpoint(s) |
|------------------|-----------------|
| **Login** | `POST /auth/login` |
| **Register** | `POST /auth/register` |
| **Home Feed** | `GET /api/feed/home` |
| **Live Streams** | `GET /api/feed/live` |
| **Personalized Feed**| `GET /api/feed/personalized` |
| **Search** | `GET /api/feed/search` |
| **User Profile** | `GET /users/profile/{username}` |
| **Edit Profile** | `PATCH /users/me/profile`, `PATCH /users/me/avatar` |
| **Channel Profile** | `GET /channels/profile/{slug}` |
| **Edit Channel** | `PATCH /channels/me`, `PATCH /channels/me/avatar` |
| **Stream Page** | `GET /api/feed/streams/{streamId}` or `GET /streams/{id}` |
| **Start Stream** | `POST /streams/{streamId}/start` |
| **Follow Channel** | `POST /follows/channels/{channelId}` |
| **Chat** | `ws://localhost:8080/ws/chat` |
| **Notifications** | `ws://localhost:8080/ws/notifications`, `GET /api/notifications` |
| **Media Upload** | `POST /media/avatar`, `POST /media/banner`, `POST /media/stream-thumbnail` |
| **Media Serving** | `GET /media/files/{id}` |

---

### Unverified / Not confirmed from source:
- Frontend specific URLs for stream playback (Playback URL is returned in StreamResponse when LIVE, but frontend must parse and use it via an HLS player like video.js or hls.js).
- Complete Chat/Notification connection handshakes on the client side (Standard STOMP over SockJS/WebSocket is expected).
