package com.senplo.plocare.auth

import androidx.compose.runtime.Composable

data class GoogleUser(
    val idToken: String,
    val email: String? = null,
    val displayName: String? = null,
    val profilePictureUri: String? = null,
)

interface GoogleAuthService {
    suspend fun signIn(): Result<GoogleUser>
    suspend fun signOut(): Result<Unit>
}

@Composable
expect fun rememberGoogleAuthService(): GoogleAuthService
