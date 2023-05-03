package com.tangem.tap.features.customtoken.impl.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tangem.core.ui.res.TangemColorPalette
import com.tangem.core.ui.res.TangemTheme
import com.tangem.tap.features.details.ui.cardsettings.TextReference
import com.tangem.tap.features.details.ui.cardsettings.resolveReference
import com.tangem.wallet.R

/**
 * Add custom token warning component
 * FIXME("Incorrect typography. Replace with typography from design system")
 *
 * @param description warning description
 * @param modifier    modifier
 *
 * @author Andrew Khokhlov on 23/04/2023
 */
@Composable
internal fun AddCustomTokenWarning(description: TextReference, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = TangemTheme.shapes.roundedCornersSmall2,
        backgroundColor = TangemColorPalette.Tangerine,
        contentColor = TangemColorPalette.White,
        elevation = TangemTheme.dimens.elevation4,
    ) {
        Column(
            modifier = Modifier.padding(all = TangemTheme.dimens.spacing16),
            verticalArrangement = Arrangement.spacedBy(TangemTheme.dimens.spacing8),
        ) {
            Text(
                text = stringResource(id = R.string.common_warning),
                maxLines = 1,
                style = TangemTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = description.resolveReference(),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
    }
}
