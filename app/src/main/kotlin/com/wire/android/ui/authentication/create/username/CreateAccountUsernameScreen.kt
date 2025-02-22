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

package com.wire.android.ui.authentication.create.username

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wire.android.R
import com.wire.android.ui.common.ShakeAnimation
import com.wire.android.ui.common.button.WireButtonState
import com.wire.android.ui.common.error.CoreFailureErrorDialog
import com.wire.android.ui.common.textfield.AutoFillTextField
import com.wire.android.ui.common.button.WirePrimaryButton
import com.wire.android.ui.common.textfield.WireTextField
import com.wire.android.ui.common.textfield.WireTextFieldState
import com.wire.android.ui.common.textfield.clearAutofillTree
import com.wire.android.ui.common.topappbar.WireCenterAlignedTopAppBar
import com.wire.android.ui.theme.wireDimensions
import com.wire.android.ui.theme.wireTypography

@Composable
fun CreateAccountUsernameScreen() {
    val viewModel: CreateAccountUsernameViewModel = hiltViewModel()
    UsernameContent(
        state = viewModel.state,
        onUsernameChange = viewModel::onUsernameChange,
        onContinuePressed = viewModel::onContinue,
        onErrorDismiss = viewModel::onErrorDismiss,
        onUsernameErrorAnimated = viewModel::onUsernameErrorAnimated
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun UsernameContent(
    state: CreateAccountUsernameViewState,
    onUsernameChange: (TextFieldValue) -> Unit,
    onContinuePressed: () -> Unit,
    onErrorDismiss: () -> Unit,
    onUsernameErrorAnimated: () -> Unit
) {
    Scaffold(
        topBar = {
            WireCenterAlignedTopAppBar(
                elevation = 0.dp,
                title = stringResource(id = R.string.create_account_username_title),
                navigationIconType = null
            )
        },
    ) { internalPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(internalPadding)
        ) {
            Text(
                text = stringResource(id = R.string.create_account_username_text),
                style = MaterialTheme.wireTypography.body01,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.wireDimensions.spacing16x,
                        vertical = MaterialTheme.wireDimensions.spacing24x
                    )
            )
            UsernameTextField(
                state = state,
                onUsernameChange = onUsernameChange,
                onUsernameErrorAnimated = onUsernameErrorAnimated
            )
            Spacer(modifier = Modifier.weight(1f))
            WirePrimaryButton(
                text = stringResource(R.string.label_confirm),
                onClick = onContinuePressed,
                fillMaxWidth = true,
                loading = state.loading,
                state = if (state.continueEnabled) WireButtonState.Default else WireButtonState.Disabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.wireDimensions.spacing16x)
            )
        }
    }
    if (state.error is CreateAccountUsernameViewState.UsernameError.DialogError.GenericError)
        CoreFailureErrorDialog(state.error.coreFailure, onErrorDismiss)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun UsernameTextField(
    state: CreateAccountUsernameViewState,
    onUsernameChange: (TextFieldValue) -> Unit,
    onUsernameErrorAnimated: () -> Unit
) {
    clearAutofillTree()

    val keyboardController = LocalSoftwareKeyboardController.current
    ShakeAnimation { animate ->
        if(state.animateUsernameError) {
            animate()
            onUsernameErrorAnimated()
        }
        AutoFillTextField(
            autofillTypes = listOf(AutofillType.Username),
            value = state.username,
            onValueChange = onUsernameChange,
            placeholderText = stringResource(R.string.create_account_username_placeholder),
            labelText = stringResource(R.string.create_account_username_label),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_mention),
                    contentDescription = stringResource(R.string.content_description_mention_icon),
                    modifier = Modifier.padding(
                        start = MaterialTheme.wireDimensions.spacing16x,
                        end = MaterialTheme.wireDimensions.spacing8x
                    )
                )
            },
            state = if (state.error is CreateAccountUsernameViewState.UsernameError.TextFieldError) when(state.error) {
                CreateAccountUsernameViewState.UsernameError.TextFieldError.UsernameTakenError ->
                    WireTextFieldState.Error(stringResource(id = R.string.create_account_username_taken_error))
                CreateAccountUsernameViewState.UsernameError.TextFieldError.UsernameInvalidError ->
                    WireTextFieldState.Error(stringResource(id = R.string.create_account_username_description))
            } else WireTextFieldState.Default,
            descriptionText = stringResource(id = R.string.create_account_username_description),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
            modifier = Modifier.padding(horizontal = MaterialTheme.wireDimensions.spacing16x)
        )
    }
}

@Composable
@Preview
private fun PreviewCreateAccountUsernameScreen() {
    UsernameContent(CreateAccountUsernameViewState(), {}, {}, {}, {})
}
