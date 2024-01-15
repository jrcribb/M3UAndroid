package com.m3u.ui

import android.graphics.Rect
import androidx.annotation.StringRes
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import com.m3u.core.unspecified.UBoolean
import com.m3u.core.wrapper.Message
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

typealias OnUserLeaveHint = () -> Unit
typealias OnPipModeChanged = Consumer<PictureInPictureModeChangedInfo>

@Stable
interface Helper {
    var title: String
    var actions: ImmutableList<Action>
    var fob: Fob?
    var statusBarVisibility: UBoolean
    var navigationBarVisibility: UBoolean
    var onUserLeaveHint: OnUserLeaveHint?
    var onPipModeChanged: OnPipModeChanged?
    var darkMode: UBoolean
    var brightness: Float
    val isInPipMode: Boolean
    var screenOrientation: Int
    val message: StateFlow<Message>
    var deep: Int

    @get:Composable
    val windowSizeClass: WindowSizeClass

    fun enterPipMode(size: Rect)
    fun toast(message: String)
    fun log(message: Message)
    fun play(url: String)
    fun replay()
}

val Helper.useRailNav: Boolean
    @Composable get() = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact

val LocalHelper = staticCompositionLocalOf { EmptyHelper }

@Immutable
data class Action(
    val icon: ImageVector,
    val contentDescription: String?,
    val onClick: () -> Unit
)

@Immutable
data class Fob(
    val rootDestination: Destination.Root,
    val icon: ImageVector,
    @StringRes val iconTextId: Int,
    val onClick: () -> Unit
)

val EmptyHelper = object : Helper {
    override var title: String
        get() = error("Cannot get title")
        set(_) {
            error("Cannot set title")
        }

    override var actions: ImmutableList<Action>
        get() = error("Cannot get actions")
        set(_) {
            error("Cannot set actions")
        }
    override var fob: Fob?
        get() = error("Cannot get fob")
        set(_) {
            error("Cannot set fob")
        }
    override val message: StateFlow<Message>
        get() = MutableStateFlow(Message.Dynamic.EMPTY)

    override var statusBarVisibility: UBoolean
        get() = error("Cannot get systemUiVisibility")
        set(_) {
            error("Cannot set systemUiVisibility")
        }
    override var navigationBarVisibility: UBoolean
        get() = error("Cannot get navigationBarsVisibility")
        set(_) {
            error("Cannot set navigationBarsVisibility")
        }

    override var deep: Int
        get() = error("Cannot get deep")
        set(_) {
            error("Cannot set deep")
        }

    override var darkMode: UBoolean
        get() = error("Cannot get darkMode")
        set(_) {
            error("Cannot set darkMode")
        }

    override var onUserLeaveHint: OnUserLeaveHint?
        get() = error("Cannot get onUserLeaveHint")
        set(_) {
            error("Cannot set onUserLeaveHint")
        }
    override var onPipModeChanged: OnPipModeChanged?
        get() = error("Cannot get onPipModeChanged")
        set(_) {
            error("Cannot set onPipModeChanged")
        }

    override var brightness: Float
        get() = error("Cannot get brightness")
        set(_) {
            error("Cannot set brightness")
        }

    override val isInPipMode: Boolean
        get() = error("Cannot get isInPipMode")

    override var screenOrientation: Int
        get() = error("Cannot get screenOrientation")
        set(_) {
            error("Cannot set screenOrientation")
        }

    override val windowSizeClass: WindowSizeClass
        @Composable get() = error("Cannot get windowSizeClass")

    override fun enterPipMode(size: Rect) = error("Cannot enterPipMode")
    override fun toast(message: String) {
        error("Cannot toast: $message")
    }

    override fun log(message: Message) {
        error("Cannot snake: $message")
    }

    override fun play(url: String) {
        error("Cannot play stream: $url")
    }

    override fun replay() {
        error("Cannot replay")
    }
}
