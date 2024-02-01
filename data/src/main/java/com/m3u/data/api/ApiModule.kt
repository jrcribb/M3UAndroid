@file:Suppress("unused")

package com.m3u.data.api

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.m3u.core.architecture.logger.Logger
import com.m3u.data.Certs
import com.m3u.data.SSL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ApiModule {
    @Provides
    @Singleton
    fun provideOkhttpClient(
        logger: Logger
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .authenticator(Authenticator.JAVA_NET_AUTHENTICATOR)
            .addInterceptor { chain ->
                val request = chain.request()
                try {
                    chain.proceed(request)
                } catch (e: Exception) {
                    logger.log(e)
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(999)
                        .message(e.message.orEmpty())
                        .body("{${e}}".toResponseBody(null))
                        .build()
                }
            }
            .sslSocketFactory(SSL.TLSTrustAll.socketFactory, Certs.TrustAll)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    @Provides
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    fun provideYaml(): Yaml = Yaml(
        configuration = YamlConfiguration(
            strictMode = false
        )
    )

    @Provides
    @Singleton
    fun provideRetrofitBuilder(
        json: Json
    ): Retrofit.Builder {
        val mediaType = "application/json".toMediaType()
        return Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory(mediaType))
    }

    @Provides
    fun provideGithubApi(
        builder: Retrofit.Builder
    ): GithubApi = builder
        .baseUrl(Contracts.GITHUB_BASE_URL)
        .build()
        .create()
}
