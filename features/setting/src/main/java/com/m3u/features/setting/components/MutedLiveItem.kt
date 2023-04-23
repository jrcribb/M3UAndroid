package com.m3u.features.setting.components

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.m3u.data.database.entity.Live
import com.m3u.ui.components.IconButton

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun MutedLiveItem(
    live: Live,
    onBannedLive: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        text = {
            val text = live.title
            Text(
                text = text,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryText = {
            val text = live.url
            Text(
                text = text,
                style = MaterialTheme.typography.subtitle2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailing = {
            IconButton(
                icon = Icons.Rounded.Close,
                contentDescription = "voice",
                onClick = onBannedLive
            )
        },
        modifier = modifier
    )
}