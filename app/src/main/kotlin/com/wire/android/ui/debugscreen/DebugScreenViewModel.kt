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

package com.wire.android.ui.debugscreen

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.platformLogWriter
import com.wire.android.datastore.GlobalDataStore
import com.wire.android.di.CurrentAccount
import com.wire.android.migration.failure.UserMigrationStatus
import com.wire.android.navigation.BackStackMode
import com.wire.android.navigation.NavigationCommand
import com.wire.android.navigation.NavigationItem
import com.wire.android.navigation.NavigationManager
import com.wire.android.util.DataDogLogger
import com.wire.android.util.EMPTY
import com.wire.android.util.LogFileWriter
import com.wire.kalium.logger.KaliumLogLevel
import com.wire.kalium.logic.CoreLogger
import com.wire.kalium.logic.data.user.UserId
import com.wire.kalium.logic.feature.client.ObserveCurrentClientIdUseCase
import com.wire.kalium.logic.feature.keypackage.MLSKeyPackageCountResult
import com.wire.kalium.logic.feature.keypackage.MLSKeyPackageCountUseCase
import com.wire.kalium.logic.sync.incremental.RestartSlowSyncProcessForRecoveryUseCase
import com.wire.kalium.logic.sync.periodic.UpdateApiVersionsScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DebugScreenState(
    val isLoggingEnabled: Boolean = false,
    val isEncryptedProteusStorageEnabled: Boolean = false,
    val currentClientId: String = String.EMPTY,
    val keyPackagesCount: Int = 0,
    val mslClientId: String = String.EMPTY,
    val mlsErrorMessage: String = String.EMPTY,
    val isManualMigrationAllowed: Boolean = false
)

@Suppress("LongParameterList")
@HiltViewModel
class DebugScreenViewModel
@Inject constructor(
    @ApplicationContext private val context: Context,
    @CurrentAccount val currentAccount: UserId,
    private val navigationManager: NavigationManager,
    private val mlsKeyPackageCountUseCase: MLSKeyPackageCountUseCase,
    private val logFileWriter: LogFileWriter,
    private val currentClientIdUseCase: ObserveCurrentClientIdUseCase,
    private val updateApiVersions: UpdateApiVersionsScheduler,
    private val globalDataStore: GlobalDataStore,
    private val restartSlowSyncProcessForRecovery: RestartSlowSyncProcessForRecoveryUseCase,
) : ViewModel() {
    val logPath: String = logFileWriter.activeLoggingFile.absolutePath

    var state by mutableStateOf(
        DebugScreenState()
    )

    init {
        observeLoggingState()
        observeEncryptedProteusStorageState()
        observeMlsMetadata()
        observeCurrentClientId()
        checkIfCanTriggerManualMigration()
    }

    // if status is NoNeed, it means that the user has already been migrated in and older app version
    // or it is a new install
    // this is why we check the existence of the database file
    private fun checkIfCanTriggerManualMigration() {
        viewModelScope.launch {
            globalDataStore.getUserMigrationStatus(currentAccount.value).first().let { migrationStatus ->
                if (migrationStatus != UserMigrationStatus.NoNeed) {
                    context.getDatabasePath(currentAccount.value).let {
                        state = state.copy(isManualMigrationAllowed = (it.exists() && it.isFile))
                    }
                }
            }
        }
    }

    private fun observeLoggingState() {
        viewModelScope.launch {
            globalDataStore.isLoggingEnabled().collect {
                state = state.copy(isLoggingEnabled = it)
            }
        }
    }

    private fun observeEncryptedProteusStorageState() {
        viewModelScope.launch {
            globalDataStore.isEncryptedProteusStorageEnabled().collect {
                state = state.copy(isEncryptedProteusStorageEnabled = it)
            }
        }
    }

    private fun observeCurrentClientId() {
        viewModelScope.launch {
            currentClientIdUseCase().collect {
                val clientId = it?.let { clientId -> clientId.value } ?: "Client not fount"
                state = state.copy(currentClientId = clientId)
            }
        }
    }

    private fun observeMlsMetadata() {
        viewModelScope.launch {
            mlsKeyPackageCountUseCase().let {
                when (it) {
                    is MLSKeyPackageCountResult.Success -> {
                        state = state.copy(
                            keyPackagesCount = it.count,
                            mslClientId = it.clientId.value
                        )
                    }

                    is MLSKeyPackageCountResult.Failure.NetworkCallFailure -> {
                        state = state.copy(mlsErrorMessage = "Network Error!")
                    }

                    is MLSKeyPackageCountResult.Failure.FetchClientIdFailure -> {
                        state = state.copy(mlsErrorMessage = "ClientId Fetch Error!")
                    }

                    is MLSKeyPackageCountResult.Failure.Generic -> {}
                }
            }
        }
    }

    fun deleteLogs() {
        logFileWriter.deleteAllLogFiles()
    }

    fun restartSlowSyncForRecovery() {
        viewModelScope.launch {
            restartSlowSyncProcessForRecovery()
        }
    }

    fun setLoggingEnabledState(isEnabled: Boolean) {
        viewModelScope.launch {
            globalDataStore.setLoggingEnabled(isEnabled)
        }
        if (isEnabled) {
            logFileWriter.start()
            CoreLogger.setLoggingLevel(level = KaliumLogLevel.VERBOSE, logWriters = arrayOf(DataDogLogger, platformLogWriter()))
        } else {
            logFileWriter.stop()
            CoreLogger.setLoggingLevel(level = KaliumLogLevel.DISABLED, logWriters = arrayOf(DataDogLogger, platformLogWriter()))
        }
    }

    fun enableEncryptedProteusStorage() {
        viewModelScope.launch {
            globalDataStore.setEncryptedProteusStorageEnabled(true)
        }
    }

    fun onStartManualMigration() {
        viewModelScope.launch {
            navigationManager.navigate(
                NavigationCommand(
                    NavigationItem.Migration.getRouteWithArgs(listOf(currentAccount)),
                    BackStackMode.CLEAR_WHOLE
                )
            )
        }
    }

    fun forceUpdateApiVersions() {
        updateApiVersions.scheduleImmediateApiVersionUpdate()
    }

    fun navigateBack() = viewModelScope.launch { navigationManager.navigateBack() }
}
