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

@file:OptIn(ExperimentalAnimationApi::class)

package com.wire.android.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.wire.android.navigation.HomeNavigationItem
import com.wire.android.navigation.navigateToItemInHome
import com.wire.android.navigation.rememberTrackingAnimatedNavController
import com.wire.android.ui.common.topappbar.search.SearchBarState
import com.wire.android.ui.common.topappbar.search.rememberSearchbarState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
class HomeStateHolder(
    val coroutineScope: CoroutineScope,
    val navController: NavHostController,
    val drawerState: DrawerState,
    val bottomSheetState: ModalBottomSheetState,
    val currentNavigationItem: HomeNavigationItem,
    val snackBarHostState: SnackbarHostState,
    val searchBarState: SearchBarState
) {

    var homeBottomSheetContent: @Composable (ColumnScope.() -> Unit)? by mutableStateOf(null)
        private set

    var snackbarState: HomeSnackbarState by mutableStateOf(HomeSnackbarState.None)
        private set

    fun setSnackBarState(state: HomeSnackbarState) {
        snackbarState = state
        if (state != HomeSnackbarState.None) closeBottomSheet()
    }

    fun clearSnackbarMessage() {
        setSnackBarState(HomeSnackbarState.None)
    }

    fun openBottomSheet() {
        coroutineScope.launch {
            if (!bottomSheetState.isVisible) bottomSheetState.animateTo(ModalBottomSheetValue.Expanded)
        }
    }

    fun closeBottomSheet() {
        coroutineScope.launch {
            if (bottomSheetState.isVisible) bottomSheetState.animateTo(ModalBottomSheetValue.Hidden)
        }
    }

    fun isBottomSheetVisible() = bottomSheetState.isVisible

    fun changeBottomSheetContent(content: @Composable ColumnScope.() -> Unit) {
        homeBottomSheetContent = content
    }

    fun closeDrawer() {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    fun openDrawer() {
        coroutineScope.launch {
            drawerState.open()
        }
    }

    fun navigateTo(homeNavigationItem: HomeNavigationItem) {
        navigateToItemInHome(navController, homeNavigationItem)
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun rememberHomeScreenState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberTrackingAnimatedNavController() { HomeNavigationItem.fromRoute(it)?.itemName },
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
): HomeStateHolder {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentNavigationItem = currentRoute?.let { HomeNavigationItem.fromRoute(it) } ?: HomeNavigationItem.Conversations

    val searchBarState = rememberSearchbarState()

    val homeState = remember(
        currentNavigationItem
    ) {
        HomeStateHolder(
            coroutineScope,
            navController,
            drawerState,
            bottomSheetState,
            currentNavigationItem,
            snackBarHostState,
            searchBarState
        )
    }

    return homeState
}
