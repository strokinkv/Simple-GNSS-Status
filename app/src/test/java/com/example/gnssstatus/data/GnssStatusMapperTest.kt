package com.example.gnssstatus.data

import android.location.GnssStatus
import com.example.gnssstatus.domain.SatelliteSystem
import org.junit.Assert.assertEquals
import org.junit.Test

class GnssStatusMapperTest {
    @Test
    fun `maps known constellation types`() {
        assertEquals(SatelliteSystem.Gps, mapConstellationType(GnssStatus.CONSTELLATION_GPS))
        assertEquals(SatelliteSystem.Glonass, mapConstellationType(GnssStatus.CONSTELLATION_GLONASS))
        assertEquals(SatelliteSystem.Galileo, mapConstellationType(GnssStatus.CONSTELLATION_GALILEO))
        assertEquals(SatelliteSystem.Beidou, mapConstellationType(GnssStatus.CONSTELLATION_BEIDOU))
        assertEquals(SatelliteSystem.Qzss, mapConstellationType(GnssStatus.CONSTELLATION_QZSS))
        assertEquals(SatelliteSystem.Sbas, mapConstellationType(GnssStatus.CONSTELLATION_SBAS))
        assertEquals(SatelliteSystem.Irnss, mapConstellationType(GnssStatus.CONSTELLATION_IRNSS))
    }

    @Test
    fun `maps unknown constellation type`() {
        assertEquals(SatelliteSystem.Unknown, mapConstellationType(-1))
    }
}
