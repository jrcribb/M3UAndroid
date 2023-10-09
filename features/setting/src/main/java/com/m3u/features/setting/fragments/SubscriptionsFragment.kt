package com.m3u.features.setting.fragments

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m3u.data.database.entity.Live
import com.m3u.features.setting.R
import com.m3u.features.setting.components.MutedLiveItem
import com.m3u.ui.components.Button
import com.m3u.ui.components.LabelField
import com.m3u.ui.components.TextButton
import com.m3u.ui.model.LocalSpacing
import com.m3u.ui.model.LocalTheme

@Composable
internal fun SubscriptionsFragment(
    title: String,
    url: String,
    uri: Uri?,
    localStorage: Boolean,
    mutedLives: List<Live>,
    onBannedLive: (Int) -> Unit,
    onTitle: (String) -> Unit,
    onUrl: (String) -> Unit,
    onSubscribe: () -> Unit,
    onLocalStorage: () -> Unit,
    openDocument: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val theme = LocalTheme.current
    val focusRequester = remember { FocusRequester() }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(spacing.small),
        modifier = modifier.padding(spacing.medium)
    ) {
        if (mutedLives.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(spacing.medium)),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_muted_lives),
                        style = MaterialTheme.typography.button,
                        color = theme.onTint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(theme.tint)
                            .padding(
                                vertical = spacing.extraSmall,
                                horizontal = spacing.medium
                            )
                    )
                    mutedLives.forEach { live ->
                        MutedLiveItem(
                            live = live,
                            onBannedLive = { onBannedLive(live.id) },
                            modifier = Modifier.background(theme.surface)
                        )
                    }
                }
            }
        }

        item {
            LabelField(
                text = title,
                placeholder = stringResource(R.string.placeholder_title).uppercase(),
                onValueChange = onTitle,
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusRequester.requestFocus()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            if (!localStorage) {
                LabelField(
                    text = url,
                    placeholder = stringResource(R.string.placeholder_url).uppercase(),
                    onValueChange = onUrl,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onSubscribe()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            } else {
                LocalStorageButton(
                    uri = uri,
                    openDocument = openDocument
                )
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(25))
                    .toggleable(
                        value = localStorage,
                        onValueChange = { onLocalStorage() },
                        role = Role.Checkbox
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = localStorage,
                    onCheckedChange = null
                )
                Text(
                    text = stringResource(R.string.local_storage),
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        item {
            val resId = R.string.label_subscribe
            Button(
                text = stringResource(resId),
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            if (!localStorage) {
                ClipboardButton(
                    onTitle = onTitle,
                    onUrl = onUrl
                )
            }
        }
    }
}

@Composable
private fun LocalStorageButton(
    uri: Uri?,
    openDocument: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = uri != null
    val theme = LocalTheme.current
    val spacing = LocalSpacing.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
        openDocument
    )
    val icon = if (selected) Icons.Rounded.Cancel else Icons.AutoMirrored.Rounded.OpenInNew
    val text = if (selected) R.string.label_selected_from_local_storage
    else R.string.label_select_from_local_storage
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(25))
            .background(theme.surface)
            .height(48.dp)
            .fillMaxWidth()
            .toggleable(
                value = selected,
                onValueChange = {
                    if (!it) openDocument(null)
                    else {
                        launcher.launch("*/*")
                    }
                },
                enabled = true,
                role = Role.Checkbox
            )
            .padding(
                horizontal = spacing.medium,
                vertical = 12.5.dp
            )
    ) {
        Text(
            text = stringResource(text).uppercase(),
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = MaterialTheme.typography.body1.fontFamily,
                fontWeight = FontWeight.Medium
            )
        )
        Icon(
            imageVector = icon,
            contentDescription = null
        )
    }
}

@Composable
private fun ClipboardButton(
    onTitle: (String) -> Unit,
    onUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    TextButton(
        text = stringResource(R.string.label_parse_from_clipboard),
        onClick = {
            val clipboardUrl = clipboardManager.getText()?.text.orEmpty()
            val clipboardTitle = run {
                val filePath = clipboardUrl.split("/")
                val fileSplit = filePath.lastOrNull()?.split(".") ?: emptyList()
                fileSplit.firstOrNull() ?: "Feed_${System.currentTimeMillis()}"
            }
            onTitle(clipboardTitle)
            onUrl(clipboardUrl)
        },
        modifier = modifier.fillMaxWidth()
    )
}