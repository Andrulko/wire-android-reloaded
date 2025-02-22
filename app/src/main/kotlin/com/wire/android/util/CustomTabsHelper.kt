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

package com.wire.android.util

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.wire.android.R

object CustomTabsHelper {

    @JvmStatic
    fun launchUrl(context: Context, url: String) {
        val builder = CustomTabsIntent.Builder()
            .setCloseButtonIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_close))
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .setShowTitle(true)

        val customTabsIntent = builder.build()
        customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + context.packageName))
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }
}
