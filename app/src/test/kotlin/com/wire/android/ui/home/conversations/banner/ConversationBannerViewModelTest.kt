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

package com.wire.android.ui.home.conversations.banner

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.wire.android.config.CoroutineTestExtension
import com.wire.android.config.mockUri
import com.wire.android.framework.TestConversationDetails
import com.wire.android.navigation.EXTRA_CONVERSATION_ID
import com.wire.android.ui.home.conversations.banner.usecase.ObserveConversationMembersByTypesUseCase
import com.wire.kalium.logic.data.id.ConversationId
import com.wire.kalium.logic.data.id.QualifiedID
import com.wire.kalium.logic.data.id.QualifiedIdMapper
import com.wire.kalium.logic.data.user.type.UserType
import com.wire.kalium.logic.feature.conversation.ObserveConversationDetailsUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.internal.assertEquals
import org.amshove.kluent.internal.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutineTestExtension::class)
class ConversationBannerViewModelTest {

    @Test
    fun `given a group members, when at least one is not internal team member, then banner should not be null`() = runTest {
        // Given
        val qualifiedId = ConversationId("some-dummy-value", "some.dummy.domain")

        val (arrangement, viewModel) = Arrangement()
            .withConversationParticipantsUserTypesUpdate(listOf(UserType.EXTERNAL))
            .withGroupConversation()
            .arrange()
        // When
        arrangement.observeConversationMembersByTypesUseCase(qualifiedId).test {
            awaitItem()
            assertNotEquals(null, viewModel.bannerState)
            awaitComplete()
        }
    }

    @Test
    fun `given a one to one conversation, when other user is not internal member, then banner should be null`() = runTest {
        // Given
        val qualifiedId = ConversationId("some-dummy-value", "some.dummy.domain")

        val (arrangement, viewModel) = Arrangement()
            .withOneOnOneConversation()
            .arrange()
        // When
        arrangement.observeConversationMembersByTypesUseCase(qualifiedId).test {
            awaitComplete()
            assertEquals(null, viewModel.bannerState)
        }
    }

    @Test
    fun `given a group members, when all of them are internal team members, then banner should be null`() = runTest {
        // Given
        val qualifiedId = ConversationId("some-dummy-value", "some.dummy.domain")

        val (arrangement, viewModel) = Arrangement()
            .withConversationParticipantsUserTypesUpdate(listOf(UserType.INTERNAL, UserType.INTERNAL))
            .withGroupConversation()
            .arrange()
        // When
        arrangement.observeConversationMembersByTypesUseCase(qualifiedId).test {
            awaitItem()
            assertEquals(null, viewModel.bannerState)
            awaitComplete()
        }
    }
}

private class Arrangement {

    @MockK
    private lateinit var savedStateHandle: SavedStateHandle

    @MockK
    private lateinit var qualifiedIdMapper: QualifiedIdMapper

    @MockK
    lateinit var observeConversationMembersByTypesUseCase: ObserveConversationMembersByTypesUseCase

    @MockK
    lateinit var observeConversationDetailsUseCase: ObserveConversationDetailsUseCase

    private val viewModel by lazy {
        ConversationBannerViewModel(
            qualifiedIdMapper,
            savedStateHandle,
            observeConversationMembersByTypesUseCase,
            observeConversationDetailsUseCase,
        )
    }
    val conversationId = "some-dummy-value@some.dummy.domain"

    init {
        // Tests setup
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockUri()
        every {
            qualifiedIdMapper.fromStringToQualifiedID("some-dummy-value@some.dummy.domain")
        } returns QualifiedID("some-dummy-value", "some.dummy.domain")
        every { savedStateHandle.get<String>(EXTRA_CONVERSATION_ID) } returns conversationId
        // Default empty values
        coEvery { observeConversationMembersByTypesUseCase(any()) } returns flowOf()
    }

    suspend fun withConversationParticipantsUserTypesUpdate(participants: List<UserType>) = apply {
        coEvery { observeConversationMembersByTypesUseCase(any()) } returns flowOf(participants.toSet())
    }

    suspend fun withGroupConversation() = apply {
        coEvery { observeConversationDetailsUseCase(any()) }
            .returns(flowOf(ObserveConversationDetailsUseCase.Result.Success(TestConversationDetails.GROUP)))
    }

    suspend fun withOneOnOneConversation() = apply {
        coEvery { observeConversationDetailsUseCase(any()) }
            .returns(flowOf(ObserveConversationDetailsUseCase.Result.Success(TestConversationDetails.CONVERSATION_ONE_ONE)))
    }

    fun arrange() = this to viewModel
}
