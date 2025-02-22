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

package com.wire.android.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wire.android.ui.common.button.WireButtonState
import com.wire.android.ui.common.button.WireSecondaryButton
import com.wire.android.ui.common.button.WireTertiaryButton
import com.wire.android.ui.common.textfield.WirePasswordTextField
import com.wire.android.ui.common.button.WirePrimaryButton
import com.wire.android.ui.theme.WireTheme
import com.wire.android.ui.theme.wireDimensions
import com.wire.android.ui.theme.wireTypography

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WireDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    optionButton1Properties: WireDialogButtonProperties,
    optionButton2Properties: WireDialogButtonProperties? = null,
    dismissButtonProperties: WireDialogButtonProperties? = null,
    buttonsHorizontalAlignment: Boolean = true,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(MaterialTheme.wireDimensions.dialogCornerSize),
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.wireDimensions.dialogContentPadding),
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable (() -> Unit)? = null
) {
    WireDialog(
        onDismiss = onDismiss,
        properties = properties,
        optionButton1Properties = optionButton1Properties,
        optionButton2Properties = optionButton2Properties,
        dismissButtonProperties = dismissButtonProperties,
        buttonsHorizontalAlignment = buttonsHorizontalAlignment,
        modifier = modifier,
        shape = shape,
        contentPadding = contentPadding,
        title = title,
        text = buildAnnotatedString {
            val style = SpanStyle(
                color = colorsScheme().onBackground,
                fontWeight = MaterialTheme.wireTypography.body01.fontWeight,
                fontSize = MaterialTheme.wireTypography.body01.fontSize,
                fontFamily = MaterialTheme.wireTypography.body01.fontFamily,
                fontStyle = MaterialTheme.wireTypography.body01.fontStyle
            )
            withStyle(style) { append(text) }
        },
        content = content
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WireDialog(
    title: String,
    text: AnnotatedString? = null,
    onDismiss: () -> Unit,
    optionButton1Properties: WireDialogButtonProperties,
    optionButton2Properties: WireDialogButtonProperties? = null,
    dismissButtonProperties: WireDialogButtonProperties? = null,
    buttonsHorizontalAlignment: Boolean = true,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(MaterialTheme.wireDimensions.dialogCornerSize),
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.wireDimensions.dialogContentPadding),
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties
    ) {
        WireDialogContent(
            optionButton1Properties = optionButton1Properties,
            optionButton2Properties = optionButton2Properties,
            dismissButtonProperties = dismissButtonProperties,
            buttonsHorizontalAlignment = buttonsHorizontalAlignment,
            modifier = modifier,
            shape = shape,
            contentPadding = contentPadding,
            title = title,
            text = text,
            content = content
        )
    }
}

@Composable
private fun WireDialogContent(
    title: String,
    text: AnnotatedString? = null,
    optionButton1Properties: WireDialogButtonProperties,
    optionButton2Properties: WireDialogButtonProperties? = null,
    dismissButtonProperties: WireDialogButtonProperties? = null,
    buttonsHorizontalAlignment: Boolean = true,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(MaterialTheme.wireDimensions.dialogCornerSize),
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.wireDimensions.dialogContentPadding),
    content: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.padding(MaterialTheme.wireDimensions.dialogCardMargin),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            Text(
                text = title,
                style = MaterialTheme.wireTypography.title02,
                modifier = Modifier.padding(bottom = MaterialTheme.wireDimensions.dialogTextsSpacing)
            )
            text?.let {
                Text(
                    text = text,
                    style = MaterialTheme.wireTypography.body01,
                    modifier = Modifier.padding(bottom = MaterialTheme.wireDimensions.dialogTextsSpacing)
                )
            }
            content?.let {
                Box {
                    it.invoke()
                }
            }

            if (buttonsHorizontalAlignment)
                Row(Modifier.padding(top = MaterialTheme.wireDimensions.dialogButtonsSpacing)) {
                    dismissButtonProperties.getButton(Modifier.weight(1f))
                    if (dismissButtonProperties != null)
                        Spacer(Modifier.width(MaterialTheme.wireDimensions.dialogButtonsSpacing))
                    optionButton1Properties.getButton(Modifier.weight(1f))
                    if (optionButton2Properties != null)
                        Spacer(Modifier.width(MaterialTheme.wireDimensions.dialogButtonsSpacing))
                    optionButton2Properties.getButton(Modifier.weight(1f))
                }
            else
                Column(Modifier.padding(top = MaterialTheme.wireDimensions.dialogButtonsSpacing)) {
                    optionButton1Properties.getButton()

                    if (optionButton2Properties != null)
                        Spacer(Modifier.height(MaterialTheme.wireDimensions.dialogButtonsSpacing))
                    optionButton2Properties.getButton()

                    if (dismissButtonProperties != null)
                        Spacer(Modifier.height(MaterialTheme.wireDimensions.dialogButtonsSpacing))
                    dismissButtonProperties.getButton()
                }
        }
    }
}

@Composable
private fun WireDialogButtonProperties?.getButton(modifier: Modifier = Modifier) {
    this?.let {
        Box(modifier = modifier) {
            when (type) {
                WireDialogButtonType.Primary ->
                    WirePrimaryButton(onClick = onClick, text = text, state = state, loading = loading, modifier = modifier)
                WireDialogButtonType.Secondary ->
                    WireSecondaryButton(onClick = onClick, text = text, state = state, loading = loading, modifier = modifier)
                WireDialogButtonType.Tertiary ->
                    WireTertiaryButton(onClick = onClick, text = text, state = state, loading = loading, modifier = modifier)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview(showBackground = true)
@Composable
fun PreviewWireDialog() {
    var password by remember { mutableStateOf(TextFieldValue("")) }
    WireTheme(isPreview = true) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            WireDialogContent(
                optionButton1Properties = WireDialogButtonProperties(
                    text = "OK",
                    onClick = { },
                    type = WireDialogButtonType.Primary,
                    state = if (password.text.isEmpty()) WireButtonState.Disabled else WireButtonState.Error,
                ),
                dismissButtonProperties = WireDialogButtonProperties(
                    text = "Cancel",
                    onClick = { }
                ),
                title = "title",
                text = buildAnnotatedString {
                    val style = SpanStyle(
                        color = colorsScheme().onBackground,
                        fontWeight = MaterialTheme.wireTypography.body01.fontWeight,
                        fontSize = MaterialTheme.wireTypography.body01.fontSize,
                        fontFamily = MaterialTheme.wireTypography.body01.fontFamily,
                        fontStyle = MaterialTheme.wireTypography.body01.fontStyle
                    )
                    withStyle(style) { append("text") }
                },
            ) {
                WirePasswordTextField(
                    value = password,
                    onValueChange = { password = it })
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview(showBackground = true)
@Composable
fun PreviewWireDialogWith2OptionButtons() {
    var password by remember { mutableStateOf(TextFieldValue("")) }
    WireTheme(isPreview = true) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            WireDialogContent(
                optionButton1Properties = WireDialogButtonProperties(
                    text = "OK",
                    onClick = { },
                    type = WireDialogButtonType.Primary,
                    state = if (password.text.isEmpty()) WireButtonState.Disabled else WireButtonState.Error,
                ),
                optionButton2Properties = WireDialogButtonProperties(
                    text = "Later",
                    onClick = { },
                    type = WireDialogButtonType.Primary,
                    state = if (password.text.isEmpty()) WireButtonState.Disabled else WireButtonState.Error,
                ),
                dismissButtonProperties = WireDialogButtonProperties(
                    text = "Cancel",
                    onClick = { }
                ),
                title = "title",
                text = buildAnnotatedString {
                    val style = SpanStyle(
                        color = colorsScheme().onBackground,
                        fontWeight = MaterialTheme.wireTypography.body01.fontWeight,
                        fontSize = MaterialTheme.wireTypography.body01.fontSize,
                        fontFamily = MaterialTheme.wireTypography.body01.fontFamily,
                        fontStyle = MaterialTheme.wireTypography.body01.fontStyle
                    )
                    withStyle(style) { append("text") }
                },
                buttonsHorizontalAlignment = false
            ) {
                WirePasswordTextField(
                    value = password,
                    onValueChange = { password = it })
            }
        }
    }
}

enum class WireDialogButtonType { Primary, Secondary, Tertiary }

data class WireDialogButtonProperties(
    val text: String,
    val onClick: () -> Unit,
    val state: WireButtonState = WireButtonState.Default,
    val type: WireDialogButtonType = WireDialogButtonType.Secondary,
    val loading: Boolean = false
)
