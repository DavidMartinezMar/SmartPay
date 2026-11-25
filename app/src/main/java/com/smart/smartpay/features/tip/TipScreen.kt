package com.smart.smartpay.features.tip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.smart.smartpay.R
import com.smart.smartpay.ui.format.formatAmount
import com.smart.smartpay.ui.theme.SmartPayAppTheme

/**
 * Tip screen: sale summary (amount, tip, total) and the tip options downloaded with the
 * profiler authentication (tipProcessing): percentages and, if enabled, a free amount.
 *
 * @param onContinue invoked with the sale amount and the selected tip, both in cents.
 */
@Composable
fun TipScreen(
    onContinue: (amountInCents: Long, tipInCents: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TipViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCustomTipDialog by rememberSaveable { mutableStateOf(false) }

    TipContent(
        uiState = uiState,
        onOptionSelected = { option ->
            when (option) {
                TipOption.None -> viewModel.onNoTipSelected()
                is TipOption.Percentage -> viewModel.onPercentageSelected(option.percentage)
                TipOption.Custom -> showCustomTipDialog = true
            }
        },
        onContinue = { onContinue(uiState.amountInCents, uiState.tipInCents) },
        modifier = modifier,
    )

    if (showCustomTipDialog) {
        CustomTipDialog(
            initialAmountInCents = (uiState.selection as? TipSelection.Custom)?.amountInCents ?: 0L,
            maxAmountInCents = uiState.customTipMaxInCents,
            onConfirm = { amountInCents ->
                viewModel.onCustomTipEntered(amountInCents)
                showCustomTipDialog = false
            },
            onDismiss = { showCustomTipDialog = false },
        )
    }
}

/** Buttons shown in the tip options grid. */
private sealed interface TipOption {
    data object None : TipOption
    data class Percentage(val percentage: Int) : TipOption
    data object Custom : TipOption
}

@Composable
private fun TipContent(
    uiState: TipUiState,
    onOptionSelected: (TipOption) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.tip_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SaleSummary(uiState)

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.hasTipOptions) {
                TipOptions(uiState = uiState, onOptionSelected = onOptionSelected)
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onContinue,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(
                text = stringResource(R.string.tip_continue_button, formatAmount(uiState.totalInCents)),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun SaleSummary(uiState: TipUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.tip_summary_title),
                style = MaterialTheme.typography.titleMedium,
            )
            SummaryRow(stringResource(R.string.tip_summary_amount), uiState.amountInCents)
            SummaryRow(stringResource(R.string.tip_summary_tip), uiState.tipInCents)
            HorizontalDivider()
            SummaryRow(stringResource(R.string.tip_summary_total), uiState.totalInCents, isTotal = true)
        }
    }
}

@Composable
private fun SummaryRow(label: String, amountInCents: Long, isTotal: Boolean = false) {
    val style = if (isTotal) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge
    val weight = if (isTotal) FontWeight.Bold else FontWeight.Normal
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = style, fontWeight = weight, modifier = Modifier.weight(1f))
        Text(text = formatAmount(amountInCents), style = style, fontWeight = weight)
    }
}

@Composable
private fun TipOptions(
    uiState: TipUiState,
    onOptionSelected: (TipOption) -> Unit,
) {
    // "No tip" first, then the configured percentages, then the free amount if enabled
    val options = buildList {
        add(TipOption.None)
        uiState.tipPercentages.forEach { add(TipOption.Percentage(it)) }
        if (uiState.customTipEnabled) add(TipOption.Custom)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.tip_options_title),
            style = MaterialTheme.typography.titleMedium,
        )
        options.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { option ->
                    TipOptionButton(
                        option = option,
                        uiState = uiState,
                        onClick = { onOptionSelected(option) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Keep the last button half-width when the row is incomplete
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TipOptionButton(
    option: TipOption,
    uiState: TipUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selection = uiState.selection
    val selected = when (option) {
        TipOption.None -> selection == TipSelection.None
        is TipOption.Percentage -> selection == TipSelection.Percentage(option.percentage)
        TipOption.Custom -> selection is TipSelection.Custom
    }
    val title = when (option) {
        TipOption.None -> stringResource(R.string.tip_option_none)
        is TipOption.Percentage -> stringResource(R.string.tip_option_percentage, option.percentage)
        TipOption.Custom -> stringResource(R.string.tip_option_custom)
    }
    val subtitle = when (option) {
        TipOption.None -> null
        is TipOption.Percentage -> formatAmount(calculateTip(uiState.amountInCents, option.percentage))
        TipOption.Custom -> (selection as? TipSelection.Custom)?.let { formatAmount(it.amountInCents) }
    }

    val content: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
    val buttonModifier = modifier.height(64.dp)
    if (selected) {
        Button(onClick = onClick, modifier = buttonModifier) { content() }
    } else {
        OutlinedButton(onClick = onClick, modifier = buttonModifier) { content() }
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun TipScreenPreview() {
    SmartPayAppTheme {
        TipContent(
            uiState = TipUiState(
                amountInCents = 12550,
                tipPercentages = listOf(10, 15, 20),
                customTipEnabled = true,
                selection = TipSelection.Custom(2000),
                isLoading = false,
            ),
            onOptionSelected = {},
            onContinue = {},
        )
    }
}
