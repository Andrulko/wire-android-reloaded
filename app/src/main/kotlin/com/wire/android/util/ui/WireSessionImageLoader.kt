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

package com.wire.android.util.ui

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import coil.Coil
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.wire.android.model.ImageAsset
import com.wire.kalium.logic.feature.asset.DeleteAssetUseCase
import com.wire.kalium.logic.feature.asset.GetAvatarAssetUseCase
import com.wire.kalium.logic.feature.asset.GetMessageAssetUseCase

/**
 * An ImageLoader that is able to load AssetIds supplied by Kalium.
 * As it uses Kalium's [GetAvatarAssetUseCase], a loader created for one session may be unable to load images from another session.
 * It wraps Coil, so it becomes easier to refactor in the future if we ever switch from Coil to something else.
 */
@Stable
class WireSessionImageLoader(private val coilImageLoader: ImageLoader) {
    private companion object {
        const val RETRY_INCREMENT_ATTEMPT_PER_STEP = 1
    }

    /**
     * Attempts to paint an Image using [asset], falling back to [fallbackData] if [asset] is null.
     * Just like [rememberAsyncImagePainter], [fallbackData] can be anything that [Coil] accepts.
     * Currently, Coil does not have a friendly API to retry a failing image request, so we have to do it ourselves.
     * adding retry_hash is a workaround to force Coil to retry the request.
     * see https://github.com/coil-kt/coil/issues/884
     */
    @Composable
    fun paint(
        asset: ImageAsset?,
        fallbackData: Any? = null
    ): Painter {
        var retryHash by remember { mutableStateOf(0) }

        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .memoryCacheKey(asset?.uniqueKey)
                .data(asset ?: fallbackData)
                .setParameter(
                    key = AssetImageFetcher.OPTION_PARAMETER_RETRY_KEY,
                    value = retryHash,
                    memoryCacheKey = null
                )
                .build(),
            imageLoader = coilImageLoader
        )

        if (painter.state is AsyncImagePainter.State.Error) {
            retryHash += RETRY_INCREMENT_ATTEMPT_PER_STEP
        }

        return painter
    }

    class Factory(
        val context: Context,
        private val getAvatarAsset: GetAvatarAssetUseCase,
        private val deleteAsset: DeleteAssetUseCase,
        private val getPrivateAsset: GetMessageAssetUseCase,
    ) {
        private val defaultImageLoader = Coil.imageLoader(context)
        private val resources = context.resources

        fun newImageLoader(): WireSessionImageLoader =
            WireSessionImageLoader(
                defaultImageLoader.newBuilder()
                    .components {
                        add(
                            AssetImageFetcher.Factory(
                                getPublicAssetUseCase = getAvatarAsset,
                                getPrivateAssetUseCase = getPrivateAsset,
                                deleteAssetUseCase = deleteAsset,
                                drawableResultWrapper = DrawableResultWrapper(resources),
                                context = context
                            )
                        )
                        if (SDK_INT >= 28) {
                            add(ImageDecoderDecoder.Factory())
                        } else {
                            add(GifDecoder.Factory())
                        }
                    }.build()
            )
    }
}
