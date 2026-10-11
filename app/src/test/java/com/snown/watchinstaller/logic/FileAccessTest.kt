package com.snown.watchinstaller.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.FileNotFoundException
import java.io.IOException
import java.net.SocketException

class FileAccessTest {

    @Test fun unreadable_file_errors() {
        assertTrue(FileAccess.isUnreadable(SecurityException("com.samsung.android.providers.media has no access to content://media/external_primary/file/1")))
        assertTrue(FileAccess.isUnreadable(FileNotFoundException("open failed: EACCES")))
        assertTrue(FileAccess.isUnreadable(IllegalArgumentException("Unknown URI")))
    }

    @Test fun connection_errors_are_not_file_errors() {
        assertFalse(FileAccess.isUnreadable(SocketException("Connection reset")))
        assertFalse(FileAccess.isUnreadable(IOException("Stream closed")))
    }
}
