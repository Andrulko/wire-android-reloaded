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

import android.content.res.Resources
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.wire.android.R
import com.wire.android.ui.common.button.WireSecondaryButton
import com.wire.android.ui.common.colorsScheme
import com.wire.android.ui.common.dimensions
import com.wire.android.ui.common.spacers.VerticalSpace
import com.wire.android.ui.home.conversations.model.UIMessageContent.SystemMessage
import com.wire.android.ui.theme.wireColorScheme
import com.wire.android.ui.theme.wireTypography
import com.wire.android.util.ui.UIText
import com.wire.android.util.ui.stringWithStyledArgs
import com.wire.android.util.ui.toUIText

@Composable
fun SystemMessageItem(message: SystemMessage) {
    val fullAvatarOuterPadding = dimensions().userAvatarClickablePadding + dimensions().userAvatarStatusBorderSize
    Row(
        Modifier
            .padding(
                end = dimensions().spacing16x,
                start = dimensions().spacing8x,
                top = fullAvatarOuterPadding,
                bottom = dimensions().messageItemBottomPadding - fullAvatarOuterPadding
            )
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .width(dimensions().userAvatarDefaultSize),
            contentAlignment = Alignment.TopEnd
        ) {
            if (message.iconResId != null) {
                Box(
                    modifier = Modifier.size(
                        width = dimensions().systemMessageIconLargeSize,
                        height = dimensions().spacing20x
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    val size =
                        if (message.isSmallIcon) dimensions().systemMessageIconSize
                        else dimensions().systemMessageIconLargeSize
                    Image(
                        painter = painterResource(id = message.iconResId),
                        contentDescription = null,
                        colorFilter = getColorFilter(message),
                        modifier = Modifier.size(size),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        Spacer(Modifier.padding(start = dimensions().spacing16x))
        Column {
            val context = LocalContext.current
            var expanded: Boolean by remember { mutableStateOf(false) }
            Text(
                modifier = Modifier
                    .defaultMinSize(minHeight = dimensions().spacing20x)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                style = MaterialTheme.wireTypography.body01,
                lineHeight = MaterialTheme.wireTypography.body02.lineHeight,
                text = message.annotatedString(
                    res = context.resources,
                    expanded = expanded,
                    normalStyle = MaterialTheme.wireTypography.body01,
                    boldStyle = MaterialTheme.wireTypography.body02,
                    normalColor = MaterialTheme.wireColorScheme.secondaryText,
                    boldColor = MaterialTheme.wireColorScheme.onBackground
                )
            )
            if (message is SystemMessage.Knock) {
                VerticalSpace.x8()
            }
            if (message.expandable) {
                WireSecondaryButton(
                    onClick = { expanded = !expanded },
                    text = stringResource(if (expanded) R.string.label_show_less else R.string.label_show_all),
                    fillMaxWidth = false,
                    minHeight = dimensions().spacing32x,
                    minWidth = dimensions().spacing40x,
                    shape = RoundedCornerShape(size = dimensions().corner12x),
                    contentPadding = PaddingValues(horizontal = dimensions().spacing12x, vertical = dimensions().spacing8x),
                    modifier = Modifier
                        .padding(top = dimensions().spacing4x)
                        .height(height = dimensions().spacing32x)
                )
            }
        }
    }
}

@Composable
private fun getColorFilter(message: SystemMessage): ColorFilter? {
    return when (message) {
        is SystemMessage.MissedCall.OtherCalled -> null
        is SystemMessage.MissedCall.YouCalled -> null
        is SystemMessage.Knock -> ColorFilter.tint(colorsScheme().primary)
        is SystemMessage.MemberAdded -> ColorFilter.tint(colorsScheme().onBackground)
        is SystemMessage.MemberJoined -> ColorFilter.tint(colorsScheme().onBackground)
        is SystemMessage.MemberLeft -> ColorFilter.tint(colorsScheme().onBackground)
        is SystemMessage.MemberRemoved -> ColorFilter.tint(colorsScheme().onBackground)
        is SystemMessage.CryptoSessionReset -> ColorFilter.tint(colorsScheme().onBackground)
        is SystemMessage.RenamedConversation -> ColorFilter.tint(colorsScheme().onBackground)
        is SystemMessage.TeamMemberRemoved -> ColorFilter.tint(colorsScheme().onBackground)
        is SystemMessage.ConversationReceiptModeChanged -> ColorFilter.tint(colorsScheme().onBackground)
        is SystemMessage.HistoryLost -> ColorFilter.tint(colorsScheme().onBackground)
        is SystemMessage.NewConversationReceiptMode -> ColorFilter.tint(colorsScheme().onBackground)
    }
}

@Preview
@Composable
fun PreviewSystemMessageAdded7Users() {
    SystemMessageItem(
        message = SystemMessage.MemberAdded(
            "Barbara Cotolina".toUIText(),
            listOf(
                "Albert Lewis".toUIText(),
                "Bert Strunk".toUIText(),
                "Claudia Schiffer".toUIText(),
                "Dorothee Friedrich".toUIText(),
                "Erich Weinert".toUIText(),
                "Frieda Kahlo".toUIText(),
                "Gudrun Gut".toUIText()
            )
        )
    )
}

@Preview
@Composable
fun PreviewSystemMessageAdded4Users() {
    SystemMessageItem(
        message = SystemMessage.MemberAdded(
            "Barbara Cotolina".toUIText(),
            listOf("Albert Lewis".toUIText(), "Bert Strunk".toUIText(), "Claudia Schiffer".toUIText(), "Dorothee Friedrich".toUIText())
        )
    )
}

@Preview
@Composable
fun PreviewSystemMessageRemoved4Users() {
    SystemMessageItem(
        message = SystemMessage.MemberRemoved(
            "Barbara Cotolina".toUIText(),
            listOf("Albert Lewis".toUIText(), "Bert Strunk".toUIText(), "Claudia Schiffer".toUIText(), "Dorothee Friedrich".toUIText())
        )
    )
}

@Preview
@Composable
fun PreviewSystemMessageLeft() {
    SystemMessageItem(message = SystemMessage.MemberLeft(UIText.DynamicString("Barbara Cotolina")))
}

@Preview
@Composable
fun PreviewSystemMessageMissedCall() {
    SystemMessageItem(message = SystemMessage.MissedCall.OtherCalled(UIText.DynamicString("Barbara Cotolina")))
}

@Preview
@Composable
fun PreviewSystemMessageKnock() {
    SystemMessageItem(message = SystemMessage.Knock(UIText.DynamicString("Barbara Cotolina")))
}

private val SystemMessage.expandable
    get() = when (this) {
        is SystemMessage.MemberAdded -> this.memberNames.size > EXPANDABLE_THRESHOLD
        is SystemMessage.MemberRemoved -> this.memberNames.size > EXPANDABLE_THRESHOLD
        is SystemMessage.MemberJoined -> false
        is SystemMessage.MemberLeft -> false
        is SystemMessage.MissedCall -> false
        is SystemMessage.RenamedConversation -> false
        is SystemMessage.TeamMemberRemoved -> false
        is SystemMessage.CryptoSessionReset -> false
        is SystemMessage.NewConversationReceiptMode -> false
        is SystemMessage.ConversationReceiptModeChanged -> false
        is SystemMessage.Knock -> false
        is SystemMessage.HistoryLost -> false
    }

private fun List<String>.toUserNamesListString(res: Resources) = when {
    this.isEmpty() -> ""
    this.size == 1 -> this[0]
    else -> res.getString(R.string.label_system_message_and, this.dropLast(1).joinToString(", "), this.last())
}

private fun List<UIText>.limitUserNamesList(res: Resources, threshold: Int): List<String> =
    if (this.size <= threshold) this.map { it.asString(res) }
    else {
        val moreCount = this.size - (threshold - 1) // the last visible place is taken by "and X more"
        this.take(threshold - 1)
            .map { it.asString(res) }
            .plus(res.getQuantityString(R.plurals.label_system_message_x_more, moreCount, moreCount))
    }

@Suppress("LongParameterList", "SpreadOperator", "ComplexMethod")
fun SystemMessage.annotatedString(
    res: Resources,
    expanded: Boolean,
    normalStyle: TextStyle,
    boldStyle: TextStyle,
    normalColor: Color,
    boldColor: Color
): AnnotatedString {
    val args = when (this) {
        is SystemMessage.MemberAdded ->
            arrayOf(
                author.asString(res),
                memberNames.limitUserNamesList(res, if (expanded) memberNames.size else EXPANDABLE_THRESHOLD).toUserNamesListString(res)
            )
        is SystemMessage.MemberRemoved ->
            arrayOf(
                author.asString(res),
                memberNames.limitUserNamesList(res, if (expanded) memberNames.size else EXPANDABLE_THRESHOLD).toUserNamesListString(res)
            )
        is SystemMessage.MemberJoined -> arrayOf(author.asString(res))
        is SystemMessage.MemberLeft -> arrayOf(author.asString(res))
        is SystemMessage.MissedCall -> arrayOf(author.asString(res))
        is SystemMessage.RenamedConversation -> arrayOf(author.asString(res), additionalContent)
        is SystemMessage.TeamMemberRemoved -> arrayOf(content.userName)
        is SystemMessage.CryptoSessionReset -> arrayOf(author.asString(res))
        is SystemMessage.NewConversationReceiptMode -> arrayOf(receiptMode.asString(res))
        is SystemMessage.ConversationReceiptModeChanged -> arrayOf(author.asString(res), receiptMode.asString(res))
        is SystemMessage.Knock -> arrayOf(author.asString(res))
        is SystemMessage.HistoryLost -> arrayOf()
    }
    return res.stringWithStyledArgs(stringResId, normalStyle, boldStyle, normalColor, boldColor, *args)
}

private const val EXPANDABLE_THRESHOLD = 4
