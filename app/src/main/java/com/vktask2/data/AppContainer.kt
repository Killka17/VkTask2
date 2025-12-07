package com.vktask2.data

import android.content.Context
import java.io.File
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

class AppContainer(context: Context) {
    private val cacheSize = 50L * 1024 * 1024
    private val okHttpCache = Cache(File(context.cacheDir, "http_cache"), cacheSize)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cache(okHttpCache)
        .addNetworkInterceptor { chain ->
            val response = chain.proceed(chain.request())
            response.newBuilder()
                .header("Cache-Control", "public, max-age=300")
                .build()
        }
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://picsum.photos/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val api: PicsumApi = retrofit.create(PicsumApi::class.java)

    val repository: PicsumRepository = PicsumRepository(api)
}

