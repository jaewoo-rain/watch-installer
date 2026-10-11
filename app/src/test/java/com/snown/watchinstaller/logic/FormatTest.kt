package com.snown.watchinstaller.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test fun size() {
        assertEquals("512 B", Format.size(512))
        assertEquals("2 KB", Format.size(2048))
        assertEquals("12.5 MB", Format.size(13_107_200))
    }

    @Test fun percent() {
        assertEquals(0, Format.percent(0, 100))
        assertEquals(50, Format.percent(50, 100))
        assertEquals(100, Format.percent(200, 100))
        assertEquals(0, Format.percent(10, 0))
    }
}
