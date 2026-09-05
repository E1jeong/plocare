package com.senplo.plocare.auth

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.senplo.plocare.shared.R

private class AndroidGoogleAuthService(
    context: Context,
    private val serverClientId: String,
) : GoogleAuthService {
    private val credentialManager = CredentialManager.create(context)
    private val activityContext = context

    override suspend fun signIn(): Result<GoogleUser> = runCatching {
        val googleOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()
        val credential = credentialManager.getCredential(
            context = activityContext,
            request = request,
        ).credential

        check(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "Google ID credential을 확인할 수 없습니다." }

        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
        GoogleUser(
            idToken = googleCredential.idToken,
            email = googleCredential.id,
            displayName = googleCredential.displayName,
            profilePictureUri = googleCredential.profilePictureUri?.toString(),
        )
    }.onFailure { error ->
        Log.e(
            "PloCareGoogleAuth",
            "Credential Manager sign-in failed: ${error::class.java.simpleName}: ${error.message}",
        )
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}

@Composable
actual fun rememberGoogleAuthService(): GoogleAuthService {
    val context = LocalContext.current
    val serverClientId = context.getString(R.string.google_web_client_id)
    return remember(context, serverClientId) {
        AndroidGoogleAuthService(context, serverClientId)
    }
}
