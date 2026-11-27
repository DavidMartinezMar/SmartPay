package com.smart.smartpay.presentation.ui.screens.splash

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smart.smartpay.R
import com.smart.smartpay.core.PaymentSdkManager
import com.smart.smartpay.domain.models.BoardingOption
import com.smart.smartpay.presentation.features.splash.SplashViewModel
import com.smart.smartpay.presentation.models.SplashUiState

/**
 * Splash shown right after the system splash (core-splashscreen), which can't show
 * a background image or a progress bar. It reproduces the same look (logo centered,
 * same size) so the hand-off isn't noticeable, plus background.webp and the startup status:
 * progress while authenticating the terminal, onboarding selection, or an error with retry.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnFinished by rememberUpdatedState(onFinished)
    val context = LocalContext.current

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        if (results.values.all { it }) viewModel.onPermissionsGranted() else viewModel.onPermissionsDenied()
    }

    val requestPermissionsOrStart = {
        if (PaymentSdkManager.hasRequiredPermissions(context)) {
            viewModel.onPermissionsGranted()
        } else {
            permissionsLauncher.launch(PaymentSdkManager.requiredPermissions)
        }
    }

    LaunchedEffect(Unit) {
        requestPermissionsOrStart()
    }

    LaunchedEffect(uiState) {
        if (uiState == SplashUiState.Ready) currentOnFinished()
    }

    SplashContent(
        uiState = uiState,
        onRetry = {
            //if (uiState == SplashUiState.PermissionsDenied) requestPermissionsOrStart() else viewModel.onRetry()
        },
        modifier = modifier,
    )

    (uiState as? SplashUiState.BoardingSelection)?.let { state ->
        BoardingSelectionDialog(options = state.options, onSelected = viewModel::onBoardingSelected)
    }
}

@Composable
private fun SplashContent(
    uiState: SplashUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Drawn edge-to-edge (no insets) to match the system splash exactly
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Same size and position as res/drawable/splash_logo.xml (system splash icon)
        Image(
            painter = painterResource(R.drawable.smart_logo_splash),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 144.dp, height = 54.dp),
        )
        when (uiState) {
            is SplashUiState.Error, SplashUiState.PermissionsDenied -> ErrorContent(
                message = when (uiState) {
                    is SplashUiState.Error -> stringResource(R.string.splash_error_message, uiState.message, uiState.code)
                    else -> stringResource(R.string.splash_permissions_denied)
                },
                onRetry = onRetry,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 120.dp)
                    .padding(horizontal = 32.dp),
            )

            else -> LinearProgressIndicator(
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 72.dp)
                    .width(144.dp),
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        ) {
            Text(stringResource(R.string.splash_retry))
        }
    }
}

@Composable
private fun BoardingSelectionDialog(
    options: List<BoardingOption>,
    onSelected: (BoardingOption) -> Unit,
) {
    AlertDialog(
        // Choosing an onboarding is required to continue
        onDismissRequest = {},
        title = { Text(stringResource(R.string.splash_boarding_title)) },
        text = {
            LazyColumn {
                items(options, key = { it.id }) { option ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(option) }
                            .padding(vertical = 12.dp),
                    ) {
                        Text(option.name, style = MaterialTheme.typography.titleMedium)
                        if (option.description.isNotBlank()) {
                            Text(option.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {},
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SplashLoadingPreview() {
    SplashContent(uiState = SplashUiState.Loading, onRetry = {})
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SplashErrorPreview() {
    SplashContent(uiState = SplashUiState.Error("E01", "Terminal no registrada"), onRetry = {})
}
