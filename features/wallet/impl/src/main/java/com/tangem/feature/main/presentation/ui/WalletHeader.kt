package com.tangem.feature.main.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.tangem.core.ui.R
import com.tangem.core.ui.components.*
import com.tangem.core.ui.res.TangemTheme
import com.valentinilk.shimmer.shimmer

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WalletHeaderCard(config: WalletHeaderConfig, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.defaultMinSize(minHeight = TangemTheme.dimens.size108),
        shape = TangemTheme.shapes.roundedCornersXMedium,
        color = TangemTheme.colors.background.primary,
        onClick = config.onClick ?: {},
        enabled = config.onClick != null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(intrinsicSize = IntrinsicSize.Max),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.padding(all = TangemTheme.dimens.spacing12),
                verticalArrangement = Arrangement.spacedBy(TangemTheme.dimens.spacing8),
            ) {
                HeaderWalletName(
                    walletName = config.walletName,
                    balanceState = config.balanceState,
                )
                HeaderBalanceTitle(
                    balance = config.balance,
                    balanceState = config.balanceState,
                )
                Text(
                    text = config.additionalInfo,
                    color = TangemTheme.colors.text.disabled,
                    style = TangemTheme.typography.caption,
                )
            }
            Image(
                modifier = Modifier
                    .width(TangemTheme.dimens.size120)
                    .padding(end = TangemTheme.dimens.spacing18)
                    .fillMaxHeight(),
                painter = config.cardImage,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
            )
        }
    }
}

@Composable
private fun HeaderWalletName(walletName: String, balanceState: BalanceUIState) {
    if (balanceState == BalanceUIState.HIDDEN) {
        Row(horizontalArrangement = Arrangement.spacedBy(TangemTheme.dimens.spacing4)) {
            Text(
                text = walletName,
                color = TangemTheme.colors.text.tertiary,
                style = TangemTheme.typography.body2,
                maxLines = 1,
            )
            Icon(
                modifier = Modifier.size(size = TangemTheme.dimens.size20),
                painter = painterResource(id = R.drawable.ic_eye_off_24),
                contentDescription = null,
                tint = TangemTheme.colors.icon.informative,
            )
        }
    } else {
        Text(
            text = walletName,
            color = TangemTheme.colors.text.tertiary,
            style = TangemTheme.typography.body2,
            maxLines = 1,
        )
    }
}

@Composable
private fun HeaderBalanceTitle(balance: String, balanceState: BalanceUIState) {
    when (balanceState) {
        BalanceUIState.VISIBLE -> {
            ResizableText(
                text = balance,
                color = TangemTheme.colors.text.primary1,
                style = TangemTheme.typography.h2,
                fontSizeRange = FontSizeRange(min = 16.sp, max = TangemTheme.typography.h2.fontSize),
                modifier = Modifier.defaultMinSize(minHeight = TangemTheme.dimens.size32),
            )
        }
        BalanceUIState.LOADING -> {
            Box(
                modifier = Modifier
                    .padding(vertical = TangemTheme.dimens.spacing4)
                    .size(width = TangemTheme.dimens.size102, height = TangemTheme.dimens.size24)
                    .shimmer()
                    .background(
                        color = TangemTheme.colors.button.secondary,
                        shape = RoundedCornerShape(TangemTheme.dimens.radius6),
                    ),
            )
        }
        BalanceUIState.HIDDEN -> {
            Text(
                text = "•••",
                color = TangemTheme.colors.text.primary1,
                style = TangemTheme.typography.h2,
            )
        }
        BalanceUIState.NONE -> {
            Text(
                text = "—",
                color = TangemTheme.colors.text.primary1,
                style = TangemTheme.typography.h2,
            )
        }
    }
}

// region Preview

@Composable
private fun WarningsPreview() {
    val walletHeaderConfig = WalletHeaderConfig(
        walletName = "Wallet 1",
        balance = "8923,05 $",
        additionalInfo = "3 cards • Seed enabled",
        balanceState = BalanceUIState.VISIBLE,
        cardImage = painterResource(id = R.drawable.ill_businessman_3d),
        onClick = {},
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        WalletHeaderCard(
            config = walletHeaderConfig,
        )
        SpacerH32()
        WalletHeaderCard(
            config = walletHeaderConfig.copy(balanceState = BalanceUIState.HIDDEN),
        )
        SpacerH32()
        WalletHeaderCard(
            config = walletHeaderConfig.copy(balanceState = BalanceUIState.LOADING),
        )
        SpacerH32()
        WalletHeaderCard(
            config = walletHeaderConfig.copy(balanceState = BalanceUIState.NONE),
        )
        SpacerH32()
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_Warning_InLightTheme() {
    TangemTheme(isDark = false) {
        WarningsPreview()
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_Warning_InDarkTheme() {
    TangemTheme(isDark = true) {
        WarningsPreview()
    }
}

// endregion Preview
