package com.snown.watchinstaller.apk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import java.io.File
import java.io.InputStream

/** 설치할 APK 한 개. 기본 경로의 파일이거나, 파일 선택기로 고른 문서(uri). */
data class ApkFile(
    val name: String,
    val size: Long,
    /** 수정 시각(ms). 모르면 null. */
    val modified: Long?,
    val file: File? = null,
    val uri: Uri? = null,
) {
    fun open(context: Context): InputStream =
        file?.inputStream()
            ?: context.contentResolver.openInputStream(uri!!)
            ?: error("파일을 열 수 없어요")
}

object ApkSource {
    const val DEFAULT_NAME = "snown-watch.apk"

    fun defaultFile(): File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), DEFAULT_NAME)

    /** `Download/snown-watch.apk` 를 이 앱이 직접 읽을 수 있으면(모든 파일 접근 허용 시) 그것을 돌려준다. */
    fun readDefault(): ApkFile? {
        val f = defaultFile()
        if (!canReadAllFiles() || !f.isFile || !f.canRead()) return null
        return ApkFile(f.name, f.length(), f.lastModified(), file = f)
    }

    fun canReadAllFiles(): Boolean = Environment.isExternalStorageManager()

    /** 파일 선택기로 고른 문서의 이름·크기·수정시각. 권한이 사라졌으면 null. */
    fun fromUri(context: Context, uri: Uri): ApkFile? = runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (!c.moveToFirst()) return@use null
            val name = c.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let(c::getString) ?: "선택한 파일"
            val size = c.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 && !c.isNull(it) }?.let(c::getLong) ?: return@use null
            val modified = c.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                .takeIf { it >= 0 && !c.isNull(it) }?.let(c::getLong)
            ApkFile(name, size, modified, uri = uri)
        }
    }.getOrNull()

    /** 다음에 앱을 다시 켜도 같은 파일을 쓸 수 있게 읽기 권한을 붙잡아 둔다. */
    fun keepPermission(context: Context, uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
