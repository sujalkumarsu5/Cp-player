# CP Player - Detailed Usage Guide

## 📚 Table of Contents

1. [Getting Started](#getting-started)
2. [Authentication](#authentication)
3. [Playing Videos](#playing-videos)
4. [Error Handling](#error-handling)
5. [Advanced Configuration](#advanced-configuration)
6. [Troubleshooting](#troubleshooting)

---

## Getting Started

### Prerequisites
- Android 7.0 (API 24) or higher
- Internet connection
- Device supports Widevine DRM
- Valid ClassPlus account

### Installation

```bash
git clone https://github.com/sujalkumarsu5/Cp-player.git
cd Cp-player
```

Open in Android Studio and build.

---

## Authentication

### Getting Access Token

#### Method 1: From Shared Preferences (if already logged in)

```kotlin
val prefs = context.getSharedPreferences("classplus_prefs", Context.MODE_PRIVATE)
val accessToken = prefs.getString("access_token", "") ?: ""
```

#### Method 2: Via Login API

```kotlin
lifecycleScope.launch {
    try {
        val response = RetrofitClient.apiService.login(
            LoginRequest(
                email = "your_email@example.com",
                password = "your_password"
            )
        )
        val token = response.accessToken
        
        // Save token securely
        val prefs = context.getSharedPreferences("classplus_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("access_token", token).apply()
    } catch (e: Exception) {
        Log.e("Auth", "Login failed: ${e.message}")
    }
}
```

#### Method 3: Direct Token (if you have it)

Just use the token directly in the player intent.

---

## Playing Videos

### Basic Usage

```kotlin
val intent = Intent(this, ClassPlusPlayerActivity::class.java)
intent.putExtra("content_id", "5f4a8c2b9d7e1f3a")  // ClassPlus video ID
intent.putExtra("access_token", "your_token_here")  // Access token
startActivity(intent)
```

### Getting Content ID

#### From ClassPlus App
1. Open ClassPlus app
2. Go to any course or video
3. Check network logs using Charles Proxy or Wireshark
4. Look for `/cams/uploader/video/` API calls
5. Extract `contentId` from URL or response

#### Example Content IDs
```
5f4a8c2b9d7e1f3a
60a9b1e4d2f8c3g5
61b5c2e5f3g9h6i2
```

### With Activity Result

```kotlin
private val playerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == RESULT_OK) {
        val duration = result.data?.getLongExtra("video_duration", 0L) ?: 0L
        Log.d("Player", "Video duration: $duration")
    }
}

// Launch player
val intent = Intent(this, ClassPlusPlayerActivity::class.java)
intent.putExtra("content_id", contentId)
intent.putExtra("access_token", token)
playerLauncher.launch(intent)
```

---

## Error Handling

### Common Errors & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| `MissingTokenException` | No access token provided | Login and get token |
| `InvalidContentIdException` | Wrong content ID | Verify content ID |
| `DrmError` | License server issue | Check network, retry |
| `TimeoutException` | Slow network | Check internet speed |
| `UnauthorizedException` | Token expired | Refresh token |
| `NetworkException` | No internet | Check connection |

### Error Logging

All errors are logged to Logcat:

```bash
# View all ClassPlus logs
adb logcat | grep ClassPlus

# View errors only
adb logcat | grep "ClassPlus.*E"

# Save to file
adb logcat | grep ClassPlus > cp-logs.txt
```

### Handling Errors in Code

```kotlin
try {
    val response = apiService.getSignedUrl(contentId, token)
    // Play video
} catch (e: HttpException) {
    when (e.code()) {
        401 -> showToast("Token expired, please login again")
        403 -> showToast("You don't have permission to watch this video")
        404 -> showToast("Video not found")
        else -> showToast("Server error: ${e.message}")
    }
} catch (e: IOException) {
    showToast("Network error: Check your internet connection")
} catch (e: Exception) {
    showToast("Error: ${e.message}")
}
```

---

## Advanced Configuration

### Custom HTTP Client

```kotlin
val httpClient = OkHttpClient.Builder()
    .addInterceptor(ClassPlusInterceptor(context))
    .addNetworkInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .certificatePinner(CertificatePinner.Builder()
        .add("api.classplusapp.com", "sha256/AAAAAAAAAAAAAAAAAAAAAA...")
        .build())
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()
```

### Using Proxy

```kotlin
val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("proxy.example.com", 8080))
val httpClient = OkHttpClient.Builder()
    .proxy(proxy)
    .proxyAuthenticator(Authenticator { _, response ->
        response.request.newBuilder()
            .header("Proxy-Authorization", "Basic base64_encoded_credentials")
            .build()
    })
    .build()
```

### Custom Headers

Edit `ClassPlusInterceptor.kt`:

```kotlin
private fun intercept(chain: Interceptor.Chain): Response {
    val newRequest = originalRequest.newBuilder()
        .header("Custom-Header", "custom-value")
        .header("X-Request-ID", UUID.randomUUID().toString())
        .build()
    return chain.proceed(newRequest)
}
```

### Token Refresh

```kotlin
val httpClient = OkHttpClient.Builder()
    .authenticator(Authenticator { _, response ->
        if (response.code == 401) {
            // Refresh token
            val newToken = refreshAccessToken()
            if (newToken != null) {
                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            }
        }
        null
    })
    .build()
```

---

## Troubleshooting

### Issue: Player Shows Black Screen

**Solutions:**
1. Check internet connection
2. Verify manifest URL is accessible
3. Wait for video to buffer
4. Check device supports Widevine DRM
5. Review logs: `adb logcat | grep ClassPlus`

### Issue: DRM License Error

**Solutions:**
1. Verify access token is valid
2. Check license server is responding
3. Ensure device has internet
4. Try on different device
5. Contact ClassPlus support

### Issue: Network Timeout

**Solutions:**
1. Check internet speed
2. Try on WiFi
3. Increase timeout in RetrofitClient (default 30s)
4. Disable VPN if using
5. Wait and retry

### Issue: Authentication Failed

**Solutions:**
1. Verify email/password
2. Check account is active
3. Clear app cache and try again
4. Check device date/time is correct
5. Reset password if necessary

### Issue: Video Not Found

**Solutions:**
1. Verify content ID is correct
2. Check video is not deleted
3. Verify you have permission to watch
4. Check video is in your course
5. Try another video

---

## Performance Optimization

### Cache Management

```kotlin
val cacheDir = File(context.cacheDir, "video_cache")
val cacheSize = 100 * 1024 * 1024 // 100MB
val cache = Cache(cacheDir, cacheSize.toLong())

val httpClient = OkHttpClient.Builder()
    .cache(cache)
    .build()
```

### Connection Pooling

```kotlin
val connectionPool = ConnectionPool(10, 5, TimeUnit.MINUTES)
val httpClient = OkHttpClient.Builder()
    .connectionPool(connectionPool)
    .build()
```

### Hardware Acceleration

```xml
<!-- AndroidManifest.xml -->
<activity
    android:name=".ui.ClassPlusPlayerActivity"
    android:hardwareAccelerated="true" />
```

---

## Security Best Practices

✅ **Always use HTTPS**
```kotlin
val httpClient = OkHttpClient.Builder()
    .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
    .build()
```

✅ **Store tokens securely**
```kotlin
val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secret_shared_prefs",
    MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

encryptedPrefs.edit().putString("access_token", token).apply()
```

✅ **Implement Certificate Pinning**
```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.classplusapp.com", "sha256/AAAAAAAAAA...")
    .build()

val httpClient = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

---

## FAQ

**Q: Can I download videos?**  
A: No, DRM protection prevents direct downloads. Use only for authorized streaming.

**Q: Does it work offline?**  
A: No, videos require internet connection for license verification.

**Q: Can I share my account?**  
A: Not recommended. Each account is for personal use only.

**Q: Is it legal?**  
A: Yes, for personal educational use with valid ClassPlus account.

**Q: Does it work on rooted devices?**  
A: May have issues due to DRM security. Use unrooted device for best experience.

---

## Support

- 📧 Email: Create GitHub issue
- 🐛 Bug Report: GitHub Issues
- 💡 Feature Request: GitHub Discussions

---

**Last Updated:** 2026-05-24  
**Version:** 1.0.0
