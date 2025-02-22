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

package com.wire.android.ui.home.conversations

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wire.android.R
import com.wire.android.ui.common.WireDialog
import com.wire.android.ui.common.WireDialogButtonProperties
import com.wire.android.ui.common.WireDialogButtonType
import com.wire.android.util.permission.rememberWriteStorageRequestFlow
import okio.Path

@Composable
fun DownloadedAssetDialog(
    downloadedAssetDialogState: DownloadedAssetDialogVisibilityState,
    onSaveFileToExternalStorage: (String, Path, Long, String) -> Unit,
    onOpenFileWithExternalApp: (Path, String?) -> Unit,
    hideOnAssetDownloadedDialog: () -> Unit
) {
    if (downloadedAssetDialogState is DownloadedAssetDialogVisibilityState.Displayed) {
        val assetName = downloadedAssetDialogState.assetName
        val assetDataPath = downloadedAssetDialogState.assetDataPath
        val assetSize = downloadedAssetDialogState.assetSize
        val messageId = downloadedAssetDialogState.messageId

        val onSaveFileWriteStorageRequest = rememberWriteStorageRequestFlow(
            onGranted = { onSaveFileToExternalStorage(assetName, assetDataPath, assetSize, messageId) },
            onDenied = { /** TODO: Show a dialog rationale explaining why the permission is needed **/ }
        )

        WireDialog(
            title = assetName,
            text = stringResource(R.string.asset_download_dialog_text),
            buttonsHorizontalAlignment = false,
            onDismiss = { hideOnAssetDownloadedDialog() },
            optionButton2Properties = WireDialogButtonProperties(
                text = stringResource(R.string.asset_download_dialog_open_text),
                type = WireDialogButtonType.Primary,
                onClick = { onOpenFileWithExternalApp(assetDataPath, assetName) }
            ),
            optionButton1Properties = WireDialogButtonProperties(
                text = stringResource(R.string.asset_download_dialog_save_text),
                type = WireDialogButtonType.Primary,
                onClick = onSaveFileWriteStorageRequest::launch
            ),
            dismissButtonProperties = WireDialogButtonProperties(
                text = stringResource(R.string.label_cancel),
                onClick = { hideOnAssetDownloadedDialog() }
            ),
        )
    }
}
