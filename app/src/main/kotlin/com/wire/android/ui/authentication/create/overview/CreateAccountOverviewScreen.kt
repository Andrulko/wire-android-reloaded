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

package com.wire.android.ui.authentication.create.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wire.android.R
import com.wire.android.ui.authentication.ServerTitle
import com.wire.android.ui.common.button.WirePrimaryButton
import com.wire.android.ui.common.topappbar.WireCenterAlignedTopAppBar
import com.wire.android.ui.theme.wireDimensions
import com.wire.android.ui.theme.wireTypography
import com.wire.android.util.CustomTabsHelper
import com.wire.kalium.logic.configuration.server.ServerConfig

@Composable
fun CreateAccountOverviewScreen(viewModel: CreateAccountOverviewViewModel, serverConfig: ServerConfig.Links) {
    OverviewContent(
        onBackPressed = viewModel::goBackToPreviousStep,
        onContinuePressed = viewModel::onOverviewContinue,
        serverConfig = serverConfig,
        overviewParams = CreateAccountOverviewParams(
            title = stringResource(id = viewModel.type.titleResId),
            contentTitle = viewModel.type.overviewResources.overviewContentTitleResId?.let { stringResource(id = it) } ?: "",
            contentText = stringResource(id = viewModel.type.overviewResources.overviewContentTextResId),
            contentIconResId = viewModel.type.overviewResources.overviewContentIconResId,
            learnMoreText = stringResource(id = viewModel.type.overviewResources.overviewLearnMoreTextResId),
            learnMoreUrl = viewModel.learnMoreUrl()
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverviewContent(
    onBackPressed: () -> Unit,
    onContinuePressed: () -> Unit,
    overviewParams: CreateAccountOverviewParams,
    serverConfig: ServerConfig.Links
) {
    Scaffold(
        topBar = {
            WireCenterAlignedTopAppBar(
                elevation = 0.dp,
                title = overviewParams.title,
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
        },
    ) { internalPadding ->
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(internalPadding)) {
            val context = LocalContext.current
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = overviewParams.contentIconResId),
                contentDescription = "",
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .padding(
                        horizontal = MaterialTheme.wireDimensions.spacing64x,
                        vertical = MaterialTheme.wireDimensions.spacing32x
                    )
            )
            OverviewTexts(
                modifier = Modifier.padding(horizontal = MaterialTheme.wireDimensions.spacing24x),
                onLearnMoreClick = { CustomTabsHelper.launchUrl(context, overviewParams.learnMoreUrl) },
                overviewParams = overviewParams,
            )
            Spacer(modifier = Modifier.weight(1f))
            WirePrimaryButton(
                text = stringResource(R.string.label_continue),
                onClick = onContinuePressed,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.wireDimensions.spacing16x),
            )
        }
    }
}

@Composable
private fun OverviewTexts(overviewParams: CreateAccountOverviewParams, modifier: Modifier, onLearnMoreClick: () -> Unit) {
    Column(modifier = modifier) {
        if (overviewParams.contentTitle.isNotEmpty())
            Text(
                text = overviewParams.contentTitle,
                style = MaterialTheme.wireTypography.title01,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.wireDimensions.spacing8x)
            )
        Text(
            text = overviewParams.contentText,
            style = MaterialTheme.wireTypography.body02,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = overviewParams.learnMoreText,
            style = MaterialTheme.wireTypography.body02.copy(
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onLearnMoreClick
                )
        )
    }
}

@Composable
@Preview
fun PreviewCreateAccountOverviewScreen() {
    OverviewContent(
        onBackPressed = { },
        onContinuePressed = { },
        overviewParams = CreateAccountOverviewParams(
            title = "title",
            contentTitle = "contentTitle",
            contentText = "contentText",
            contentIconResId = R.drawable.ic_create_personal_account,
            learnMoreText = "learn more",
            learnMoreUrl = ""
        ),
        ServerConfig.DEFAULT
    )
}
