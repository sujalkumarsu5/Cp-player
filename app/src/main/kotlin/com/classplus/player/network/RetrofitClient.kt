package com.classplus.player.network

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.classplus.player.api.ClassPlusApiService
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.classplusapp.com/"
    private const val TAG = "RetrofitClient"
    private lateinit var context: Context
    private var retrofit: Retrofit? = null

    fun init(ctx: Context) {
        context = ctx
        Log.d(TAG, "RetrofitClient initialized")
    }

    val apiService: ClassPlusApiService
        get() {
            if (retrofit == null) {
                retrofit = buildRetrofit()
            }
            return retrofit!!.create(ClassPlusApiService::class.java)
        }

    private fun buildRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(ClassPlusInterceptor(context))
            .addNetworkInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        Log.d(TAG, "Building Retrofit with base URL: $BASE_URL")

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
