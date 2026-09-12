package com.senplo.plocare.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual fun createPlatformHttpClient(): HttpClient =
    HttpClientFactory.configure(HttpClient(Js))
