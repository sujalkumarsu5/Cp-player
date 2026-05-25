# CP Player - Comprehensive Troubleshooting Guide

## 🔧 Issue #1: contentHashId = null

### Problem
```
Error: contentHashId is null
Code: MEDIA_NOT_FOUND
HTTP: 500 Internal Server Error
```

### Causes
- Video doesn't have JW Player metadata
- Content not properly indexed in ClassPlus
- API returning incomplete data

### Solutions

**Solution 1: Use Fallback Endpoints**
```kotlin
// Try multiple endpoints in sequence
val endpoints = listOf(
    "https://api.classplusapp.com/cams/uploader/video/$contentId/stream.m3u8",
    "https://api.classplusapp.com/cams/uploader/video/$contentId/playlist.m3u8",
    "https://api.classplusapp.com/cams/uploader/video/$contentId/signed-url"
)

for (endpoint in endpoints) {
    try {
        val response = client.newCall(Request.Builder().url(endpoint).build()).execute()
        if (response.isSuccessful) {
            return endpoint
        }
    } catch (e: Exception) {
        continue
    }
}
```

**Solution 2: Extract Media ID from HTML**
```kotlin
val html = getVideoPageHTML(contentId, token)
val mediaId = extractJWPlayerID(html)
if (mediaId != null) {
    val streamUrl = resolveStreamUrl(mediaId, token)
}
```

**Solution 3: Use Android Headers**
```kotlin
val request = Request.Builder()
    .url("https://api.classplusapp.com/cams/uploader/video/jw-signed-url?contentId=$contentId")
    .header("User-Agent", "Classplus/4.5.9 (Android 13)")
    .header("x-platform", "android")
    .header("x-device-id", deviceId)
    .build()
```

---

## 🔧 Issue #2: vidkey Field Not Found

### Problem
```
Warning: vidkey field not found in response
No video source available
```

### Causes
- Video metadata incomplete
- JW Player not configured for video
- API response format changed

### Solutions

**Solution 1: Multiple Extraction Patterns**
```kotlin
val patterns = listOf(
    """"vidkey"\s*:\s*"([^"]+)""",
    """"vid"\s*:\s*"([^"]+)""",
    """"mediaId"\s*:\s*"([^"]+)""",
    """"contentId"\s*:\s*"([^"]+)"""
)

for (pattern in patterns) {
    val regex = pattern.toRegex()
    val match = regex.find(response)
    if (match != null) {
        return match.groupValues[1]
    }
}
```

**Solution 2: Check Response Format**
```kotlin
try {
    val json = Gson().fromJson(response, JsonObject::class.java)
    
    // Try different field names
    val id = json.get("vidkey")?.asString
        ?: json.get("mediaId")?.asString
        ?: json.get("contentId")?.asString
        ?: json.get("id")?.asString
    
    return id
} catch (e: Exception) {
    // HTML fallback
    return extractFromHTML(response)
}
```

**Solution 3: Fallback to Direct Stream**
```kotlin
if (vidkey == null) {
    // Try direct m3u8 instead
    val m3u8Url = "https://api.classplusapp.com/cams/uploader/video/$contentId/stream.m3u8"
    return m3u8Url
}
```

---

## 🔧 Issue #3: SSL Certificate Error

### Problem
```
javax.net.ssl.SSLHandshakeException: CERTIFICATE_VERIFY_FAILED
ssl_cert_expired
```

### Causes
- Server SSL certificate expired
- Incorrect date/time on device
- Network interceptor issue

### Solutions

**Solution 1: Update System Date/Time**
```
Device Settings → Date & Time → Set Automatically (ON)
```

**Solution 2: Use TrustAllCerts (Testing Only)**
```kotlin
import javax.net.ssl.SSLContext

val sslContext = SSLContext.getInstance("TLS")
sslContext.init(null, arrayOf(TrustAllCerts()), java.security.SecureRandom())

val client = OkHttpClient.Builder()
    .sslSocketFactory(sslContext.socketFactory, TrustAllCerts())
    .hostnameVerifier { _, _ -> true }
    .build()
```

⚠️ **WARNING: For Testing Only!** Do not use in production!

**Solution 3: Use Production SSL Handler**
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.classplusapp.com", "sha256/AAAAAAAAAAAAAAAA...")
    .build()

val client = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

---

## 🔧 Issue #4: 401 Unauthorized

### Problem
```
HTTP 401 Unauthorized
Token expired or invalid
```

### Causes
- Access token expired
- Token not sent in headers
- Token format incorrect

### Solutions

**Solution 1: Refresh Token**
```kotlin
if (response.code == 401) {
    val newToken = refreshAccessToken(refreshToken)
    saveToken(newToken)
    
    // Retry request with new token
    val newRequest = originalRequest.newBuilder()
        .header("Authorization", "Bearer $newToken")
        .build()
    return chain.proceed(newRequest)
}
```

**Solution 2: Check Token Format**
```kotlin
// ✅ Correct format
headers["Authorization"] = "Bearer $token"
headers["x-access-token"] = token

// ❌ Wrong format
headers["Authorization"] = token  // Missing "Bearer"
```

**Solution 3: Verify Token Storage**
```kotlin
val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
val token = prefs.getString("access_token", "")

if (token.isNullOrEmpty()) {
    // Token missing, need to login again
    startLoginActivity()
}
```

---

## 🔧 Issue #5: 403 Forbidden

### Problem
```
HTTP 403 Forbidden
You don't have permission to access this resource
```

### Causes
- User not enrolled in course
- Video access restricted
- Account not verified

### Solutions

**Solution 1: Check Course Enrollment**
```kotlin
try {
    val courses = apiService.getCourses(token)
    if (!courses.any { it.id == courseId }) {
        showError("You are not enrolled in this course")
    }
} catch (e: Exception) {
    Log.e("Auth", "Error checking enrollment")
}
```

**Solution 2: Verify User Account**
```kotlin
val profile = apiService.getUserProfile(token)
if (!profile.isVerified) {
    showError("Please verify your account to access videos")
}
```

**Solution 3: Request Access**
```kotlin
// Provide option to request access
apiService.requestVideoAccess(contentId, token)
showMessage("Access request sent to instructor")
```

---

## 🔧 Issue #6: 404 Not Found

### Problem
```
HTTP 404 Not Found
Video not found
```

### Causes
- Video ID incorrect
- Video deleted
- Video not indexed yet

### Solutions

**Solution 1: Verify Content ID**
```kotlin
// Get from browser console
// Open ClassPlus website → F12 → Network tab
// Look for /cams/uploader/video/ requests
// Copy contentId parameter

val correctContentId = "5f4a8c2b9d7e1f3a"  // Format example
```

**Solution 2: Check If Video Exists**
```kotlin
try {
    val response = apiService.getVideoDetails(contentId, token)
    Log.d("Video", "Found: ${response.title}")
} catch (e: HttpException) {
    if (e.code() == 404) {
        showError("Video not found. It may have been deleted.")
    }
}
```

**Solution 3: Try Alternative Endpoints**
```kotlin
val endpoints = listOf(
    "/cams/uploader/video/$contentId",
    "/videos/$contentId",
    "/content/$contentId"
)
```

---

## 🔧 Issue #7: Network Timeout

### Problem
```
java.net.SocketTimeoutException: timeout
Read timed out
```

### Causes
- Slow internet connection
- Server overloaded
- Large file size

### Solutions

**Solution 1: Increase Timeout**
```kotlin
val httpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)  // Default: 10s
    .readTimeout(60, TimeUnit.SECONDS)     // Default: 10s
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()
```

**Solution 2: Check Internet Connection**
```kotlin
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return connectivityManager.activeNetworkInfo?.isConnected == true
}

if (!isNetworkAvailable(context)) {
    showError("No internet connection")
}
```

**Solution 3: Retry with Exponential Backoff**
```kotlin
suspend fun retryWithBackoff(
    maxRetries: Int = 3,
    delayMs: Long = 1000,
    block: suspend () -> String
): String {
    var lastException: Exception? = null
    
    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            if (attempt < maxRetries - 1) {
                delay(delayMs * (attempt + 1))
            }
        }
    }
    
    throw lastException ?: Exception("Failed after $maxRetries retries")
}
```

---

## 🔧 Issue #8: Black Screen / No Video Display

### Problem
```
Video loads but screen is black
No error in logs
```

### Causes
- Video data loading but not rendering
- Wrong video format
- Player not initialized

### Solutions

**Solution 1: Check Video Duration**
```kotlin
val player = exoPlayer
Log.d("Player", "Duration: ${player.duration}")

if (player.duration == 0L) {
    Log.e("Player", "Video not loaded properly")
}
```

**Solution 2: Wait for Player Ready**
```kotlin
exoPlayer.addListener(object : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> Log.d("Player", "Idle")
            Player.STATE_BUFFERING -> Log.d("Player", "Buffering...")
            Player.STATE_READY -> {
                Log.d("Player", "Ready to play")
                exoPlayer.play()
            }
            Player.STATE_ENDED -> Log.d("Player", "Playback ended")
        }
    }
})
```

**Solution 3: Debug Video Source**
```kotlin
try {
    val mediaItem = MediaItem.Builder()
        .setUri(videoUri)
        .build()
    
    exoPlayer.setMediaItem(mediaItem)
    exoPlayer.prepare()
    
    Log.d("Player", "Media item set: $videoUri")
    Log.d("Player", "MediaSource: ${exoPlayer.mediaItemCount}")
} catch (e: Exception) {
    Log.e("Player", "Error: ${e.message}", e)
}
```

---

## 🔧 Issue #9: App Crashes

### Problem
```
Fatal Exception: java.lang.NullPointerException
App stops unexpectedly
```

### Causes
- Null token/content ID
- Missing permissions
- Memory leak

### Solutions

**Solution 1: Add Null Checks**
```kotlin
val contentId = intent.getStringExtra("content_id")
val token = intent.getStringExtra("access_token")

if (contentId.isNullOrEmpty() || token.isNullOrEmpty()) {
    Log.e("Player", "Missing required parameters")
    finish()
    return
}
```

**Solution 2: Check Permissions**
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

**Solution 3: Handle Exceptions**
```kotlin
try {
    loadVideo(contentId, token)
} catch (e: NullPointerException) {
    Log.e("Player", "Null pointer: ${e.message}", e)
    showError("An error occurred")
} catch (e: Exception) {
    Log.e("Player", "Error: ${e.message}", e)
    showError("${e.message}")
}
```

---

## 🔧 Issue #10: DRM License Error

### Problem
```
DrmException: License server error
Widevine license acquisition failed
```

### Causes
- Device not DRM capable
- License server unreachable
- Device not provisioned

### Solutions

**Solution 1: Check DRM Support**
```kotlin
import android.media.MediaDrm

fun isDrmSupported(): Boolean {
    return try {
        val mediaDrm = MediaDrm(UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed"))
        true
    } catch (e: Exception) {
        false
    }
}
```

**Solution 2: Handle License Error**
```kotlin
exoPlayer.addListener(object : Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        if (error.cause is DrmException) {
            Log.e("DRM", "DRM Error: ${error.message}")
            showError("DRM not supported on this device")
        }
    }
})
```

**Solution 3: Use Non-DRM Fallback**
```kotlin
try {
    playWithDRM(videoUri)
} catch (e: DrmException) {
    Log.w("DRM", "DRM failed, trying without DRM")
    playWithoutDRM(videoUri)
}
```

---

## 📊 Debug Logging

### Enable Verbose Logging
```kotlin
val httpClient = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .build()
```

### View Logs
```bash
# All ClassPlus logs
adb logcat | grep ClassPlus

# Errors only
adb logcat | grep "E.*ClassPlus"

# Save to file
adb logcat > debug.log &
# ... run app ...
# Stop with Ctrl+C
```

---

## 🆘 Getting Help

1. Check all above solutions
2. Review application logs
3. Create GitHub issue with:
   - Error message
   - Logcat output
   - Steps to reproduce
   - Device info (Android version, model)
   - Content ID (masked if sensitive)

---

**Last Updated:** 2026-05-25