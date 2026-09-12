package com.senplo.plocare.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private object WebDemoGoogleAuthService : GoogleAuthService {
    override suspend fun signIn(): Result<GoogleUser> = Result.success(
        GoogleUser(
            idToken = "web-demo",
            email = "demo@plocare.kr",
            displayName = "PloCare Demo",
        )
    )

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)
}

@Composable
actual fun rememberGoogleAuthService(): GoogleAuthService = remember { WebDemoGoogleAuthService }
