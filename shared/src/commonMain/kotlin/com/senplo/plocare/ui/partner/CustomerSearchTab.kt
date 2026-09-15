package com.senplo.plocare.ui.partner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senplo.plocare.ui.theme.PloCareColor

@Composable
internal fun CustomerSearchTab(
    visitViews: List<PartnerVisitView>,
    onOpenCustomer: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val visits = visitViews.map { it.visit }
    val visibleCustomers = searchPartnerVisits(query, visits).mapNotNull { match ->
        visitViews.find { it.customerId == match.customerId }
    }

    fun openExactOrKeepList() {
        findPartnerVisit(query, visits)?.let { onOpenCustomer(it.customerId) }
    }

    PartnerPage("고객 조회", "고객 번호로 검색하거나 담당 고객을 확인하세요") {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("고객 번호") },
            placeholder = { Text("예: C-1024") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { openExactOrKeepList() }),
            trailingIcon = {
                Text(
                    "검색",
                    color = PloCareColor.AquaTeal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { openExactOrKeepList() },
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PloCareColor.TextPrimary,
                unfocusedTextColor = PloCareColor.TextPrimary,
                focusedBorderColor = PloCareColor.AquaTeal,
                unfocusedBorderColor = PloCareColor.SurfaceBorder,
                focusedLabelColor = PloCareColor.AquaTeal,
                unfocusedLabelColor = PloCareColor.TextSecondary,
                focusedContainerColor = PloCareColor.SurfaceDark,
                unfocusedContainerColor = PloCareColor.SurfaceDark,
            ),
        )
        SectionTitle("교체 긴급 고객", "필터 소진율 100% 이상")
        visitViews.firstOrNull { it.urgent && !it.completed }?.let { urgent ->
            CustomerCard(urgent, onOpenCustomer = { onOpenCustomer(urgent.customerId) })
        }
        SectionTitle("담당 고객", "긴급도 높은 순 · ${visibleCustomers.size}명 표시")
        if (visibleCustomers.isEmpty()) {
            EmptySearchResult(query)
        } else {
            visibleCustomers.sortedByDescending { it.urgent }.forEach { customer ->
                CustomerCard(customer, onOpenCustomer = { onOpenCustomer(customer.customerId) })
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun CustomerCard(customer: PartnerVisitView, onOpenCustomer: () -> Unit) {
    PartnerCard(onClick = onOpenCustomer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(customer.displayName, color = PloCareColor.TextPrimary, fontWeight = FontWeight.Bold)
                    if (customer.urgent) {
                        Spacer(Modifier.width(7.dp))
                        StatusPill("교체 긴급", PloCareColor.StatusAlert)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(customer.address, color = PloCareColor.TextSecondary, fontSize = 12.sp)
                Text("${customer.filtersLabel} · ${customer.exhaustionLabel}", color = if (customer.urgent) PloCareColor.StatusAlert else PloCareColor.TextTertiary, fontSize = 11.sp)
            }
            Text("현장 화면  ›", color = PloCareColor.AquaTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptySearchResult(query: String) {
    PartnerCard {
        Text("검색 결과가 없습니다", color = PloCareColor.TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text("‘$query’ 고객 번호를 다시 확인해 주세요.", color = PloCareColor.TextSecondary, fontSize = 12.sp)
    }
}
