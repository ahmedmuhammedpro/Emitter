package com.ahmed.emitter.network

import com.ahmed.emitter.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface UserService {
    @GET("users")
    suspend fun getUsers(): List<User>
}

object UserNetwork {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .client(getHttpLoggingInterceptor())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val userNetwork: UserService = retrofit.create(UserService::class.java)

    private fun getHttpLoggingInterceptor(): OkHttpClient {
        val client = OkHttpClient.Builder()
        client.connectTimeout(30, TimeUnit.SECONDS)
        client.readTimeout(30, TimeUnit.SECONDS)
        client.writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(httpLoggingInterceptor)
        }
        return client.build()
    }
}