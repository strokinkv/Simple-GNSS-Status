package com.example.gnssstatus.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class GnssFormattersTest {
    @Test
    fun `formats coordinates with six decimals`() {
        assertEquals("55.751244, 37.618423", formatCoordinates(55.7512444, 37.6184231))
    }

    @Test
    fun `formats missing coordinates as waiting`() {
        assertEquals("Ожидание координат", formatCoordinates(null, 37.6184231))
        assertEquals("Ожидание координат", formatCoordinates(55.7512444, null))
    }

    @Test
    fun `formats accuracy in meters`() {
        assertEquals("±4.8 м", formatAccuracy(4.84f))
    }

    @Test
    fun `formats missing accuracy`() {
        assertEquals("—", formatAccuracy(null))
    }

    @Test
    fun `formats satellite summary`() {
        assertEquals("8 / 18", formatSatelliteSummary(used = 8, visible = 18))
    }

    @Test
    fun `formats satellite id as digits only`() {
        assertEquals("12", formatSatelliteId(12))
    }

    @Test
    fun `formats satellite system as owner flag`() {
        assertEquals("🇺🇸", formatSatelliteSystemFlag(SatelliteSystem.Gps))
        assertEquals("🇷🇺", formatSatelliteSystemFlag(SatelliteSystem.Glonass))
        assertEquals("🇪🇺", formatSatelliteSystemFlag(SatelliteSystem.Galileo))
        assertEquals("🇨🇳", formatSatelliteSystemFlag(SatelliteSystem.Beidou))
        assertEquals("•", formatSatelliteSystemFlag(SatelliteSystem.Unknown))
    }

    @Test
    fun `maps cn0 to bounded signal fraction`() {
        assertEquals(0f, signalFraction(-5f))
        assertEquals(0.5f, signalFraction(30f))
        assertEquals(1f, signalFraction(65f))
    }

    @Test
    fun `classifies satellite signal quality`() {
        assertEquals(SignalQuality.Weak, classifySignalQuality(cn0DbHz = 18f, usedInFix = false))
        assertEquals(SignalQuality.Usable, classifySignalQuality(cn0DbHz = 28f, usedInFix = false))
        assertEquals(SignalQuality.Usable, classifySignalQuality(cn0DbHz = 32f, usedInFix = true))
        assertEquals(SignalQuality.GoodUsed, classifySignalQuality(cn0DbHz = 36f, usedInFix = true))
    }

    @Test
    fun `averages signal level by used satellites first`() {
        val state = GnssUiState(
            satellites = listOf(
                SatelliteInfo(1, SatelliteSystem.Gps, 0f, false),
                SatelliteInfo(2, SatelliteSystem.Gps, 10f, false),
                SatelliteInfo(3, SatelliteSystem.Gps, 30f, true),
                SatelliteInfo(4, SatelliteSystem.Gps, 40f, true),
            )
        )

        assertEquals(35f, state.averageSignalLevelDbHz)
    }

    @Test
    fun `does not average signal level when no satellites are used`() {
        val state = GnssUiState(
            satellites = listOf(
                SatelliteInfo(1, SatelliteSystem.Gps, 0f, false),
                SatelliteInfo(2, SatelliteSystem.Gps, 20f, false),
                SatelliteInfo(3, SatelliteSystem.Gps, 30f, false),
            )
        )

        assertEquals(null, state.averageSignalLevelDbHz)
    }

}
