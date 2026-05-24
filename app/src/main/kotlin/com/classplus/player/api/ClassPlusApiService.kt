package com.classplus.player.api

import retrofit2.http.*
import com.google.gson.annotations.SerializedName

interface ClassPlusApiService {
    
    @GET("cams/uploader/video/jw-signed-url")
    suspend fun getSignedUrl(
        @Query("contentId") contentId: String,
        @Header("x-access-token") accessToken: String
    ): VideoUrlResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("cams/content/{contentId}")
    suspend fun getContentInfo(
        @Path("contentId") contentId: String
    ): ContentInfo
}

// Data Classes

data class VideoUrlResponse(
    val drmUrls: DrmUrls,
    val playbackUrls: PlaybackUrls? = null,
    val error: String? = null
)

data class DrmUrls(
    val manifestUrl: String,
    val licenseUrl: String,
    val pssh: String? = null
)

data class PlaybackUrls(
    @SerializedName("hls_url")
    val hlsUrl: String? = null,
    @SerializedName("dash_url")
    val dashUrl: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val user: User,
    val message: String? = null
)

data class User(
    val id: String,
    val email: String,
    val name: String,
    val avatar: String? = null
)

data class ContentInfo(
    val id: String,
    val title: String,
    val description: String? = null,
    val duration: Long = 0,
    val thumbnail: String? = null
)
