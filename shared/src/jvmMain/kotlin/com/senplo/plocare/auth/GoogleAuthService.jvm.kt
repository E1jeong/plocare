package com.senplo.plocare.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private object DesktopDebugGoogleAuthService : GoogleAuthService {
    override suspend fun signIn(): Result<GoogleUser> = Result.success(
        GoogleUser(
            idToken = "desktop-debug",
            email = "desktop.debug@plocare.local",
            displayName = "Desktop Debug",
        ),
    )

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)
}

@Composable
actual fun rememberGoogleAuthService(): GoogleAuthService = remember { DesktopDebugGoogleAuthService }
