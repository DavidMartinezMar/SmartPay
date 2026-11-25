package com.smart.smartpay.navigation

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smart.smartpay.features.history.HistoryScreen
import com.smart.smartpay.features.history.TransactionDetailScreen
import com.smart.smartpay.features.payment.PaymentScreen
import com.smart.smartpay.features.sale.SaleScreen
import com.smart.smartpay.features.splash.SplashScreen
import com.smart.smartpay.features.tip.TipScreen

/**
 * App navigation graph.
 *
 * Each destination handles its own insets: the splash is drawn edge-to-edge (to match the
 * system splash), the rest use safeDrawingPadding().
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier,
    ) {
        composable<SplashRoute> {
            SplashScreen(
                onFinished = {
                    navController.navigate(SaleRoute) {
                        // Remove the splash from the back stack: back on Sale exits the app
                        popUpTo<SplashRoute> { inclusive = true }
                    }
                },
            )
        }

        composable<SaleRoute> {
            SaleScreen(
                onCharge = { amountInCents, askForTip ->
                    // Tip disabled by the terminal parameters: straight to the payment, without tip
                    navController.navigate(if (askForTip) TipRoute(amountInCents) else PaymentRoute(amountInCents, 0L))
                },
                onHistory = { navController.navigate(HistoryRoute) },
                modifier = Modifier.safeDrawingPadding(),
            )
        }

        composable<HistoryRoute> {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onTransactionSelected = { id -> navController.navigate(TransactionDetailRoute(id)) },
                modifier = Modifier.safeDrawingPadding(),
            )
        }

        composable<TransactionDetailRoute> {
            // TransactionDetailViewModel reads the id from the route (SavedStateHandle)
            TransactionDetailScreen(
                onBack = { navController.popBackStack() },
                // TODO: cancellation/refund disabled while they depend on common
                onCancelSale = { _, _ -> },
                onRefund = { _, _ -> },
                onTransactionSelected = { id -> navController.navigate(TransactionDetailRoute(id)) },
                modifier = Modifier.safeDrawingPadding(),
            )
        }

        composable<TipRoute> {
            // TipViewModel reads the amount from the route (SavedStateHandle)
            TipScreen(
                onContinue = { amountInCents, tipInCents ->
                    navController.navigate(PaymentRoute(amountInCents, tipInCents))
                },
                modifier = Modifier.safeDrawingPadding(),
            )
        }

        composable<PaymentRoute> {
            // PaymentViewModel reads the amounts from the route (SavedStateHandle)
            PaymentScreen(
                onNewSale = { navController.navigateToNewSale() },
                // TODO: cancellation/refund disabled while they depend on common
                onCancelSale = { _, _ -> },
                onRefund = { _, _ -> },
                modifier = Modifier.safeDrawingPadding(),
            )
        }
    }
}

/** Fresh calculator; nothing of the finished transaction stays in the back stack. */
private fun NavHostController.navigateToNewSale() {
    navigate(SaleRoute) {
        popUpTo<SaleRoute> { inclusive = true }
    }
}

/** History over a fresh calculator: back from it returns to selling. */
private fun NavHostController.navigateToHistory() {
    navigate(HistoryRoute) {
        popUpTo<SaleRoute>()
    }
}
