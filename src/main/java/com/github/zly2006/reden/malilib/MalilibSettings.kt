@file:Suppress("HasPlatformType")

package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.malilib.options.*
import com.github.zly2006.reden.utils.startDebugAppender
import com.github.zly2006.reden.utils.stopDebugAppender
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.options.ConfigBase
import fi.dy.masa.malilib.hotkeys.IHotkey

@JvmField val HOTKEYS = mutableListOf<IHotkey>()
@JvmField val GENERIC_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val DEBUG_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val HIDDEN_TAB = mutableListOf<ConfigBase<*>>()
private fun <T : IHotkey> T.hotkey() = this.apply(HOTKEYS::add)
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.generic() = this.apply(GENERIC_TAB::add) as T
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.debug() = this.apply(DEBUG_TAB::add) as T
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.hidden() = this.apply(HIDDEN_TAB::add) as T

// Generic
@JvmField val REDEN_CONFIG_KEY = RedenConfigHotkey("redenConfigKey", "R,C").generic().hotkey()
@JvmField val SELECTION_TOOL = RedenConfigString("selectionTool", "minecraft:blaze_rod").generic()
@JvmField val BLOCK_BORDER_ALPHA = RedenConfigFloat("blockBorderAlpha", 0.1f, 0f, 1f).generic()
@JvmField val UNDO_KEY = RedenConfigHotkey("rollbackKey", "LEFT_CONTROL,Z").generic().hotkey()
@JvmField val REDO_KEY = RedenConfigHotkey("redoKey", "LEFT_CONTROL,Y").generic().hotkey()
@JvmField val UNDO_CHEATING_ONLY = RedenConfigBoolean("undoCheatingOnly", true).generic()
@JvmField val MAX_RENDER_DISTANCE = RedenConfigInteger("maxRenderDistance", 48).generic()
@JvmField val EASTER_EGG_RATE = RedenConfigInteger("easterEggRate", 3, 0, 100).generic()
@JvmField val RUN_COMMAND = RedenConfigCommandHotkeyList("runCommand").generic()
// Debug
@JvmField val DEBUG_LOGGER = RedenConfigBoolean("debugLogger", false) {
    if (booleanValue) startDebugAppender()
    else stopDebugAppender()
}.debug()
@JvmField val DEBUG_TAG_BLOCK_POS = RedenConfigHotkey("debugTagBlockPos", "LEFT_CONTROL,LEFT_SHIFT,T").debug().hotkey()
@JvmField val DEBUG_PREVIEW_UNDO = RedenConfigHotkey("debugPreviewUndo", "LEFT_CONTROL,LEFT_SHIFT,Z").debug().hotkey()
@JvmField val ALLOW_SOCIAL_FOLLOW = RedenConfigBoolean("allowSocialFollow", true).debug()
@JvmField val SPONSOR_SCREEN_KEY = RedenConfigHotkey("sponsorScreenKey", "").debug().hotkey()
@JvmField val CREDIT_SCREEN_KEY = RedenConfigHotkey("creditScreenKey", "").debug().hotkey()
@JvmField val DEBUG_VIEW_ALL_CONFIGS = RedenConfigHotkey("debugViewAllConfigs", "").debug().hotkey()

// Hidden
@JvmField val iEVER_USED_UNDO = RedenConfigBoolean("iEverUsedUndo", false).hidden()
@JvmField val iPRIVACY_SETTING_SHOWN = RedenConfigBoolean("iPrivacySettingShown", false).hidden()
@JvmField val data_BASIC = RedenConfigBoolean("dataBasic", true).hidden()
@JvmField val data_USAGE = RedenConfigBoolean("dataUsage", true).hidden()
@JvmField val data_IDENTIFICATION = RedenConfigBoolean("dataIdentification", false).hidden()

fun getAllOptions() = GENERIC_TAB + DEBUG_TAB + HIDDEN_TAB
