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

package com.wire.android.ui.authentication.create.email

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wire.android.R
import com.wire.android.ui.authentication.ServerTitle
import com.wire.android.ui.authentication.create.common.CreateAccountFlowType
import com.wire.android.ui.common.WireDialog
import com.wire.android.ui.common.WireDialogButtonProperties
import com.wire.android.ui.common.WireDialogButtonType
import com.wire.android.ui.common.button.WireButtonState
import com.wire.android.ui.common.button.WireSecondaryButton
import com.wire.android.ui.common.error.CoreFailureErrorDialog
import com.wire.android.ui.common.textfield.AutoFillTextField
import com.wire.android.ui.common.button.WirePrimaryButton
import com.wire.android.ui.common.textfield.WireTextField
import com.wire.android.ui.common.textfield.WireTextFieldState
import com.wire.android.ui.common.textfield.clearAutofillTree
import com.wire.android.ui.common.topappbar.WireCenterAlignedTopAppBar
import com.wire.android.ui.theme.wireColorScheme
import com.wire.android.ui.theme.wireDimensions
import com.wire.android.ui.theme.wireTypography
import com.wire.android.util.CustomTabsHelper
import com.wire.kalium.logic.configuration.server.ServerConfig

@Composable
fun CreateAccountEmailScreen(viewModel: CreateAccountEmailViewModel, serverConfig: ServerConfig.Links) {
    EmailContent(
        state = viewModel.emailState,
        onEmailChange = viewModel::onEmailChange,
        onBackPressed = viewModel::goBackToPreviousStep,
        onContinuePressed = { viewModel.onEmailContinue() },
        onLoginPressed = viewModel::openLogin,
        onTermsDialogDismiss = viewModel::onTermsDialogDismiss,
        onTermsAccept = { viewModel.onTermsAccept() },
        onErrorDismiss = viewModel::onEmailErrorDismiss,
        tosUrl = viewModel.tosUrl(),
        serverConfig = serverConfig
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun EmailContent(
    state: CreateAccountEmailViewState,
    onEmailChange: (TextFieldValue) -> Unit,
    onBackPressed: () -> Unit,
    onContinuePressed: () -> Unit,
    onLoginPressed: () -> Unit,
    onTermsDialogDismiss: () -> Unit,
    onTermsAccept: () -> Unit,
    onErrorDismiss: () -> Unit,
    tosUrl: String,
    serverConfig: ServerConfig.Links
) {
    clearAutofillTree()

    Scaffold(topBar = {
        WireCenterAlignedTopAppBar(
            elevation = 0.dp,
            title = stringResource(id = state.type.titleResId),
            onNavigationPressed = onBackPressed,
            subtitleContent = {
                if (serverConfig.isOnPremises) {
                    ServerTitle(
                        serverLinks = serverConfig,
                        style = MaterialTheme.wireTypography.body01
                    )
                }
            }
        )
    }) { internalPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(internalPadding)
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            Text(
                text = stringResource(id = state.type.emailResources.emailSubtitleResId),
                style = MaterialTheme.wireTypography.body01,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.wireDimensions.spacing16x,
                        vertical = MaterialTheme.wireDimensions.spacing24x
                    )
                    .testTag("createTeamText")
            )
            AutoFillTextField(
                autofillTypes = listOf(AutofillType.EmailAddress),
                value = state.email,
                onValueChange = onEmailChange,
                placeholderText = stringResource(R.string.create_account_email_placeholder),
                labelText = stringResource(R.string.create_account_email_label),
                state = if (state.error is CreateAccountEmailViewState.EmailError.None) WireTextFieldState.Default
                else WireTextFieldState.Error(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.wireDimensions.spacing16x)
                    .testTag("emailField")
            )
            AnimatedVisibility(visible = state.error !is CreateAccountEmailViewState.EmailError.None) {
                EmailErrorText(state.error)
            }
            Spacer(modifier = Modifier.weight(1f))
            EmailFooter(state = state, onLoginPressed = onLoginPressed, onContinuePressed = onContinuePressed)
        }
    }
    if (state.termsDialogVisible) {
        val context = LocalContext.current
        TermsConditionsDialog(
            onDialogDismiss = onTermsDialogDismiss,
            onContinuePressed = onTermsAccept,
            onViewPolicyPressed = { CustomTabsHelper.launchUrl(context, tosUrl) }
        )
    }
    if (state.error is CreateAccountEmailViewState.EmailError.DialogError.GenericError)
        CoreFailureErrorDialog(state.error.coreFailure, onErrorDismiss)
}

@Composable
private fun EmailErrorText(error: CreateAccountEmailViewState.EmailError) {
    val learnMoreTag = "learn_more"
    val context = LocalContext.current
    val learnMoreUrl = "https://support.wire.com/hc/en-us/articles/115004082129" //TODO should we keep it in a different way?
    val learnMoreText = stringResource(id = R.string.label_learn_more)
    val annotatedText = buildAnnotatedString {
        append(
            if (error is CreateAccountEmailViewState.EmailError.TextFieldError) when (error) {
                CreateAccountEmailViewState.EmailError.TextFieldError.AlreadyInUseError ->
                    stringResource(R.string.create_account_email_already_in_use_error)
                CreateAccountEmailViewState.EmailError.TextFieldError.BlacklistedEmailError ->
                    stringResource(R.string.create_account_email_blacklisted_error)
                CreateAccountEmailViewState.EmailError.TextFieldError.DomainBlockedError ->
                    stringResource(R.string.create_account_email_domain_blocked_error)
                CreateAccountEmailViewState.EmailError.TextFieldError.InvalidEmailError ->
                    stringResource(R.string.create_account_email_invalid_error)
            } else ""
        )
        if (error is CreateAccountEmailViewState.EmailError.TextFieldError.AlreadyInUseError) {
            append(" ")
            pushStringAnnotation(tag = learnMoreTag, annotation = learnMoreUrl)
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.wireColorScheme.onTertiaryButtonSelected,
                    fontWeight = MaterialTheme.wireTypography.label05.fontWeight,
                    fontSize = MaterialTheme.wireTypography.label05.fontSize,
                    textDecoration = TextDecoration.Underline
                )
            ) { append(learnMoreText) }
            pop()
        }
    }
    ClickableText(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = MaterialTheme.wireDimensions.spacing8x,
                horizontal = MaterialTheme.wireDimensions.spacing16x
            ),
        style = MaterialTheme.wireTypography.label04.copy(color = MaterialTheme.wireColorScheme.error, textAlign = TextAlign.Start),
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = learnMoreTag, start = offset, end = offset).firstOrNull()?.let {
                CustomTabsHelper.launchUrl(context, learnMoreUrl)
            }
        }
    )
}

@Composable
private fun EmailFooter(state: CreateAccountEmailViewState, onLoginPressed: () -> Unit, onContinuePressed: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = MaterialTheme.wireDimensions.spacing16x)
    ) {
        Text(
            text = "${stringResource(R.string.create_account_email_footer_text)} ",
            style = MaterialTheme.wireTypography.body02,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.label_login),
            style = MaterialTheme.wireTypography.body02.copy(textDecoration = TextDecoration.Underline),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onLoginPressed
            )
        )
    }
    WirePrimaryButton(
        text = stringResource(R.string.label_continue),
        onClick = onContinuePressed,
        fillMaxWidth = true,
        loading = state.loading,
        state = if (state.continueEnabled) WireButtonState.Default else WireButtonState.Disabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.wireDimensions.spacing16x),
    )
}

@Composable
private fun TermsConditionsDialog(onDialogDismiss: () -> Unit, onContinuePressed: () -> Unit, onViewPolicyPressed: () -> Unit) {
    WireDialog(
        title = stringResource(R.string.create_account_email_terms_dialog_title),
        text = stringResource(R.string.create_account_email_terms_dialog_text),
        onDismiss = onDialogDismiss,
        optionButton1Properties = WireDialogButtonProperties(
            onClick = onContinuePressed,
            text = stringResource(id = R.string.label_continue),
            type = WireDialogButtonType.Primary,
        )
    ) {
        Column {
            WireSecondaryButton(
                text = stringResource(R.string.label_cancel),
                onClick = onDialogDismiss,
                fillMaxWidth = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.wireDimensions.spacing8x)
                    .testTag("cancelButton"),
            )
            WireSecondaryButton(
                text = stringResource(R.string.create_account_email_terms_dialog_view_policy),
                onClick = onViewPolicyPressed,
                fillMaxWidth = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("viewTC")
            )
        }
    }
}

@Composable
@Preview
fun PreviewCreateAccountEmailScreen() {
    EmailContent(CreateAccountEmailViewState(CreateAccountFlowType.CreatePersonalAccount), {}, {}, {}, {}, {}, {}, {}, "",
        ServerConfig.DEFAULT)
}
