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

package com.wire.android.ui.authentication.create.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wire.android.di.AuthServerConfigProvider
import com.wire.android.di.ClientScopeProvider
import com.wire.android.navigation.BackStackMode
import com.wire.android.navigation.NavigationCommand
import com.wire.android.navigation.NavigationItem
import com.wire.android.navigation.NavigationManager
import com.wire.android.ui.authentication.create.code.CreateAccountCodeViewModel
import com.wire.android.ui.authentication.create.code.CreateAccountCodeViewState
import com.wire.android.ui.authentication.create.details.CreateAccountDetailsViewModel
import com.wire.android.ui.authentication.create.details.CreateAccountDetailsViewState
import com.wire.android.ui.authentication.create.email.CreateAccountEmailViewModel
import com.wire.android.ui.authentication.create.email.CreateAccountEmailViewState
import com.wire.android.ui.authentication.create.overview.CreateAccountOverviewViewModel
import com.wire.android.ui.common.textfield.CodeFieldValue
import com.wire.android.util.WillNeverOccurError
import com.wire.kalium.logic.configuration.server.ServerConfig
import com.wire.kalium.logic.data.user.UserId
import com.wire.kalium.logic.feature.auth.AddAuthenticatedUserUseCase
import com.wire.kalium.logic.feature.auth.ValidateEmailUseCase
import com.wire.kalium.logic.feature.auth.ValidatePasswordUseCase
import com.wire.kalium.logic.feature.auth.autoVersioningAuth.AutoVersionAuthScopeUseCase
import com.wire.kalium.logic.feature.client.RegisterClientResult
import com.wire.kalium.logic.feature.client.RegisterClientUseCase.RegisterClientParam
import com.wire.kalium.logic.feature.register.RegisterParam
import com.wire.kalium.logic.feature.register.RegisterResult
import com.wire.kalium.logic.feature.register.RequestActivationCodeResult
import com.wire.kalium.logic.feature.server.FetchApiVersionResult
import com.wire.kalium.logic.feature.server.FetchApiVersionUseCase
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions", "LongParameterList")
abstract class CreateAccountBaseViewModel(
    final override val type: CreateAccountFlowType,
    private val savedStateHandle: SavedStateHandle,
    private val navigationManager: NavigationManager,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val authScope: AutoVersionAuthScopeUseCase,
    private val addAuthenticatedUser: AddAuthenticatedUserUseCase,
    private val clientScopeProviderFactory: ClientScopeProvider.Factory,
    private val authServerConfigProvider: AuthServerConfigProvider,
    private val fetchApiVersion: FetchApiVersionUseCase
) : ViewModel(),
    CreateAccountOverviewViewModel,
    CreateAccountEmailViewModel,
    CreateAccountDetailsViewModel,
    CreateAccountCodeViewModel {

    val serverConfig: ServerConfig.Links = authServerConfigProvider.authServer.value

    override fun tosUrl(): String = authServerConfigProvider.authServer.value.tos

    override fun learnMoreUrl(): String = authServerConfigProvider.authServer.value.pricing

    override var emailState: CreateAccountEmailViewState by mutableStateOf(
        CreateAccountEmailViewState(
            type,
            TextFieldValue(savedStateHandle.get<String>(CreateAccountDetailsViewModel.EMAIL).orEmpty())
        )
    )
    override var detailsState: CreateAccountDetailsViewState by mutableStateOf(CreateAccountDetailsViewState(type))
    override var codeState: CreateAccountCodeViewState by mutableStateOf(CreateAccountCodeViewState(type))

    fun closeForm() {
        viewModelScope.launch { navigationManager.navigateBack() }
    }

    // Overview
    final override fun onOverviewContinue() {
        emailState = CreateAccountEmailViewState(type)
        detailsState = CreateAccountDetailsViewState(type)
        codeState = CreateAccountCodeViewState(type)
        onOverviewSuccess()
    }

    abstract fun onOverviewSuccess()

    // Email
    final override fun onEmailChange(newText: TextFieldValue) {
        emailState = emailState.copy(
            email = newText,
            error = CreateAccountEmailViewState.EmailError.None,
            continueEnabled = newText.text.isNotEmpty() && !emailState.loading
        )
        codeState = codeState.copy(email = newText.text)
        savedStateHandle[CreateAccountDetailsViewModel.EMAIL] = newText.text
    }

    final override fun onEmailErrorDismiss() {
        emailState = emailState.copy(error = CreateAccountEmailViewState.EmailError.None)
    }

    final override fun onEmailContinue() {
        emailState = emailState.copy(loading = true, continueEnabled = false)
        viewModelScope.launch {
            fetchApiVersion(authServerConfigProvider.authServer.value).let {
                when (it) {
                    is FetchApiVersionResult.Success -> {}
                    is FetchApiVersionResult.Failure.UnknownServerVersion -> {
                        emailState = emailState.copy(showServerVersionNotSupportedDialog = true)
                        return@launch
                    }
                    is FetchApiVersionResult.Failure.TooNewVersion -> {
                        emailState = emailState.copy(showClientUpdateDialog = true)
                        return@launch
                    }
                    is FetchApiVersionResult.Failure.Generic -> {
                        return@launch
                    }
                }
            }

            val emailError =
                if (validateEmailUseCase(emailState.email.text.trim().lowercase())) CreateAccountEmailViewState.EmailError.None
                else CreateAccountEmailViewState.EmailError.TextFieldError.InvalidEmailError
            emailState = emailState.copy(
                loading = false,
                continueEnabled = true,
                termsDialogVisible = !emailState.termsAccepted && emailError is CreateAccountEmailViewState.EmailError.None,
                error = emailError
            )
            if (emailState.termsAccepted) onTermsAccept()
        }.invokeOnCompletion {
            emailState = emailState.copy(loading = false)
        }
    }

    final override fun onTermsAccept() {
        emailState = emailState.copy(loading = true, continueEnabled = false, termsDialogVisible = false, termsAccepted = true)
        viewModelScope.launch {
            val authScope = authScope().let {
                when (it) {
                    is AutoVersionAuthScopeUseCase.Result.Success -> it.authenticationScope

                    is AutoVersionAuthScopeUseCase.Result.Failure.UnknownServerVersion -> {
                        // TODO: show dialog
                        return@launch
                    }
                    is AutoVersionAuthScopeUseCase.Result.Failure.TooNewVersion -> {
                        // TODO: show dialog
                        return@launch
                    }
                    is AutoVersionAuthScopeUseCase.Result.Failure.Generic -> {
                        return@launch
                    }
                }
            }

            val emailError = authScope.registerScope.requestActivationCode(emailState.email.text.trim().lowercase()).toEmailError()
            emailState = emailState.copy(loading = false, continueEnabled = true, error = emailError)
            if (emailError is CreateAccountEmailViewState.EmailError.None) onTermsSuccess()
        }
    }

    final override fun onTermsDialogDismiss() {
        emailState = emailState.copy(termsDialogVisible = false)
    }

    abstract fun onTermsSuccess()
    final override fun openLogin() {
        viewModelScope.launch {
            navigationManager.navigate(
                NavigationCommand(
                    NavigationItem.Login.getRouteWithArgs(),
                    BackStackMode.CLEAR_TILL_START
                )
            )
        }
    }

    // Details
    final override fun onDetailsChange(newText: TextFieldValue, fieldType: CreateAccountDetailsViewModel.DetailsFieldType) {
        detailsState = when (fieldType) {
            CreateAccountDetailsViewModel.DetailsFieldType.FirstName -> detailsState.copy(firstName = newText)
            CreateAccountDetailsViewModel.DetailsFieldType.LastName -> detailsState.copy(lastName = newText)
            CreateAccountDetailsViewModel.DetailsFieldType.Password -> detailsState.copy(password = newText)
            CreateAccountDetailsViewModel.DetailsFieldType.ConfirmPassword -> detailsState.copy(confirmPassword = newText)
            CreateAccountDetailsViewModel.DetailsFieldType.TeamName -> detailsState.copy(teamName = newText)
        }.let {
            it.copy(
                error = CreateAccountDetailsViewState.DetailsError.None,
                continueEnabled = it.fieldsNotEmpty() && !it.loading
            )
        }
    }

    final override fun onDetailsErrorDismiss() {
        detailsState = detailsState.copy(error = CreateAccountDetailsViewState.DetailsError.None)
    }

    final override fun onDetailsContinue() {
        detailsState = detailsState.copy(loading = true, continueEnabled = false)
        viewModelScope.launch {
            val detailsError = when {
                !validatePasswordUseCase(detailsState.password.text) ->
                    CreateAccountDetailsViewState.DetailsError.TextFieldError.InvalidPasswordError
                detailsState.password.text != detailsState.confirmPassword.text ->
                    CreateAccountDetailsViewState.DetailsError.TextFieldError.PasswordsNotMatchingError
                else -> CreateAccountDetailsViewState.DetailsError.None
            }
            detailsState = detailsState.copy(
                loading = false,
                continueEnabled = true,
                error = detailsError
            )
            if (detailsState.error is CreateAccountDetailsViewState.DetailsError.None) onDetailsSuccess()
        }
    }

    abstract fun onDetailsSuccess()

    // Code
    final override fun onCodeChange(newValue: CodeFieldValue) {
        codeState = codeState.copy(code = newValue, error = CreateAccountCodeViewState.CodeError.None)
        if (newValue.isFullyFilled) onCodeContinue()
    }

    final override fun onCodeErrorDismiss() {
        codeState = codeState.copy(error = CreateAccountCodeViewState.CodeError.None)
    }

    final override fun resendCode() {
        codeState = codeState.copy(loading = true)
        viewModelScope.launch {
            val authScope = authScope().let {
                when (it) {
                    is AutoVersionAuthScopeUseCase.Result.Success -> it.authenticationScope

                    is AutoVersionAuthScopeUseCase.Result.Failure.UnknownServerVersion -> {
                        // TODO: show dialog
                        return@launch
                    }
                    is AutoVersionAuthScopeUseCase.Result.Failure.TooNewVersion -> {
                        // TODO: show dialog
                        return@launch
                    }
                    is AutoVersionAuthScopeUseCase.Result.Failure.Generic -> {
                        return@launch
                    }
                }
            }

            val codeError = authScope.registerScope.requestActivationCode(emailState.email.text.trim().lowercase()).toCodeError()
            codeState = codeState.copy(loading = false, error = codeError)
        }
    }

    @Suppress("ComplexMethod")
    private fun onCodeContinue() {
        codeState = codeState.copy(loading = true)
        viewModelScope.launch {
            val authScope = authScope().let {
                when (it) {
                    is AutoVersionAuthScopeUseCase.Result.Success -> it.authenticationScope

                    is AutoVersionAuthScopeUseCase.Result.Failure.UnknownServerVersion -> {
                        // TODO: show dialog
                        return@launch
                    }
                    is AutoVersionAuthScopeUseCase.Result.Failure.TooNewVersion -> {
                        // TODO: show dialog
                        return@launch
                    }
                    is AutoVersionAuthScopeUseCase.Result.Failure.Generic -> {
                        return@launch
                    }
                }
            }

            val registerParam = registerParamFromType()

            val registerResult = authScope.registerScope.register(registerParam).let {
                when (it) {
                    is RegisterResult.Failure -> {
                        updateCodeErrorState(it.toCodeError())
                        return@launch
                    }
                    is RegisterResult.Success -> it
                }
            }
            val storedUserId = addAuthenticatedUser(
                authTokens = registerResult.authData,
                ssoId = registerResult.ssoID,
                serverConfigId = registerResult.serverConfigId,
                proxyCredentials = registerResult.proxyCredentials,
                replace = false
            ).let {
                when (it) {
                    is AddAuthenticatedUserUseCase.Result.Failure -> {
                        updateCodeErrorState(it.toCodeError())
                        return@launch
                    }
                    is AddAuthenticatedUserUseCase.Result.Success -> it.userId
                }
            }
            registerClient(storedUserId, registerParam.password).let {
                when (it) {
                    is RegisterClientResult.Failure -> {
                        updateCodeErrorState(it.toCodeError())
                        return@launch
                    }
                    is RegisterClientResult.Success -> {
                        onCodeSuccess()
                    }
                }
            }
        }
    }

    private fun registerParamFromType() = when (type) {
        CreateAccountFlowType.CreatePersonalAccount ->
            RegisterParam.PrivateAccount(
                firstName = detailsState.firstName.text.trim(),
                lastName = detailsState.lastName.text.trim(),
                password = detailsState.password.text,
                email = emailState.email.text.trim().lowercase(),
                emailActivationCode = codeState.code.text.text
            )
        CreateAccountFlowType.CreateTeam ->
            RegisterParam.Team(
                firstName = detailsState.firstName.text.trim(),
                lastName = detailsState.lastName.text.trim(),
                password = detailsState.password.text,
                email = emailState.email.text.trim().lowercase(),
                emailActivationCode = codeState.code.text.text,
                teamName = detailsState.teamName.text.trim(),
                teamIcon = "default"
            )
    }

    private fun updateCodeErrorState(codeError: CreateAccountCodeViewState.CodeError) {
        codeState = if (codeError is CreateAccountCodeViewState.CodeError.None) {
            codeState.copy(error = codeError)

        } else {
            codeState.copy(loading = false, error = codeError)
        }
    }

    private suspend fun registerClient(userId: UserId, password: String) =
        clientScopeProviderFactory.create(userId).clientScope.getOrRegister(
            RegisterClientParam(
                password = password,
                capabilities = null
            )
        )

    abstract fun onCodeSuccess()
    final override fun onTooManyDevicesError() {
        codeState = codeState.copy(
            code = CodeFieldValue(text = TextFieldValue(""), isFullyFilled = false),
            error = CreateAccountCodeViewState.CodeError.None
        )
        viewModelScope.launch {
            navigationManager.navigate(NavigationCommand(NavigationItem.RemoveDevices.getRouteWithArgs(), BackStackMode.CLEAR_WHOLE))
        }
    }

    override fun dismissClientUpdateDialog() {
        emailState = emailState.copy(showClientUpdateDialog = false)
    }

    override fun dismissApiVersionNotSupportedDialog() {
        emailState = emailState.copy(showServerVersionNotSupportedDialog = false)
    }

    override fun updateTheApp() {
        // todo : update the app after releasing on the store
    }
}

private fun RequestActivationCodeResult.toEmailError() = when (this) {
    RequestActivationCodeResult.Failure.AlreadyInUse -> CreateAccountEmailViewState.EmailError.TextFieldError.AlreadyInUseError
    RequestActivationCodeResult.Failure.BlacklistedEmail -> CreateAccountEmailViewState.EmailError.TextFieldError.BlacklistedEmailError
    RequestActivationCodeResult.Failure.DomainBlocked -> CreateAccountEmailViewState.EmailError.TextFieldError.DomainBlockedError
    RequestActivationCodeResult.Failure.InvalidEmail -> CreateAccountEmailViewState.EmailError.TextFieldError.InvalidEmailError
    is RequestActivationCodeResult.Failure.Generic -> CreateAccountEmailViewState.EmailError.DialogError.GenericError(this.failure)
    RequestActivationCodeResult.Success -> CreateAccountEmailViewState.EmailError.None
}

private fun RequestActivationCodeResult.toCodeError() = when (this) {
    RequestActivationCodeResult.Failure.AlreadyInUse -> CreateAccountCodeViewState.CodeError.DialogError.AccountAlreadyExistsError
    RequestActivationCodeResult.Failure.BlacklistedEmail -> CreateAccountCodeViewState.CodeError.DialogError.BlackListedError
    RequestActivationCodeResult.Failure.DomainBlocked -> CreateAccountCodeViewState.CodeError.DialogError.EmailDomainBlockedError
    RequestActivationCodeResult.Failure.InvalidEmail -> CreateAccountCodeViewState.CodeError.DialogError.InvalidEmailError
    is RequestActivationCodeResult.Failure.Generic -> CreateAccountCodeViewState.CodeError.DialogError.GenericError(this.failure)
    RequestActivationCodeResult.Success -> CreateAccountCodeViewState.CodeError.None
}

private fun RegisterClientResult.Failure.toCodeError() = when (this) {
    is RegisterClientResult.Failure.TooManyClients -> CreateAccountCodeViewState.CodeError.TooManyDevicesError
    is RegisterClientResult.Failure.Generic -> CreateAccountCodeViewState.CodeError.DialogError.GenericError(this.genericFailure)
    is RegisterClientResult.Failure.InvalidCredentials ->
        throw WillNeverOccurError("RegisterClient: wrong password when register client after creating a new account")
    is RegisterClientResult.Failure.PasswordAuthRequired ->
        throw WillNeverOccurError("RegisterClient: password required to register client after creating new account with email")
}

private fun RegisterResult.Failure.toCodeError() = when (this) {
    RegisterResult.Failure.InvalidActivationCode -> CreateAccountCodeViewState.CodeError.TextFieldError.InvalidActivationCodeError
    RegisterResult.Failure.AccountAlreadyExists -> CreateAccountCodeViewState.CodeError.DialogError.AccountAlreadyExistsError
    RegisterResult.Failure.BlackListed -> CreateAccountCodeViewState.CodeError.DialogError.BlackListedError
    RegisterResult.Failure.EmailDomainBlocked -> CreateAccountCodeViewState.CodeError.DialogError.EmailDomainBlockedError
    RegisterResult.Failure.InvalidEmail -> CreateAccountCodeViewState.CodeError.DialogError.InvalidEmailError
    RegisterResult.Failure.TeamMembersLimitReached -> CreateAccountCodeViewState.CodeError.DialogError.TeamMembersLimitError
    RegisterResult.Failure.UserCreationRestricted -> CreateAccountCodeViewState.CodeError.DialogError.CreationRestrictedError
    is RegisterResult.Failure.Generic -> CreateAccountCodeViewState.CodeError.DialogError.GenericError(this.failure)
}

private fun AddAuthenticatedUserUseCase.Result.Failure.toCodeError() = when (this) {
    is AddAuthenticatedUserUseCase.Result.Failure.Generic ->
        CreateAccountCodeViewState.CodeError.DialogError.GenericError(this.genericFailure)
    AddAuthenticatedUserUseCase.Result.Failure.UserAlreadyExists -> CreateAccountCodeViewState.CodeError.DialogError.UserAlreadyExists
}
