# CP Player - ClassPlus Video Player

Advanced Android video player for ClassPlus with Widevine DRM support, Android app headers, and web block bypass.

## 🎯 Features

✅ **Widevine DRM Protection** - Secure video streaming  
✅ **Android App Headers** - Web playback block bypass  
✅ **Signed URL Support** - ClassPlus API integration  
✅ **ExoPlayer 1.3.0** - Latest media3 framework  
✅ **Custom Interceptor** - Automatic header injection  
✅ **Error Handling** - Detailed logging and recovery  
✅ **Lifecycle Management** - Auto pause/resume  
✅ **Full Controls** - Play, pause, seek, fullscreen  

## 📦 Project Structure

```
Cp-player/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── kotlin/com/classplus/player/
│       │   ├── network/
│       │   │   ├── ClassPlusInterceptor.kt
│       │   │   └── RetrofitClient.kt
│       │   ├── api/
│       │   │   └── ClassPlusApiService.kt
│       │   └── ui/
│       │       ├── ClassPlusPlayerActivity.kt
│       │       └── ClassPlusPlayerViewModel.kt
│       ├── res/layout/
│       │   └── activity_classplus_player.xml
│       └── AndroidManifest.xml
├── README.md
├── USAGE.md
└── .gitignore
```

## 🚀 Quick Start

### Clone Repository
```bash
git clone https://github.com/sujalkumarsu5/Cp-player.git
cd Cp-player
```

### Open in Android Studio
1. Android Studio खोलें
2. File → Open → Cp-player folder select करें
3. Gradle sync होने दें

### Build & Run
```bash
./gradlew build
./gradlew installDebug
```

### Start Player
```kotlin
val intent = Intent(this, ClassPlusPlayerActivity::class.java)
intent.putExtra("content_id", "YOUR_CONTENT_ID")
intent.putExtra("access_token", "YOUR_ACCESS_TOKEN")
startActivity(intent)
```

## 🔐 API Integration

### Get Signed URL
```
GET https://api.classplusapp.com/cams/uploader/video/jw-signed-url?contentId={contentId}
Headers:
  x-access-token: {token}
```

### Response Format
```json
{
  "drmUrls": {
    "manifestUrl": "https://example.com/stream.mpd",
    "licenseUrl": "https://widevine-license-server.com/...",
    "pssh": "AAAAOHBzcw..."
  }
}
```

## 📱 Android Headers Sent

```
User-Agent: Classplus/4.3.5 (Android 13; classplus.com)
x-app-version: 4.3.5
x-platform: android
x-device-id: {device_id}
Authorization: Bearer {token}
x-access-token: {token}
Accept: */*
Accept-Language: en-IN
```

## 🛠️ Technology Stack

- **Language**: Kotlin
- **Player**: ExoPlayer 1.3.0 (Media3)
- **DRM**: Widevine
- **Networking**: Retrofit + OkHttp
- **JSON**: Gson
- **Async**: Coroutines
- **Min SDK**: 24
- **Target SDK**: 34

## 🧪 Testing

### Google Demo Stream (Widevine Test)
```kotlin
val MANIFEST_URL = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd"
val LICENSE_URL = "https://proxy.uat.widevine.com/proxy?provider=widevine_test"
```

## ⚠️ Important Notes

🔒 **Security**
- Never hardcode access tokens
- Use EncryptedSharedPreferences for storage
- Validate SSL certificates
- Use HTTPS only

🎬 **Playback**
- Requires internet connection
- Device must support Widevine DRM
- Check Logcat for detailed error logs

## 🐛 Troubleshooting

### Player Not Loading
1. Check internet connection
2. Verify content ID and token are correct
3. Check device supports Widevine DRM
4. Review Logcat: `adb logcat | grep ClassPlus`

### DRM Error
1. Verify access token is valid
2. Check license server URL
3. Ensure device has internet
4. Retry after some time

### Black Screen
1. Wait for buffering to complete
2. Check manifest URL is valid
3. Verify internet speed
4. Check video duration

## 📝 Usage Guide

Detailed usage instructions are available in [USAGE.md](USAGE.md)

## 📄 License

MIT License - See LICENSE file for details

## 👨‍💻 Author

**Sujal Kumar**  
https://github.com/sujalkumarsu5

## 💬 Support

For issues and questions:
1. Check README.md
2. Review USAGE.md
3. Check Logcat logs
4. Create GitHub issue with:
   - Error logs
   - Device info (Android version)
   - Steps to reproduce
   - Content ID (if possible)

---

**Made with ❤️ for ClassPlus Users**
