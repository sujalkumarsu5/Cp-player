package com.classplus.player.network

import okhttp3.Interceptor
import okhttp3.Response
import android.content.Context
import android.provider.Settings
import android.util.Log

class ClassPlusInterceptor(private val context: Context) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val newRequest = originalRequest.newBuilder()
            .header("User-Agent", "Classplus/4.3.5 (Android 13; classplus.com)")
            .header("x-app-version", "4.3.5")
            .header("x-platform", "android")
            .header("x-device-id", getDeviceId())
            .header("Accept", "*/*")
            .header("Accept-Encoding", "gzip, deflate")
            .header("Accept-Language", "en-IN")
            .header("Connection", "keep-alive")
            .header("Cache-Control", "no-cache")
            .apply {
                val token = getAccessToken()
                if (token.isNotEmpty()) {
                    addHeader("Authorization", "Bearer $token")
                    addHeader("x-access-token", token)
                }
            }
            .build()

        Log.d(TAG, "Request URL: ${newRequest.url}")
        Log.d(TAG, "Request Headers: ${newRequest.headers}")

        return try {
            chain.proceed(newRequest).also {
                Log.d(TAG, "Response Code: ${it.code}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Request failed: ${e.message}", e)
            throw e
        }
    }

    private fun getDeviceId(): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: "unknown_device"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device ID: ${e.message}", e)
            "unknown_device"
        }
    }

    private fun getAccessToken(): String {
        return try {
            val prefs = context.getSharedPreferences("classplus_prefs", Context.MODE_PRIVATE)
            prefs.getString("access_token", "") ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting access token: ${e.message}", e)
            ""
        }
    }

    companion object {
        private const val TAG = "ClassPlusInterceptor"
    }
}
