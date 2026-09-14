package com.senplo.plocare.ui.partner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor

private data class StockFixture(val name: String, val required: Int, val loaded: Int)

private val stocks = listOf(
    StockFixture("1단계 세디먼트", 6, 8),
    StockFixture("2단계 프리카본", 4, 5),
    StockFixture("3단계 RO 멤브레인", 1, 1),
    StockFixture("4단계 포스트카본", 3, 4),
)

@Composable
internal fun InventoryTab() {
    var loadedConfirmed by remember { mutableStateOf(false) }

    PartnerPage("차량 재고 관리", "스타리아 12가 3456 · 김파트너") {
        SectionTitle("오늘 필요 수량", "방문 8건의 교체 예정 필터 자동 합산")
        PartnerCard { stocks.forEach { InventoryRow(it, showLoaded = false) } }
        SectionTitle("현재 트렁크 재고", "출발 전 부족 수량을 확인하세요")
        PartnerCard {
            stocks.forEach { InventoryRow(it, showLoaded = true) }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { loadedConfirmed = !loadedConfirmed },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (loadedConfirmed) PloCareColor.StatusGood else PloCareColor.AquaTeal,
                    contentColor = PloCareColor.BgDeep,
                ),
            ) {
                Text(if (loadedConfirmed) "차량 적재 확인 완료" else "차량 적재 확인", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        PartnerCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("수동 재고 등록", color = PloCareColor.TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("추가 적재 또는 현장 사용 수량을 반영합니다.", color = PloCareColor.TextSecondary, fontSize = 12.sp)
                }
                Text("등록  ›", color = PloCareColor.AquaTeal, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InventoryRow(stock: StockFixture, showLoaded: Boolean) {
    val amount = if (showLoaded) stock.loaded else stock.required
    val state = when {
        !showLoaded -> "필요"
        stock.loaded > stock.required -> "여유"
        stock.loaded == stock.required -> "정확"
        else -> "부족"
    }
    val stateColor = when (state) {
        "여유" -> PloCareColor.StatusGood
        "정확" -> PloCareColor.StatusWarn
        "부족" -> PloCareColor.StatusAlert
        else -> PloCareColor.VividCyan
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(stock.name, color = PloCareColor.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text("${amount}개", color = PloCareColor.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        StatusPill(state, stateColor)
    }
}
