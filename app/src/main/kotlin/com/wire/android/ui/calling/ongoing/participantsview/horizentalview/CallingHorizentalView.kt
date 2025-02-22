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

package com.wire.android.ui.calling.ongoing.participantsview.horizentalview

import android.view.View
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wire.android.ui.calling.ConversationName
import com.wire.android.ui.calling.getConversationName
import com.wire.android.ui.calling.model.UICallParticipant
import com.wire.android.ui.calling.ongoing.participantsview.ParticipantTile
import com.wire.android.ui.common.dimensions
import com.wire.android.ui.theme.wireDimensions

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OneOnOneCallView(
    participants: List<UICallParticipant>,
    pageIndex: Int,
    isSelfUserMuted: Boolean,
    isSelfUserCameraOn: Boolean,
    onSelfVideoPreviewCreated: (view: View) -> Unit,
    onSelfClearVideoPreview: () -> Unit
) {
    val config = LocalConfiguration.current

    LazyColumn(
        modifier = Modifier.padding(dimensions().spacing4x),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.wireDimensions.spacing2x)
    ) {
        items(items = participants, key = { it.id.toString() + it.clientId }) { participant ->
            // since we are getting participants by chunk of 8 items,
            // we need to check that we are on first page for self user
            val isSelfUser = pageIndex == 0 && participants.first() == participant

            val isCameraOn = if (isSelfUser)
                isSelfUserCameraOn else participant.isCameraOn
            val isMuted = if (isSelfUser)
                isSelfUserMuted else participant.isMuted

            val username = when (val conversationName = getConversationName(participant.name)) {
                is ConversationName.Known -> conversationName.name
                is ConversationName.Unknown -> stringResource(id = conversationName.resourceId)
            }

            val participantState = UICallParticipant(
                id = participant.id,
                clientId = participant.clientId,
                name = username,
                isMuted = isMuted,
                isSpeaking = participant.isSpeaking,
                isCameraOn = isCameraOn,
                isSharingScreen = participant.isSharingScreen,
                avatar = participant.avatar,
                membership = participant.membership
            )
            val maxHeight = (config.screenHeightDp - TOP_APP_BAR_AND_BOTTOM_SHEET_HEIGHT) / participants.size
            ParticipantTile(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxHeight.dp)
                    .animateItemPlacement(tween(durationMillis = 200)),
                participantTitleState = participantState,
                isSelfUser = isSelfUser,
                onSelfUserVideoPreviewCreated = {
                    if (isSelfUser) onSelfVideoPreviewCreated(it)
                },
                onClearSelfUserVideoPreview = {
                    if (isSelfUser)
                        onSelfClearVideoPreview()
                }
            )
        }
    }

}

private const val TOP_APP_BAR_AND_BOTTOM_SHEET_HEIGHT = 170
