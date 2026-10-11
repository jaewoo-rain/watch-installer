package com.snown.watchinstaller.logic

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
}
