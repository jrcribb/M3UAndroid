package com.m3u.androidApp.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.Icon
import androidx.compose.material.LocalAbsoluteElevation
import androidx.compose.material.MaterialTheme
import androidx.compose.material.NavigationRailItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.m3u.ui.components.NavigationSheet
import com.m3u.ui.model.LocalTheme

@Composable
fun BottomNavigationSheet(
    destinations: List<TopLevelDestination>,
    navigateToTopLevelDestination: NavigateToTopLevelDestination,
    currentTopLevelDestination: TopLevelDestination?,
    modifier: Modifier = Modifier
) {
    val controller = rememberSystemUiController()
    val backgroundColor = BottomSheetDefaults.navigationBackgroundColor()
    val contentColor = BottomSheetDefaults.navigationContentColor()
    NavigationSheet(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
        elevation = LocalAbsoluteElevation.current
    ) {
        destinations.forEach { destination ->
            val selected = currentTopLevelDestination == destination
            NavigationBarItem(
                alwaysShowLabel = false,
                selected = selected,
                onClick = { navigateToTopLevelDestination(destination) },
                tint = BottomSheetDefaults.navigationSelectedItemColor(),
                icon = {
                    val icon = if (selected) destination.selectedIcon
                    else destination.unselectedIcon
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(destination.iconTextId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.iconTextId),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    }


    DisposableEffect(backgroundColor) {
        controller.setNavigationBarColor(backgroundColor)
        onDispose {
            controller.setNavigationBarColor(Color.Transparent)
        }
    }
}


@Composable
private fun NavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    tint: Color
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        selectedContentColor = tint,
        interactionSource = remember { MutableInteractionSource() },
    )
}

object BottomSheetDefaults {
    @Composable
    fun navigationBackgroundColor() = Color(0xff000000)

    @Composable
    fun navigationContentColor() = Color(0xFFEEEEEE)

    @Composable
    fun navigationSelectedItemColor() = LocalTheme.current.tint
}