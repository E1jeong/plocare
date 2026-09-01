package com.senplo.plocare.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun createPlatformHttpClient(): HttpClient {
    return HttpClientFactory.configure(HttpClient(OkHttp))
}
