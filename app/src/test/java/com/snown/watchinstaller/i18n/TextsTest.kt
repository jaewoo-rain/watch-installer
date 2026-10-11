package com.snown.watchinstaller.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class TextsTest {

    /** 인자 없는 문구 전부(리플렉션으로 꺼낸다). */
    private fun plain(t: Texts): List<String> = Texts::class.java.methods
        .filter { it.parameterCount == 0 && it.returnType == String::class.java }
        .map { it.invoke(t) as String }

    private fun all(t: Texts): List<String> = plain(t) +
        listOf(t.connectedTo("1.2.3.4:5"), t.modifiedAt("x"), t.installNoCode("x"), t.installOther("x"), t.errUnknown("x"))

    @Test fun first_run_follows_device_language() {
        assertEquals(Lang.KO, Lang.fromLocale(Locale.KOREA))
        assertEquals(Lang.EN, Lang.fromLocale(Locale.US))
        assertEquals(Lang.EN, Lang.fromLocale(Locale.JAPAN))
    }

    @Test fun saved_choice_round_trips() {
        Lang.entries.forEach { assertEquals(it, Lang.fromTag(it.name)) }
        assertNull(Lang.fromTag(null))
        assertNull(Lang.fromTag("fr"))
    }

    @Test fun app_names() {
        assertEquals("웨어 인스톨러", Ko.appName)
        assertEquals("Wear Installer", En.appName)
    }

    /** 영어 문구에 한글이 섞여 있지 않은지 — 옮기다 빠뜨린 곳 찾기. */
    @Test fun english_has_no_hangul() {
        val values = all(En)
        assertTrue(values.size > 80)
        values.forEach { assertFalse(it, Regex("[가-힣]").containsMatchIn(it)) }
    }

    @Test fun no_blank_texts() {
        listOf(Ko, En).forEach { t -> all(t).forEach { assertTrue(it.isNotBlank()) } }
    }

    @Test fun same_wifi_guidance() {
        assertTrue(Ko.sameWifi.contains("같은 Wi-Fi") && Ko.sameWifi.contains("핫스팟"))
        assertTrue(En.sameWifi.contains("same Wi-Fi") && En.sameWifi.contains("hotspot"))
    }

    @Test fun no_brand_names_on_screen() {
        val brand = Regex("스논|snown", RegexOption.IGNORE_CASE)
        listOf(Ko, En).forEach { t -> all(t).forEach { assertFalse(it, brand.containsMatchIn(it)) } }
    }
}
