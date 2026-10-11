package com.snown.watchinstaller.logic

import com.snown.watchinstaller.i18n.En
import com.snown.watchinstaller.i18n.Ko
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidationTest {

    @Test fun ip_ok() {
        assertNull(Validation.ipError("192.168.0.12"))
        assertNull(Validation.ipError(" 10.0.0.1 "))
    }

    @Test fun ip_bad() {
        assertNotNull(Validation.ipError(""))
        assertNotNull(Validation.ipError("192.168.0"))
        assertNotNull(Validation.ipError("192.168.0.256"))
        assertNotNull(Validation.ipError("watch.local"))
        assertNotNull(Validation.ipError("192.168.0.12:37123"))
    }

    @Test fun port() {
        assertNull(Validation.portError("37123"))
        assertNull(Validation.portError("1"))
        assertNull(Validation.portError("65535"))
        assertNotNull(Validation.portError(""))
        assertNotNull(Validation.portError("0"))
        assertNotNull(Validation.portError("65536"))
        assertNotNull(Validation.portError("12a"))
    }

    @Test fun code() {
        assertNull(Validation.codeError("123456"))
        assertNull(Validation.codeError(" 012345 "))
        assertNotNull(Validation.codeError(""))
        assertNotNull(Validation.codeError("12345"))
        assertNotNull(Validation.codeError("1234567"))
        assertNotNull(Validation.codeError("12345a"))
    }

    @Test fun splitHostPort() {
        assertEquals("192.168.0.12" to "37123", Validation.splitHostPort("192.168.0.12:37123"))
        assertEquals("192.168.0.12" to "37123", Validation.splitHostPort(" 192.168.0.12 : 37123 "))
        assertNull(Validation.splitHostPort("192.168.0.12"))
        assertNull(Validation.splitHostPort("192.168.0.12:"))
        assertNull(Validation.splitHostPort(":37123"))
    }

    @Test fun messages_in_both_languages() {
        assertEquals("IP 주소를 넣어 주세요", Validation.ipError("")!!(Ko))
        assertEquals("Enter the IP address", Validation.ipError("")!!(En))
        assertEquals("Each number must be between 0 and 255", Validation.ipError("1.2.3.256")!!(En))
        assertEquals("The port must contain only digits", Validation.portError("12a")!!(En))
        assertEquals("It must be exactly 6 digits", Validation.codeError("12345")!!(En))
        assertEquals("숫자 6자리여야 해요", Validation.codeError("12345")!!(Ko))
    }
}
