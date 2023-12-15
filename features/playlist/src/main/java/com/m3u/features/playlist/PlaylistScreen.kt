@file:OptIn(ExperimentalMaterialApi::class)

package com.m3u.features.playlist

import android.Manifest
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.content.res.Configuration.UI_MODE_TYPE_APPLIANCE
import android.content.res.Configuration.UI_MODE_TYPE_CAR
import android.content.res.Configuration.UI_MODE_TYPE_DESK
import android.content.res.Configuration.UI_MODE_TYPE_MASK
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
import android.content.res.Configuration.UI_MODE_TYPE_VR_HEADSET
import android.content.res.Configuration.UI_MODE_TYPE_WATCH
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BackdropScaffold
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowCircleUp
import androidx.compose.material.icons.rounded.Refresh
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.foundation.lazy.grid.rememberTvLazyGridState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.m3u.core.architecture.pref.LocalPref
import com.m3u.core.util.compose.observableStateOf
import com.m3u.core.wrapper.Event
import com.m3u.data.database.entity.Stream
import com.m3u.data.database.entity.StreamHolder
import com.m3u.data.database.entity.rememberStreamHolder
import com.m3u.features.playlist.components.DialogStatus
import com.m3u.features.playlist.components.PlaylistDialog
import com.m3u.features.playlist.components.StreamGallery
import com.m3u.features.playlist.components.TvStreamGallery
import com.m3u.i18n.R.string
import com.m3u.material.components.Background
import com.m3u.material.components.TextField
import com.m3u.material.ktx.animateColor
import com.m3u.material.ktx.interceptVolumeEvent
import com.m3u.material.ktx.isAtTop
import com.m3u.material.model.LocalSpacing
import com.m3u.ui.Action
import com.m3u.ui.Destination
import com.m3u.ui.EventHandler
import com.m3u.ui.Fob
import com.m3u.ui.LocalHelper
import com.m3u.ui.MessageEventHandler
import com.m3u.ui.isAtTop
import com.m3u.ui.repeatOnLifecycle
import kotlinx.coroutines.launch

internal typealias NavigateToStream = () -> Unit

private typealias OnMenu = (Stream) -> Unit
private typealias OnScrollUp = () -> Unit
private typealias OnRefresh = () -> Unit

@Composable
internal fun PlaylistRoute(
    contentPadding: PaddingValues,
    playlistUrl: String,
    navigateToStream: NavigateToStream,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val helper = LocalHelper.current
    val pref = LocalPref.current

    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val floating by viewModel.floating.collectAsStateWithLifecycle()
    var dialogStatus: DialogStatus by remember { mutableStateOf(DialogStatus.Idle) }
    val writeExternalPermissionState = rememberPermissionState(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    MessageEventHandler(message)

    LaunchedEffect(playlistUrl) {
        viewModel.onEvent(PlaylistEvent.Observe(playlistUrl))
    }

    helper.repeatOnLifecycle {
        actions = listOf(
            Action(
                icon = Icons.Rounded.Refresh,
                contentDescription = "refresh",
                onClick = {
                    viewModel.onEvent(PlaylistEvent.Refresh)
                }
            )
        )
    }

    LaunchedEffect(pref.autoRefresh, state.url) {
        if (state.url.isNotEmpty() && pref.autoRefresh) {
            viewModel.onEvent(PlaylistEvent.Refresh)
        }
    }

    BackHandler(state.query.isNotEmpty()) {
        viewModel.onEvent(PlaylistEvent.Query(""))
    }
    val interceptVolumeEventModifier = remember(pref.godMode) {
        if (pref.godMode) {
            Modifier.interceptVolumeEvent { event ->
                when (event) {
                    KeyEvent.KEYCODE_VOLUME_UP -> pref.rowCount =
                        (pref.rowCount - 1).coerceAtLeast(1)

                    KeyEvent.KEYCODE_VOLUME_DOWN -> pref.rowCount =
                        (pref.rowCount + 1).coerceAtMost(3)
                }
            }
        } else Modifier
    }

    PlaylistScreen(
        query = state.query,
        onQuery = { viewModel.onEvent(PlaylistEvent.Query(it)) },
        rowCount = pref.rowCount,
        channelHolder = rememberChannelHolder(
            channels = state.channels,
            floating = floating
        ),
        scrollUp = state.scrollUp,
        refreshing = state.fetching,
        onRefresh = { viewModel.onEvent(PlaylistEvent.Refresh) },
        navigateToStream = navigateToStream,
        onMenu = {
            dialogStatus = DialogStatus.Selections(it)
        },
        onScrollUp = { viewModel.onEvent(PlaylistEvent.ScrollUp) },
        contentPadding = contentPadding,
        modifier = modifier
            .fillMaxSize()
            .then(interceptVolumeEventModifier)
    )

    PlaylistDialog(
        status = dialogStatus,
        onUpdate = { dialogStatus = it },
        onFavorite = { id, target -> viewModel.onEvent(PlaylistEvent.Favourite(id, target)) },
        onBanned = { id, target -> viewModel.onEvent(PlaylistEvent.Mute(id, target)) },
        onSavePicture = { id ->
            if (writeExternalPermissionState.status is PermissionStatus.Denied) {
                writeExternalPermissionState.launchPermissionRequest()
                return@PlaylistDialog
            }
            viewModel.onEvent(PlaylistEvent.SavePicture(id))
        }
    )
}

@Composable
private fun PlaylistScreen(
    query: String,
    onQuery: (String) -> Unit,
    rowCount: Int,
    channelHolder: ChannelHolder,
    scrollUp: Event<Unit>,
    refreshing: Boolean,
    onRefresh: OnRefresh,
    navigateToStream: NavigateToStream,
    onMenu: OnMenu,
    onScrollUp: OnScrollUp,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val helper = LocalHelper.current
    val theme = MaterialTheme.colorScheme
    val pref = LocalPref.current
    val configuration = LocalConfiguration.current
    val spacing = LocalSpacing.current
    Box(modifier) {
        val isAtTopState = remember {
            observableStateOf(true) { newValue ->
                helper.fob = if (newValue) null
                else {
                    Fob(
                        icon = Icons.Rounded.ArrowCircleUp,
                        rootDestination = Destination.Root.Foryou,
                        onClick = onScrollUp
                    )
                }
            }
        }

        val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)
        val connection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    return if (scaffoldState.isRevealed) available
                    else Offset.Zero
                }
            }
        }
        val currentColor by animateColor("color") { theme.background }
        val currentContentColor by animateColor("color") { theme.onBackground }
        val focusManager = LocalFocusManager.current

        BackdropScaffold(
            scaffoldState = scaffoldState,
            appBar = { /*TODO*/ },
            frontLayerShape = RectangleShape,
            peekHeight = 0.dp,
            backLayerContent = {
                LaunchedEffect(scaffoldState.currentValue) {
                    if (scaffoldState.isConcealed) {
                        focusManager.clearFocus()
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(spacing.medium)
                        .fillMaxWidth()
                ) {
                    TextField(
                        text = query,
                        onValueChange = onQuery,
                        fontWeight = FontWeight.Bold,
                        placeholder = stringResource(string.feat_playlist_query_placeholder).capitalize(
                            Locale.current
                        )
                    )
                }
            },
            frontLayerContent = {
                Background(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PlaylistPager(channelHolder) { streamHolder, padding ->
                        val type = configuration.uiMode and UI_MODE_TYPE_MASK
                        when {
                            !pref.useCommonUIMode && type == UI_MODE_TYPE_TELEVISION -> {
                                val state = rememberTvLazyGridState()
                                LaunchedEffect(state.isAtTop) {
                                    isAtTopState.value = state.isAtTop
                                }
                                EventHandler(scrollUp) {
                                    state.animateScrollToItem(0)
                                }
                                TvStreamGallery(
                                    state = state,
                                    rowCount = 4,
                                    streamHolder = streamHolder,
                                    play = { url ->
                                        helper.play(url)
                                        navigateToStream()
                                    },
                                    onMenu = onMenu,
                                    contentPadding = padding
                                )
                            }

                            else -> {
                                val state = rememberLazyStaggeredGridState()
                                LaunchedEffect(state.isAtTop) {
                                    isAtTopState.value = state.isAtTop
                                }
                                EventHandler(scrollUp) {
                                    state.animateScrollToItem(0)
                                }
                                val orientation = configuration.orientation
                                val actualRowCount = remember(orientation, rowCount) {
                                    when (orientation) {
                                        ORIENTATION_LANDSCAPE -> rowCount + 2
                                        ORIENTATION_PORTRAIT -> rowCount
                                        else -> rowCount
                                    }
                                }
                                StreamGallery(
                                    state = state,
                                    rowCount = actualRowCount,
                                    streamHolder = streamHolder,
                                    play = { url ->
                                        helper.play(url)
                                        navigateToStream()
                                    },
                                    onMenu = onMenu,
                                    modifier = modifier,
                                    contentPadding = padding
                                )
                            }
                        }
                    }
                }
            },
            backLayerBackgroundColor = currentColor,
            backLayerContentColor = currentContentColor,
            frontLayerScrimColor = currentColor.copy(alpha = 0.45f),
            frontLayerBackgroundColor = Color.Transparent,
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .nestedScroll(
                    connection = connection,
                )
        )
    }
}

@Composable
private fun PlaylistPager(
    channelHolder: ChannelHolder,
    modifier: Modifier = Modifier,
    content: @Composable (streamHolder: StreamHolder, PaddingValues) -> Unit,
) {
    val theme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    Box(modifier) {
        val channels = channelHolder.channels
        val floating = channelHolder.floating
        val pagerState = rememberPagerState { channels.size }
        val coroutineScope = rememberCoroutineScope()
        val holders = List(channels.size) {
            rememberStreamHolder(
                streams = channels[it].streams,
                floating = floating
            )
        }
        var tabRowHeight by remember { mutableStateOf(0.dp) }
        HorizontalPager(pagerState) { pager ->
            content(
                holders[pager].copy(floating = floating),
                PaddingValues(top = tabRowHeight)
            )
        }
        if (channels.size > 1) {
            PrimaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    val index = pagerState.currentPage
                    with(TabRowDefaults) {
                        Modifier.tabIndicatorOffset(
                            currentTabPosition = tabPositions[index]
                        )
                    }
                },
                tabs = {
                    val keys = remember(channels) { channels.map { it.title } }
                    keys.forEachIndexed { index, title ->
                        val selected = pagerState.currentPage == index
                        Tab(
                            selected = selected,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (selected) theme.onBackground else Color.Unspecified
                                )
                            },
                            icon = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        tabRowHeight = with(density) {
                            it.size.height.toDp()
                        }
                    }
            )
        }
    }
}

@Composable
private fun UnsupportedUIModeContent(
    type: Int,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    val spacing = LocalSpacing.current

    val device = remember(type) {
        when (type) {
            UI_MODE_TYPE_NORMAL -> "Normal"
            UI_MODE_TYPE_DESK -> "Desk"
            UI_MODE_TYPE_CAR -> "Car"
            UI_MODE_TYPE_TELEVISION -> "Television"
            UI_MODE_TYPE_APPLIANCE -> "Appliance"
            UI_MODE_TYPE_WATCH -> "Watch"
            UI_MODE_TYPE_VR_HEADSET -> "VR-Headset"
            else -> "Device Type $type"
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            spacing.medium,
            Alignment.CenterVertically
        ),
        modifier = modifier.fillMaxSize()
    ) {
        Text("Unsupported UI Mode: $device")
        if (description != null) {
            Text(description)
        }
    }
}