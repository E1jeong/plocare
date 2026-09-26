package com.senplo.plocare.ui.partner

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor

private data class StockFixture(val stage: Int, val name: String)

private val stocks = listOf(
    StockFixture(1, "1단계 세디먼트"),
    StockFixture(2, "2단계 프리카본"),
    StockFixture(3, "3단계 RO 멤브레인"),
    StockFixture(4, "4단계 포스트카본"),
)

@Composable
internal fun InventoryTab(
    visitViews: List<PartnerVisitView>,
    loaded: Map<Int, Int>,
    loadedConfirmed: Boolean,
    onAdjustStock: (Int, Int) -> Unit,
    onToggleLoadedConfirmation: () -> Unit,
) {
    val required = inventoryRequirements(visitViews)
    val canConfirm = canConfirmInventory(required, loaded)

    PartnerPage("차량 재고 관리", "스타리아 12가 3456 · 김파트너") {
        SectionTitle("오늘 필요 수량", "미완료 방문 ${visitViews.count { !it.completed }}건의 교체 예정 필터 합산")
        PartnerCard { stocks.forEach { InventoryRow(it.name, required[it.stage] ?: 0, required[it.stage] ?: 0, showLoaded = false) } }
        SectionTitle("현재 트렁크 재고", "출발 전 부족 수량을 확인하세요")
        PartnerCard {
            stocks.forEach { stock ->
                InventoryRow(stock.name, required[stock.stage] ?: 0, loaded[stock.stage] ?: 0, showLoaded = true)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onToggleLoadedConfirmation,
                enabled = loadedConfirmed || canConfirm,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (loadedConfirmed) PloCareColor.StatusGood else PloCareColor.AquaTeal,
                    contentColor = PloCareColor.BgDeep,
                ),
            ) {
                Text(if (loadedConfirmed) "차량 적재 확인 완료" else "차량 적재 확인", fontWeight = FontWeight.Bold)
            }
            if (!canConfirm) {
                Text("부족한 필터를 적재한 뒤 확인할 수 있습니다.", color = PloCareColor.StatusAlert, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        PartnerCard {
            Text("수동 재고 등록", color = PloCareColor.TextPrimary, fontWeight = FontWeight.SemiBold)
            Text("추가 적재는 +, 현장 사용은 −로 반영합니다. 이 기기에서만 유지됩니다.", color = PloCareColor.TextSecondary, fontSize = 12.sp)
            stocks.forEach { stock ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stock.name, color = PloCareColor.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Button(onClick = {
                        onAdjustStock(stock.stage, -1)
                    }, modifier = Modifier.semantics {
                        contentDescription = "${stock.name} 재고 1개 사용"
                    }) { Text("−") }
                    Spacer(Modifier.width(6.dp))
                    Button(onClick = {
                        onAdjustStock(stock.stage, 1)
                    }, modifier = Modifier.semantics {
                        contentDescription = "${stock.name} 재고 1개 추가"
                    }) { Text("+") }
                }
            }
        }
    }
}

@Composable
private fun InventoryRow(name: String, required: Int, loaded: Int, showLoaded: Boolean) {
    val amount = if (showLoaded) loaded else required
    val state = when {
        !showLoaded -> "필요"
        loaded > required -> "여유"
        loaded == required -> "정확"
        else -> "부족"
    }
    val stateColor = when (state) {
        "여유" -> PloCareColor.StatusGood
        "정확" -> PloCareColor.StatusWarn
        "부족" -> PloCareColor.StatusAlert
        else -> PloCareColor.VividCyan
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(name, color = PloCareColor.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text("${amount}개", color = PloCareColor.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        StatusPill(state, stateColor)
    }
}
