package com.senplo.plocare

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PloCare",
        state = rememberWindowState(width = 440.dp, height = 860.dp),
    ) {
        App()
    }
}