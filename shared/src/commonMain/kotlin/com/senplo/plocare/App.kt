package com.senplo.plocare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.senplo.plocare.navigation.PloCareNavHost
import com.senplo.plocare.ui.theme.AppAudience
import com.senplo.plocare.ui.theme.PloCareColor
import com.senplo.plocare.ui.theme.PloCareTheme

@Composable
@Preview
fun App() {
    var audience by remember { mutableStateOf(AppAudience.USER) }

    PloCareTheme(audience = audience) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PloCareColor.BrandNavy),
        ) {
            PloCareNavHost(
                audience = audience,
                onAudienceChange = { audience = it },
            )
        }
    }
}
