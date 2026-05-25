# CP Player - Complete API Endpoints Reference

## 🌐 Base URL
```
https://api.classplusapp.com
```

---

## 🔐 Authentication Endpoints

### 1. Login
```
POST /auth/login

Request:
{
  "email": "user@example.com",
  "password": "password"
}

Response (200):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "user_id",
    "email": "user@example.com",
    "name": "User Name"
  }
}
```

### 2. Refresh Token
```
POST /auth/refresh

Request:
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response (200):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. Logout
```
POST /auth/logout

Headers:
  Authorization: Bearer {accessToken}

Response (200):
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

## 🎬 Video Endpoints

### 1. Get Signed Stream URL (PRIMARY)
```
GET /cams/uploader/video/jw-signed-url?contentId={contentId}

Headers:
  x-access-token: {token}
  Authorization: Bearer {token}
  User-Agent: Classplus/4.5.9 (Android 13)

Response (200):
{
  "drmUrls": {
    "manifestUrl": "https://example.com/stream.mpd",
    "licenseUrl": "https://widevine-license-server.com/...",
    "pssh": "AAAAOHBzcw..."
  },
  "contentId": "5f4a8c2b9d7e1f3a",
  "duration": 3600,
  "quality": "hd"
}

Error (500):
{
  "error": "contentHashId is null",
  "code": "MEDIA_NOT_FOUND"
}
```

### 2. Get M3U8 Stream (FALLBACK 1)
```
GET /cams/uploader/video/{contentId}/stream.m3u8

Headers:
  x-access-token: {token}
  User-Agent: Classplus/4.5.9 (Android 13)

Response (200):
#EXTM3U
#EXT-X-VERSION:3
#EXT-X-TARGETDURATION:10
#EXTINF:10.0,
segment-1.ts
#EXTINF:10.0,
segment-2.ts
...
#EXT-X-ENDLIST
```

### 3. Get HLS Playlist (FALLBACK 2)
```
GET /cams/uploader/video/{contentId}/playlist.m3u8

Headers:
  x-access-token: {token}
  User-Agent: Classplus/4.5.9 (Android 13)

Response (200):
#EXTM3U
#EXT-X-STREAM-INF:BANDWIDTH=1280000
stream-1280k.m3u8
#EXT-X-STREAM-INF:BANDWIDTH=2560000
stream-2560k.m3u8
```

### 4. Get Signed URL (FALLBACK 3)
```
GET /cams/uploader/video/{contentId}/signed-url

Headers:
  Authorization: Bearer {token}
  x-access-token: {token}
  User-Agent: Classplus/4.5.9 (Android 13)

Response (200):
{
  "url": "https://example.com/video?signature=xxx&expires=1234567890",
  "expires_in": 3600,
  "content_id": "5f4a8c2b9d7e1f3a"
}
```

### 5. Get Course Videos
```
GET /cams/course/{courseId}/videos

Headers:
  Authorization: Bearer {token}
  x-access-token: {token}

Response (200):
{
  "data": [
    {
      "id": "5f4a8c2b9d7e1f3a",
      "title": "Video 1",
      "description": "Description",
      "thumbnail": "https://...",
      "duration": 3600,
      "contentHashId": "hash123"
    },
    ...
  ]
}
```

---

## 📊 Content Endpoints

### 1. Get All Courses
```
GET /courses

Headers:
  Authorization: Bearer {token}

Response (200):
{
  "data": [
    {
      "id": "course_id_1",
      "name": "Course Name",
      "description": "Description",
      "instructor": "Instructor Name",
      "videoCount": 50
    }
  ]
}
```

### 2. Get Course Details
```
GET /courses/{courseId}

Headers:
  Authorization: Bearer {token}

Response (200):
{
  "id": "course_id_1",
  "name": "Course Name",
  "description": "Long description",
  "instructor": "Instructor Name",
  "thumbnail": "https://...",
  "totalVideos": 50,
  "totalDuration": 180000,
  "videos": [...]
}
```

### 3. Get User Profile
```
GET /user/profile

Headers:
  Authorization: Bearer {token}
  x-access-token: {token}

Response (200):
{
  "id": "user_id",
  "email": "user@example.com",
  "name": "User Name",
  "avatar": "https://...",
  "enrolledCourses": 5,
  "watchedVideos": 25
}
```

---

## 🔄 Android-Specific Headers

### Required Headers for All Requests:
```
User-Agent: Classplus/4.5.9 (Linux; Android 13; SM-G991B)
x-app-version: 4.5.9
x-platform: android
x-device-id: {unique_device_id}
x-client: android
X-Requested-With: com.classplus.app
Accept: application/json, */*
Accept-Encoding: gzip, deflate
Accept-Language: en-IN,en;q=0.9
Connection: keep-alive
```

### Optional Headers:
```
Authorization: Bearer {accessToken}
x-access-token: {accessToken}
Referer: https://app.classplusapp.com/
Origin: https://app.classplusapp.com
Cache-Control: no-cache
Pragma: no-cache
```

---

## 🛠️ cURL Examples

### Example 1: Login
```bash
curl -X POST https://api.classplusapp.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password"
  }'
```

### Example 2: Get Signed URL
```bash
curl -X GET "https://api.classplusapp.com/cams/uploader/video/jw-signed-url?contentId=5f4a8c2b9d7e1f3a" \
  -H "x-access-token: YOUR_TOKEN" \
  -H "User-Agent: Classplus/4.5.9 (Android 13)"
```

### Example 3: Get M3U8 Stream
```bash
curl -X GET "https://api.classplusapp.com/cams/uploader/video/5f4a8c2b9d7e1f3a/stream.m3u8" \
  -H "x-access-token: YOUR_TOKEN" \
  -H "User-Agent: Classplus/4.5.9 (Android 13)"
```

### Example 4: Get Courses
```bash
curl -X GET https://api.classplusapp.com/courses \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "User-Agent: Classplus/4.5.9 (Android 13)"
```

---

## ⚠️ Common Error Codes

| Code | Status | Solution |
|------|--------|----------|
| 401 | Unauthorized | Token expired, refresh or login again |
| 403 | Forbidden | No permission to access video |
| 404 | Not Found | Video/course not found |
| 500 | Server Error | contentHashId = null, try fallback endpoints |
| 503 | Service Unavailable | Server down, retry later |

---

## 🔌 Testing Endpoints in Postman

1. **Import Collection:**
   - Create new Collection: "CP Player API"
   - Add requests as shown in cURL examples

2. **Set Environment Variables:**
   ```
   {
    "token": "YOUR_ACCESS_TOKEN",
    "contentId": "5f4a8c2b9d7e1f3a",
    "baseUrl": "https://api.classplusapp.com"
   }
   ```

3. **Test Each Endpoint:**
   - Login first to get token
   - Use token for other requests
   - Check responses in Body tab

---

**Last Updated:** 2026-05-25