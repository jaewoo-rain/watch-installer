package com.snown.watchinstaller.logic

import com.snown.watchinstaller.i18n.Texts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Format {
    fun size(bytes: Long): String = when {
        bytes >= 1024L * 1024 -> String.format(Locale.US, "%.1f MB", bytes / 1024.0 / 1024.0)
        bytes >= 1024L -> String.format(Locale.US, "%.0f KB", bytes / 1024.0)
        else -> "$bytes B"
    }

    fun time(millis: Long, t: Texts): String =
        SimpleDateFormat(t.datePattern, t.locale).format(Date(millis))

    /** 0..100 */
    fun percent(sent: Long, total: Long): Int =
        if (total <= 0) 0 else ((sent * 100) / total).toInt().coerceIn(0, 100)
}
