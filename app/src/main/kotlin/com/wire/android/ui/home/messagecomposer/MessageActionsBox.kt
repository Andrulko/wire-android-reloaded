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

package com.wire.android.ui.home.messagecomposer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Transition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wire.android.R
import com.wire.android.ui.common.button.WireButtonState
import com.wire.android.ui.common.button.WireSecondaryIconButton
import com.wire.android.ui.common.dimensions
import com.wire.android.ui.theme.wireColorScheme
import com.wire.android.util.debug.LocalFeatureVisibilityFlags

@ExperimentalAnimationApi
@Composable
fun MessageComposeActionsBox(
    transition: Transition<MessageComposeInputState>,
    isMentionActive: Boolean,
    isFileSharingEnabled: Boolean,
    startMention: () -> Unit,
    onAdditionalOptionButtonClicked: () -> Unit,
    onPingClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.wrapContentSize()) {
        Divider(color = MaterialTheme.wireColorScheme.outline)
        Box(Modifier.wrapContentSize()) {
            transition.AnimatedContent(
                contentKey = { state -> state is MessageComposeInputState.Active },
                transitionSpec = {
                    slideInVertically { fullHeight -> fullHeight / 2 } + fadeIn() with
                            slideOutVertically { fullHeight -> fullHeight / 2 } + fadeOut()
                }
            ) { state ->
                if (state is MessageComposeInputState.Active) {
                    MessageComposeActions(
                        state.attachmentOptionsDisplayed,
                        isMentionActive,
                        state.isEditMessage,
                        isFileSharingEnabled,
                        startMention,
                        onAdditionalOptionButtonClicked,
                        onPingClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageComposeActions(
    attachmentOptionsDisplayed: Boolean,
    isMentionsSelected: Boolean,
    isEditMessage: Boolean,
    isFileSharingEnabled: Boolean = true,
    startMention: () -> Unit,
    onAdditionalOptionButtonClicked: () -> Unit,
    onPingClicked: () -> Unit
) {
    val localFeatureVisibilityFlags = LocalFeatureVisibilityFlags.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions().spacing56x)
    ) {
        with(localFeatureVisibilityFlags) {
            if (!isEditMessage) AdditionalOptionButton(attachmentOptionsDisplayed, isFileSharingEnabled, onAdditionalOptionButtonClicked)
            if (RichTextIcon) RichTextEditingAction()
            if (!isEditMessage && EmojiIcon) AddEmojiAction()
            if (!isEditMessage && GifIcon) AddGifAction()
            AddMentionAction(isMentionsSelected, startMention)
            if (!isEditMessage && PingIcon) PingAction(onPingClicked = onPingClicked)
        }
    }
}

@Composable
private fun RichTextEditingAction() {
    WireSecondaryIconButton(
        onButtonClicked = {},
        blockUntilSynced = true,
        iconResource = R.drawable.ic_rich_text,
        contentDescription = R.string.content_description_conversation_enable_rich_text_mode
    )
}

@Composable
private fun AddEmojiAction() {
    WireSecondaryIconButton(
        onButtonClicked = {},
        blockUntilSynced = true,
        iconResource = R.drawable.ic_emoticon,
        contentDescription = R.string.content_description_conversation_send_emoticon
    )
}

@Composable
private fun AddGifAction() {
    WireSecondaryIconButton(
        onButtonClicked = {},
        blockUntilSynced = true,
        iconResource = R.drawable.ic_gif,
        contentDescription = R.string.content_description_conversation_send_gif
    )
}

@Composable
private fun AddMentionAction(isSelected: Boolean, addMentionAction: () -> Unit) {
    val onButtonClicked = remember(isSelected) {
        { if (!isSelected) addMentionAction() }
    }
    WireSecondaryIconButton(
        onButtonClicked = onButtonClicked,
        blockUntilSynced = true,
        iconResource = R.drawable.ic_mention,
        contentDescription = R.string.content_description_conversation_mention_someone,
        state = if (isSelected) WireButtonState.Selected else WireButtonState.Default
    )
}

@Composable
private fun PingAction(onPingClicked: () -> Unit) {
    WireSecondaryIconButton(
        onButtonClicked = onPingClicked,
        blockUntilSynced = true,
        iconResource = R.drawable.ic_ping,
        contentDescription = R.string.content_description_ping_everyone
    )
}

@Preview
@Composable
fun PreviewMessageActionsBox() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensions().spacing56x)
    ) {
        AdditionalOptionButton(isSelected = false, isEnabled = true, onClick = {})
        RichTextEditingAction()
        AddEmojiAction()
        AddGifAction()
        AddMentionAction(isSelected = false, addMentionAction = {})
        PingAction {}
    }
}
