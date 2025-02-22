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

package com.wire.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun WireTheme(
    isPreview: Boolean = false,
    wireColorScheme: WireColorScheme = WireColorSchemeTypes.currentTheme,
    wireTypography: WireTypography = WireTypographyTypes.currentScreenSize,
    wireDimensions: WireDimensions = WireDimensionsTypes.currentScreenSize.currentOrientation,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    CompositionLocalProvider(
        LocalWireColors provides wireColorScheme,
        LocalWireTypography provides wireTypography,
        LocalWireDimensions provides wireDimensions
    ) {
        MaterialTheme(
            colorScheme = wireColorScheme.toColorScheme(),
            typography = wireTypography.toTypography()
        ) {
            if (!isPreview) {
                val backgroundColor = MaterialTheme.wireColorScheme.background
                val darkIcons = MaterialTheme.wireColorScheme.useDarkSystemBarIcons
                SideEffect { systemUiController.setSystemBarsColor(color = backgroundColor, darkIcons = darkIcons) }
            }
            content()
        }
    }
}

private val LocalWireColors = staticCompositionLocalOf { WireColorSchemeTypes.light }
private val LocalWireTypography = staticCompositionLocalOf { WireTypographyTypes.defaultPhone }
private val LocalWireDimensions = staticCompositionLocalOf { WireDimensionsTypes.defaultPhone.portrait }

val MaterialTheme.wireColorScheme
    @Composable
    get() = LocalWireColors.current

val MaterialTheme.wireTypography
    @Composable
    get() = LocalWireTypography.current

val MaterialTheme.wireDimensions
    @Composable
    get() = LocalWireDimensions.current
