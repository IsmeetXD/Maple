package com.ismeetxd.maple.database.backup

import com.ismeetxd.maple.database.objects.CommandTemplate
import com.ismeetxd.maple.database.objects.DownloadedVideoInfo
import com.ismeetxd.maple.database.objects.OptionShortcut
import kotlinx.serialization.Serializable

@Serializable
data class Backup(
    val templates: List<CommandTemplate>? = null,
    val shortcuts: List<OptionShortcut>? = null,
    val downloadHistory: List<DownloadedVideoInfo>? = null,
)
