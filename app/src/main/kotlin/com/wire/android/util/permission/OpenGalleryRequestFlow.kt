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

package com.wire.android.util.permission

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Flow that will launch gallery browser to select a picture.
 * This will handle the permissions request in case there is no permission granted for the storage.
 *
 * @param onGalleryItemPicked action that will be executed when selecting a media result from [ActivityResultContract]
 * @param onPermissionDenied action to be executed when the permissions is denied
 */
@Composable
fun rememberOpenGalleryFlow(
    onGalleryItemPicked: (Uri) -> Unit,
    onPermissionDenied: () -> Unit
): UseStorageRequestFlow {
    val context = LocalContext.current
    val openGalleryLauncher: ManagedActivityResultLauncher<String, Uri?> = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { onChosenPictureUri ->
        onChosenPictureUri?.let { onGalleryItemPicked(it) }
    }

    val requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean> =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGalleryLauncher.launch(MIME_TYPE)
            } else {
                onPermissionDenied()
            }
        }

    return remember {
        UseStorageRequestFlow(MIME_TYPE, context, openGalleryLauncher, requestPermissionLauncher)
    }
}

private const val MIME_TYPE = "image/*"
