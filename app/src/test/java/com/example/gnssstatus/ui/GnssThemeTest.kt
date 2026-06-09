package com.example.gnssstatus.ui

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class GnssThemeTest {
    @Test
    fun `uses black dark theme surfaces`() {
        assertEquals(Color.Black, GnssColors.AppBackground)
        assertEquals(Color(0xFF101214), GnssColors.CardBackground)
        assertEquals(Color(0xFF1B1F23), GnssColors.MetricBackground)
        assertEquals(Color(0xFFE8EAED), GnssColors.PrimaryText)
    }
}
