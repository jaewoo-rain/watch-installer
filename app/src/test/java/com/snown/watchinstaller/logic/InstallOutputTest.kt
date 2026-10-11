package com.snown.watchinstaller.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallOutputTest {

    @Test fun success_line() {
        assertEquals(InstallResult.Success, InstallOutput.parse("Success\n", ended = false))
        assertEquals(InstallResult.Success, InstallOutput.parse("Success", ended = false))
    }

    @Test fun failure_line_keeps_text() {
        val r = InstallOutput.parse("Failure [INSTALL_FAILED_VERSION_DOWNGRADE]\n", ended = false)
        assertEquals(InstallResult.Failure("Failure [INSTALL_FAILED_VERSION_DOWNGRADE]"), r)
    }

    @Test fun result_after_other_lines() {
        val out = "Performing Streamed Install\r\nSuccess\r\n"
        assertEquals(InstallResult.Success, InstallOutput.parse(out, ended = false))
        val fail = InstallOutput.parse("Performing Streamed Install\nFailure [INSTALL_FAILED_OLDER_SDK]\n", ended = false)
        assertTrue(fail is InstallResult.Failure && fail.output.contains("INSTALL_FAILED_OLDER_SDK"))
    }

    @Test fun waits_while_no_result_line() {
        assertNull(InstallOutput.parse("", ended = false))
        assertNull(InstallOutput.parse("Performing Streamed Install\n", ended = false))
    }

    @Test fun ended_without_result_is_failure_with_output() {
        assertEquals(InstallResult.Failure("cmd: Can't find service: package"),
            InstallOutput.parse("cmd: Can't find service: package\n", ended = true))
        assertEquals(InstallResult.Failure(""), InstallOutput.parse("", ended = true))
    }

    @Test fun timeout() {
        assertFalse(InstallOutput.timedOut(startedAt = 1_000, now = 1_000))
        assertFalse(InstallOutput.timedOut(startedAt = 1_000, now = 1_000 + InstallOutput.TIMEOUT_MS - 1))
        assertTrue(InstallOutput.timedOut(startedAt = 1_000, now = 1_000 + InstallOutput.TIMEOUT_MS))
        assertEquals(120_000L, InstallOutput.TIMEOUT_MS)
    }
}
