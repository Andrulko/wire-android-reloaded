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

package com.wire.android.notification

import androidx.annotation.StringRes
import com.wire.android.R
import com.wire.kalium.logic.data.notification.LocalNotificationCommentType
import com.wire.kalium.logic.data.notification.LocalNotificationConversation
import com.wire.kalium.logic.data.notification.LocalNotificationMessage

data class NotificationConversation(
    val id: String,
    val name: String,
    val image: ByteArray?,
    val messages: List<NotificationMessage>,
    val isOneToOneConversation: Boolean,
    val lastMessageTime: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotificationConversation

        if (id != other.id) return false
        if (name != other.name) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false
        if (messages != other.messages) return false
        if (isOneToOneConversation != other.isOneToOneConversation) return false
        if (lastMessageTime != other.lastMessageTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        result = 31 * result + messages.hashCode()
        result = 31 * result + isOneToOneConversation.hashCode()
        result = 31 * result + lastMessageTime.hashCode()
        return result
    }
}

sealed class NotificationMessage(open val author: NotificationMessageAuthor, open val time: Long) {
    data class Text(
        override val author: NotificationMessageAuthor,
        override val time: Long,
        val text: String,
        val isQuotingSelfUser: Boolean
    ) :
        NotificationMessage(author, time)

    // shared file, picture, reaction
    data class Comment(override val author: NotificationMessageAuthor, override val time: Long, val textResId: CommentResId) :
        NotificationMessage(author, time)

    data class Knock(override val author: NotificationMessageAuthor, override val time: Long) :
        NotificationMessage(author, time)

    data class ConnectionRequest(override val author: NotificationMessageAuthor, override val time: Long, val authorId: String) :
        NotificationMessage(author, time)

    data class ConversationDeleted(override val author: NotificationMessageAuthor, override val time: Long) :
        NotificationMessage(author, time)
}

data class NotificationMessageAuthor(val name: String, val image: ByteArray?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotificationMessageAuthor

        if (name != other.name) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }
}

enum class CommentResId(@StringRes val value: Int) {
    PICTURE(R.string.notification_shared_picture),
    FILE(R.string.notification_shared_file),
    REACTION(R.string.notification_reacted),
    MISSED_CALL(R.string.notification_missed_call),
    NOT_SUPPORTED(R.string.notification_not_supported_issue),
    KNOCK(R.string.notification_knock),
}

fun LocalNotificationConversation.intoNotificationConversation(): NotificationConversation {

    val notificationMessages = this.messages.map { it.intoNotificationMessage() }.sortedBy { it.time }
    val lastMessageTime = this.messages.maxOfOrNull { it.time.toEpochMilliseconds() } ?: 0

    return NotificationConversation(
        id = id.toString(),
        name = conversationName,
        image = null, // TODO
        messages = notificationMessages,
        isOneToOneConversation = isOneToOneConversation,
        lastMessageTime = lastMessageTime
    )
}

fun LocalNotificationMessage.intoNotificationMessage(): NotificationMessage {

    val notificationMessageAuthor = NotificationMessageAuthor(author.name, null) // TODO image
    val notificationMessageTime = time.toEpochMilliseconds()

    return when (this) {
        is LocalNotificationMessage.Text -> NotificationMessage.Text(
            author = notificationMessageAuthor,
            time = notificationMessageTime,
            text = text,
            isQuotingSelfUser = isQuotingSelfUser
        )

        is LocalNotificationMessage.Comment -> NotificationMessage.Comment(
            notificationMessageAuthor,
            notificationMessageTime,
            type.intoCommentResId()
        )

        is LocalNotificationMessage.ConnectionRequest -> NotificationMessage.ConnectionRequest(
            notificationMessageAuthor,
            notificationMessageTime,
            this.authorId.toString()
        )

        is LocalNotificationMessage.ConversationDeleted -> {
            NotificationMessage.ConversationDeleted(
                notificationMessageAuthor,
                notificationMessageTime
            )
        }

        is LocalNotificationMessage.Knock -> {
            NotificationMessage.Knock(
                notificationMessageAuthor,
                notificationMessageTime
            )
        }
    }
}

fun LocalNotificationCommentType.intoCommentResId(): CommentResId =
    when (this) {
        LocalNotificationCommentType.PICTURE -> CommentResId.PICTURE
        LocalNotificationCommentType.FILE -> CommentResId.FILE
        LocalNotificationCommentType.REACTION -> CommentResId.REACTION
        LocalNotificationCommentType.MISSED_CALL -> CommentResId.MISSED_CALL
        LocalNotificationCommentType.NOT_SUPPORTED_YET -> CommentResId.NOT_SUPPORTED
    }
