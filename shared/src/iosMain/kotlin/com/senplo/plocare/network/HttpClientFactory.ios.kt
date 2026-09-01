package com.senplo.plocare.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createPlatformHttpClient(): HttpClient {
    return HttpClientFactory.configure(HttpClient(Darwin))
}
