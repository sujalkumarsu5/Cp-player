package com.classplus.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.classplus.player.api.ClassPlusApiService
import com.classplus.player.api.VideoUrlResponse
import android.util.Log

class ClassPlusPlayerViewModel(
    private val apiService: ClassPlusApiService
) : ViewModel() {

    private val _videoState = MutableStateFlow<VideoPlayerState>(VideoPlayerState.Loading)
    val videoState: StateFlow<VideoPlayerState> = _videoState

    private val TAG = "ClassPlusPlayerViewModel"

    fun loadVideo(contentId: String, accessToken: String) {
        if (contentId.isEmpty() || accessToken.isEmpty()) {
            _videoState.value = VideoPlayerState.Error("Missing content ID or access token")
            return
        }

        viewModelScope.launch {
            try {
                _videoState.value = VideoPlayerState.Loading
                Log.d(TAG, "Loading video: $contentId")
                
                val response = apiService.getSignedUrl(contentId, accessToken)
                
                if (response.error != null) {
                    Log.e(TAG, "API error: ${response.error}")
                    _videoState.value = VideoPlayerState.Error(response.error)
                } else {
                    Log.d(TAG, "Video loaded successfully")
                    _videoState.value = VideoPlayerState.Success(response)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading video: ${e.message}", e)
                _videoState.value = VideoPlayerState.Error(
                    e.message ?: "Unknown error occurred"
                )
            }
        }
    }
}

sealed class VideoPlayerState {
    object Loading : VideoPlayerState()
    data class Success(val data: VideoUrlResponse) : VideoPlayerState()
    data class Error(val message: String) : VideoPlayerState()
}
