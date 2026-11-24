package com.ismeetxd.maple

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.ismeetxd.maple.App.Companion.applicationScope
import com.ismeetxd.maple.App.Companion.context
import com.ismeetxd.maple.App.Companion.startService
import com.ismeetxd.maple.App.Companion.stopService
import com.ismeetxd.maple.database.objects.CommandTemplate
import com.ismeetxd.maple.util.COMMAND_DIRECTORY
import com.ismeetxd.maple.util.DownloadUtil
import com.ismeetxd.maple.util.FileUtil
import com.ismeetxd.maple.util.NotificationUtil
import com.ismeetxd.maple.util.PreferenceUtil.getString
import com.ismeetxd.maple.util.ToastUtil
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Singleton Downloader for custom command tasks & service state holder */
object Downloader {

    private const val TAG = "Downloader"

    sealed class State {
        data class DownloadingPlaylist(val currentItem: Int = 0, val itemCount: Int = 0) : State()

        data object DownloadingVideo : State()

        data object FetchingInfo : State()

        data object Idle : State()

        data object Updating : State()
    }

    data class CustomCommandTask(
        val template: CommandTemplate,
        val url: String,
        val output: String,
        val state: State,
        val currentLine: String,
    ) {
        fun toKey() = makeKey(url, template.name)

        sealed class State {
            data class Error(val errorReport: String) : State()

            object Completed : State()

            object Canceled : State()

            data class Running(val progress: Float) : State()
        }

        override fun hashCode(): Int {
            return (this.url + this.template.name + this.template.template).hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CustomCommandTask

            if (template != other.template) return false
            if (url != other.url) return false
            if (output != other.output) return false
            if (state != other.state) return false
            if (currentLine != other.currentLine) return false

            return true
        }

        fun onCopyLog(clipboardManager: ClipboardManager) {
            clipboardManager.setText(AnnotatedString(output))
        }

        fun onRestart() {
            applicationScope.launch(Dispatchers.IO) {
                DownloadUtil.executeCommandInBackground(url, template)
            }
        }

        fun onCopyError(clipboardManager: ClipboardManager) {
            clipboardManager.setText(AnnotatedString(currentLine))
            ToastUtil.makeToast(R.string.error_copied)
        }

        fun onCancel() {
            toKey().run {
                YoutubeDL.destroyProcessById(this)
                onProcessCanceled(this)
            }
        }
    }

    private val mutableDownloaderState: MutableStateFlow<State> = MutableStateFlow(State.Idle)
    private val mutableProcessCount = MutableStateFlow(0)
    private val mutableQuickDownloadCount = MutableStateFlow(0)

    val mutableTaskList = mutableStateMapOf<String, CustomCommandTask>()

    val downloaderState = mutableDownloaderState.asStateFlow()
    val processCount = mutableProcessCount.asStateFlow()

    init {
        applicationScope.launch {
            downloaderState
                .combine(processCount) { state, cnt ->
                    if (cnt > 0) true
                    else
                        when (state) {
                            is State.Idle -> false
                            else -> true
                        }
                }
                .combine(mutableQuickDownloadCount) { isRunning, cnt ->
                    if (!isRunning) cnt > 0 else true
                }
                .collect { if (it) startService() else stopService() }
        }
    }

    fun isDownloaderAvailable(): Boolean {
        return downloaderState.value is State.Idle
    }

    fun makeKey(url: String, templateName: String): String = "${templateName}_$url"

    fun onTaskStarted(template: CommandTemplate, url: String) =
        CustomCommandTask(
                template = template,
                url = url,
                output = "",
                state = CustomCommandTask.State.Running(0f),
                currentLine = "",
            )
            .run { mutableTaskList.put(this.toKey(), this) }

    fun updateTaskOutput(template: CommandTemplate, url: String, line: String, progress: Float) {
        val key = makeKey(url, template.name)
        val oldValue = mutableTaskList[key] ?: return
        val newValue =
            oldValue.run {
                copy(
                    output = output + line + "\n",
                    currentLine = line,
                    state = CustomCommandTask.State.Running(progress),
                )
            }
        mutableTaskList[key] = newValue
    }

    fun onTaskEnded(template: CommandTemplate, url: String, response: String? = null) {
        val key = makeKey(url, template.name)
        NotificationUtil.finishNotification(
            notificationId = key.toNotificationId(),
            title = key,
            text = context.getString(R.string.status_completed),
        )
        mutableTaskList.run {
            val oldValue = get(key) ?: return
            val newValue =
                oldValue.copy(state = CustomCommandTask.State.Completed).run {
                    response?.let { copy(output = response) } ?: this
                }
            this[key] = newValue
        }
        FileUtil.scanDownloadDirectoryToMediaLibrary(COMMAND_DIRECTORY.getString())
    }

    fun onProcessEnded() = mutableProcessCount.update { it - 1 }

    fun onProcessCanceled(taskId: String) =
        mutableTaskList.run {
            get(taskId)?.let { this.put(taskId, it.copy(state = CustomCommandTask.State.Canceled)) }
        }

    fun onTaskError(errorReport: String, template: CommandTemplate, url: String) =
        mutableTaskList.run {
            val key = makeKey(url, template.name)
            NotificationUtil.notifyError(
                title = "",
                notificationId = key.toNotificationId(),
                report = errorReport,
            )
            val oldValue = mutableTaskList[key] ?: return
            mutableTaskList[key] =
                oldValue.copy(
                    state = CustomCommandTask.State.Error(errorReport),
                    currentLine = errorReport,
                    output = oldValue.output + "\n" + errorReport,
                )
        }

    fun updateState(state: State) = mutableDownloaderState.update { state }

    fun executeCommandWithUrl(url: String) =
        applicationScope.launch(Dispatchers.IO) { DownloadUtil.executeCommandInBackground(url) }

    fun onProcessStarted() = mutableProcessCount.update { it + 1 }

    fun String.toNotificationId(): Int = this.hashCode()
}
