package com.senplo.plocare

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform