package com.classplus.player.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.classplus.player.R
import com.classplus.player.network.RetrofitClient
import kotlinx.coroutines.launch

class ClassPlusPlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private val apiService = RetrofitClient.apiService
    private val TAG = "ClassPlusPlayerActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classplus_player)

        playerView = findViewById(R.id.player_view)
        
        val contentId = intent.getStringExtra("content_id") ?: ""
        val accessToken = intent.getStringExtra("access_token") ?: ""

        Log.d(TAG, "Activity created with contentId: $contentId")

        if (contentId.isEmpty() || accessToken.isEmpty()) {
            Log.e(TAG, "Missing content ID or access token")
            Toast.makeText(this, "Missing content ID or access token", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        RetrofitClient.init(this)
        loadAndPlayVideo(contentId, accessToken)
    }

    private fun loadAndPlayVideo(contentId: String, accessToken: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Fetching signed URL for content: $contentId")
                val response = apiService.getSignedUrl(contentId, accessToken)
                
                if (response.error != null) {
                    Log.e(TAG, "API Error: ${response.error}")
                    Toast.makeText(this@ClassPlusPlayerActivity, "Error: ${response.error}", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val manifestUrl = response.drmUrls.manifestUrl
                val licenseUrl = response.drmUrls.licenseUrl
                
                Log.d(TAG, "Got manifest URL: $manifestUrl")
                Log.d(TAG, "Got license URL: $licenseUrl")

                initializePlayer(
                    manifestUrl = manifestUrl,
                    licenseUrl = licenseUrl,
                    accessToken = accessToken
                )
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun initializePlayer(
        manifestUrl: String,
        licenseUrl: String,
        accessToken: String
    ) {
        try {
            Log.d(TAG, "Initializing ExoPlayer...")
            player = ExoPlayer.Builder(this).build()
            playerView.player = player

            val drmSchemeUuid = Util.getDrmUuid("widevine")
                ?: throw Exception("Widevine DRM not supported on this device")

            Log.d(TAG, "DRM UUID: $drmSchemeUuid")

            val keyRequestProperties = mapOf(
                "x-access-token" to accessToken,
                "User-Agent" to "Classplus/4.3.5 (Android 13; classplus.com)",
                "x-platform" to "android",
                "Authorization" to "Bearer $accessToken"
            )

            val mediaItem = MediaItem.Builder()
                .setUri(manifestUrl)
                .setDrmConfiguration(
                    MediaItem.DrmConfiguration.Builder(drmSchemeUuid)
                        .setLicenseUri(licenseUrl)
                        .setForceDefaultLicenseUri(false)
                        .setMultiSession(false)
                        .setRequestHeaders(keyRequestProperties)
                        .build()
                )
                .build()

            Log.d(TAG, "Setting media item and preparing player...")
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true

            setupPlayerListeners()
            Log.d(TAG, "Player initialized successfully")
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                handlePlaybackError(error)
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "Player state: BUFFERING")
                        showBuffering()
                    }
                    Player.STATE_READY -> {
                        Log.d(TAG, "Player state: READY")
                        hideBuffering()
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "Player state: ENDED")
                        onVideoEnded()
                    }
                    Player.STATE_IDLE -> {
                        Log.d(TAG, "Player state: IDLE")
                    }
                }
            }
        })
    }

    private fun handlePlaybackError(error: PlaybackException) {
        Log.e(TAG, "Playback error - Code: ${error.errorCode}", error)
        Log.e(TAG, "Error message: ${error.message}")
        
        val errorMsg = when (error.errorCode) {
            PlaybackException.ERROR_CODE_DRM_SCHEME_UNSUPPORTED -> 
                "DRM scheme not supported on this device"
            PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> 
                "Failed to acquire DRM license"
            PlaybackException.ERROR_CODE_NETWORK_TRANSIENT -> 
                "Network error, please check your connection"
            PlaybackException.ERROR_CODE_REMOTE_ERROR -> 
                "Remote error: ${error.message}"
            else -> error.message ?: "Unknown playback error"
        }

        Toast.makeText(this, "Playback error: $errorMsg", Toast.LENGTH_LONG).show()
    }

    private fun handleError(e: Exception) {
        Log.e(TAG, "Error: ${e.message}", e)
        
        val errorMsg = when {
            e.message?.contains("401") == true -> "Unauthorized: Invalid token"
            e.message?.contains("403") == true -> "Forbidden: No permission"
            e.message?.contains("404") == true -> "Video not found"
            e.message?.contains("500") == true -> "Server error"
            e.message?.contains("timeout") == true -> "Request timeout"
            e.message?.contains("network") == true -> "Network error"
            else -> e.message ?: "Unknown error occurred"
        }

        Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_LONG).show()
    }

    private fun showBuffering() {
        // Show buffering indicator
        Log.d(TAG, "Buffering started")
    }

    private fun hideBuffering() {
        // Hide buffering indicator
        Log.d(TAG, "Buffering finished")
    }

    private fun onVideoEnded() {
        Log.d(TAG, "Video playback completed")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.release()
            Log.d(TAG, "Player released")
        }
    }

    override fun onPause() {
        super.onPause()
        if (::player.isInitialized) {
            player.playWhenReady = false
            Log.d(TAG, "Player paused")
        }
    }

    override fun onResume() {
        super.onResume()
        if (::player.isInitialized) {
            player.playWhenReady = true
            Log.d(TAG, "Player resumed")
        }
    }
}
