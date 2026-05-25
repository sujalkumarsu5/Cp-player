# CP Player - Advanced Setup Guide

## 📋 Prerequisites

- Android Studio 2023.1 or higher
- Android SDK 24+ (API 24)
- Kotlin 1.8+
- Gradle 8.0+
- Valid ClassPlus account with videos

## 🚀 Installation Steps

### Step 1: Clone Repository

```bash
# Clone the repository
git clone --branch advanced-video-streaming \
  https://github.com/sujalkumarsu5/Cp-player.git

# Navigate to project
cd Cp-player
```

### Step 2: Open in Android Studio

1. Open Android Studio
2. File → Open
3. Select Cp-player folder
4. Wait for Gradle sync to complete

### Step 3: Configure Gradle

Edit `app/build.gradle.kts`:

```kotlin
dependencies {
    // ExoPlayer (Media3)
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-ui:1.1.1")
    
    // Network
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    
    // JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    
    // Security (for encrypted preferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
```

### Step 4: Update AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <application
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.CpPlayer">
        
        <!-- Player Activity -->
        <activity
            android:name=".ui.ClassPlusPlayerActivity"
            android:configChanges="orientation|screenSize"
            android:hardwareAccelerated="true"
            android:screenOrientation="portrait"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

### Step 5: Build Project

```bash
# Build debug version
./gradlew build

# Or in Android Studio: Build → Make Project
```

### Step 6: Run on Device/Emulator

```bash
# Install APK
./gradlew installDebug

# Or: Run → Run 'app' in Android Studio
```

---

## 🔑 Getting ClassPlus Credentials

### Method 1: Web Login
1. Go to https://app.classplusapp.com
2. Open DevTools (F12)
3. Go to Application → Cookies
4. Find `access_token` cookie
5. Copy the value

### Method 2: Android App Network Logs
1. Install Charles Proxy on desktop
2. Configure Android device to use proxy
3. Open ClassPlus app
4. Go to any video
5. Check network logs for token in headers

### Method 3: API Login
```kotlin
val response = apiService.login(
    email = "your_email@example.com",
    password = "your_password"
)
val token = response.accessToken
```

---

## 🎮 Using the Player

### Basic Usage

```kotlin
val intent = Intent(this, ClassPlusPlayerActivity::class.java)
intent.putExtra("content_id", "YOUR_CONTENT_ID")
intent.putExtra("access_token", "YOUR_TOKEN")
startActivity(intent)
```

### Getting Content ID

#### From ClassPlus Web
1. Open https://app.classplusapp.com
2. Open any course video
3. Open DevTools (F12)
4. Go to Network tab
5. Search for `/cams/uploader/video/`
6. Look for `contentId` parameter

#### Example Content IDs
```
5f4a8c2b9d7e1f3a
60a9b1e4d2f8c3g5
61b5c2e5f3g9h6i2
```

### Advanced Usage

```kotlin
lifecycleScope.launch {
    try {
        // Step 1: Get token
        val token = getAccessToken()
        
        // Step 2: Resolve stream URL
        val streamUrl = videoUrlResolver.resolveStreamUrl(contentId, token)
        
        // Step 3: Play video
        val intent = Intent(this@MainActivity, ClassPlusPlayerActivity::class.java)
        intent.putExtra("content_id", contentId)
        intent.putExtra("access_token", token)
        intent.putExtra("stream_url", streamUrl)
        startActivity(intent)
    } catch (e: Exception) {
        Log.e("Player", "Error: ${e.message}")
    }
}
```

---

## 🧪 Testing

### Test with Sample Video

```kotlin
// ClassPlus sample video
val contentId = "5f4a8c2b9d7e1f3a"
val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

val intent = Intent(this, ClassPlusPlayerActivity::class.java)
intent.putExtra("content_id", contentId)
intent.putExtra("access_token", token)
startActivity(intent)
```

### Debug Logging

```bash
# View player logs
adb logcat | grep ClassPlus

# Save to file
adb logcat | grep ClassPlus > debug.log

# Monitor in real-time
adb logcat -f debug.log
tail -f debug.log
```

---

## 🔐 Security Considerations

### ⚠️ DO NOT
- ❌ Hardcode tokens in source code
- ❌ Share tokens in version control
- ❌ Use TrustAllCerts in production
- ❌ Store tokens in SharedPreferences without encryption
- ❌ Log sensitive data

### ✅ DO
- ✅ Use EncryptedSharedPreferences
- ✅ Implement token refresh logic
- ✅ Validate SSL certificates
- ✅ Use HTTPS only
- ✅ Store tokens securely

### Secure Token Storage

```kotlin
val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secret_prefs",
    MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

encryptedPrefs.edit().putString("access_token", token).apply()
```

---

## 📦 Project Structure

```
Cp-player/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── kotlin/com/classplus/player/
│   │   │   ├── network/
│   │   │   │   ├── TrustAllCerts.kt
│   │   │   │   ├── VideoStreamInterceptor.kt
│   │   │   │   ├── ClassPlusInterceptor.kt
│   │   │   │   └── RetrofitClient.kt
│   │   │   ├── api/
│   │   │   │   ├── ClassPlusApiService.kt
│   │   │   │   ├── JWPlayerApiService.kt
│   │   │   │   └── VideoStreamService.kt
│   │   │   ├── models/
│   │   │   │   ├── VideoResponse.kt
│   │   │   │   └── StreamUrl.kt
│   │   │   ├── ui/
│   │   │   │   ├── ClassPlusPlayerActivity.kt
│   │   │   │   ├── VideoListActivity.kt
│   │   │   │   └── PlayerViewModel.kt
│   │   │   ├── utils/
│   │   │   │   ├── JWPlayerExtractor.kt
│   │   │   │   ├── VideoUrlResolver.kt
│   │   │   │   └── ContentIdExtractor.kt
│   │   │   └── managers/
│   │   │       ├── StreamManager.kt
│   │   │       └── CacheManager.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── values/
│   │   │   └── drawable/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── README.md
├── API_ENDPOINTS.md
├── TROUBLESHOOTING.md
└── SETUP.md
```

---

## 🆘 Getting Help

1. **Check documentation:**
   - [README.md](README.md) - Overview
   - [API_ENDPOINTS.md](API_ENDPOINTS.md) - API details
   - [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Common issues

2. **View logs:**
   ```bash
   adb logcat | grep ClassPlus
   ```

3. **Create GitHub issue** with:
   - Error message
   - Logcat output
   - Device info (Android version, model)
   - Steps to reproduce

---

**Last Updated:** 2026-05-25  
**Version:** 1.0.0