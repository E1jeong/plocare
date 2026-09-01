package com.senplo.plocare.di

import com.russhwolf.settings.Settings
import com.senplo.plocare.network.createPlatformHttpClient
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect val platformModule: Module

val networkModule = module {
    single<HttpClient> { createPlatformHttpClient() }
}

val storageModule = module {
    single<Settings> { Settings() }
}

val appModule = module {
    includes(networkModule, storageModule, platformModule)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModule)
    }
