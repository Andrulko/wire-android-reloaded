/*
 * Wire
 * Copyright (C) 2023 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 *
 *
 */

package com.wire.android.ui.common.button

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wire.android.R
import com.wire.android.ui.common.dimensions
import com.wire.android.ui.theme.wireDimensions

@Composable
fun WirePrimaryIconButton(
    onButtonClicked: () -> Unit,
    @DrawableRes iconResource: Int,
    @StringRes contentDescription: Int,
    shape: Shape = RoundedCornerShape(MaterialTheme.wireDimensions.buttonCornerSize),
    minHeight: Dp = dimensions().spacing32x,
    minWidth: Dp = dimensions().spacing40x,
    state: WireButtonState = WireButtonState.Default,
    colors: WireButtonColors = wirePrimaryButtonColors(),
    blockUntilSynced: Boolean = false,
    modifier: Modifier = Modifier
) {
    WirePrimaryButton(
        onClick = onButtonClicked,
        leadingIcon = {
            Icon(
                painter = painterResource(id = iconResource),
                contentDescription = stringResource(contentDescription),
                modifier = Modifier.size(dimensions().wireIconButtonSize)
            )
        },
        shape = shape,
        minHeight = minHeight,
        minWidth = minWidth,
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
        leadingIconAlignment = IconAlignment.Center,
        state = state,
        colors = colors,
        blockUntilSynced = blockUntilSynced,
        fillMaxWidth = false,
        modifier = modifier
    )
}

@Preview
@Composable
fun PreviewWirePrimaryIconButton() {
    WirePrimaryIconButton({}, R.drawable.ic_add, 0)
}
@Preview
@Composable
fun PreviewWirePrimaryIconButtonRound() {
    WirePrimaryIconButton({}, R.drawable.ic_add, 0, CircleShape, 40.dp, 40.dp)
}
