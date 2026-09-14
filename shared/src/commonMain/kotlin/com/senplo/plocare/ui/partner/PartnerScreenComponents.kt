package com.senplo.plocare.ui.partner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor

@Composable
internal fun PartnerPage(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("PLOCARE PARTNER", color = PloCareColor.AquaTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(8.dp))
        Text(title, color = PloCareColor.TextPrimary, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, color = PloCareColor.TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        content()
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
internal fun SectionTitle(title: String, description: String?) {
    Spacer(Modifier.height(18.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(title, color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        description?.let { Text(it, color = PloCareColor.TextTertiary, fontSize = 10.sp) }
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
internal fun PartnerCard(onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val modifier = if (onClick == null) Modifier.fillMaxWidth() else Modifier.fillMaxWidth().clickable(onClick = onClick)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PloCareColor.SurfaceCard),
        border = BorderStroke(1.dp, PloCareColor.SurfaceBorder),
    ) {
        Column(Modifier.fillMaxWidth().padding(17.dp), content = content)
    }
}

@Composable
internal fun InfoStrip(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PloCareColor.SurfaceDark).padding(13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = PloCareColor.TextSecondary, fontSize = 12.sp)
        Text(value, color = PloCareColor.VividCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun SmallAction(
    text: String,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier.clip(RoundedCornerShape(10.dp))
            .background(if (secondary) PloCareColor.SurfaceDark else PloCareColor.DeepBlue)
            .border(1.dp, if (secondary) PloCareColor.SurfaceBorder else PloCareColor.DeepBlue, RoundedCornerShape(10.dp))
            .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun StatusPill(text: String, color: Color) {
    Box(Modifier.padding(top = 5.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.16f)).padding(horizontal = 7.dp, vertical = 3.dp)) {
        Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
