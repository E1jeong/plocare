package com.senplo.plocare.auth

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
