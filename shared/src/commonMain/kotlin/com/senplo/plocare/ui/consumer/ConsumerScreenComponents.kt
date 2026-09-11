package com.senplo.plocare.ui.consumer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor

@Composable
internal fun ConsumerScreenHeader(title: String, subtitle: String) {
    Text(text = title, color = PloCareColor.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    Text(text = subtitle, color = PloCareColor.TextSecondary, fontSize = 13.sp)
}

@Composable
internal fun ConsumerSectionTitle(title: String, caption: String? = null) {
    Text(text = title, color = PloCareColor.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    if (caption != null) {
        Spacer(Modifier.height(4.dp))
        Text(text = caption, color = PloCareColor.TextTertiary, fontSize = 11.sp)
    }
}

@Composable
internal fun ConsumerCard(
    modifier: Modifier = Modifier,
    borderColor: Color = PloCareColor.SurfaceBorder,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PloCareColor.SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}
