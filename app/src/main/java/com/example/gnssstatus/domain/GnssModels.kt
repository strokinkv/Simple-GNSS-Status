package com.example.gnssstatus.domain

enum class SatelliteSystem {
    Gps,
    Glonass,
    Galileo,
    Beidou,
    Qzss,
    Sbas,
    Irnss,
    Unknown,
}

enum class SignalQuality {
    Weak,
    Usable,
    GoodUsed,
}

data class SatelliteInfo(
    val svid: Int,
    val system: SatelliteSystem,
    val cn0DbHz: Float,
    val usedInFix: Boolean,
)

data class GnssUiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val horizontalAccuracyMeters: Float? = null,
    val satellites: List<SatelliteInfo> = emptyList(),
    val isLocationEnabled: Boolean = true,
    val errorMessage: String? = null,
) {
    val visibleSatellites: Int
        get() = satellites.size

    val usedSatellites: Int
        get() = satellites.count { it.usedInFix }

    val averageSignalLevelDbHz: Float?
        get() {
            val usedSignals = satellites.filter { it.usedInFix && it.cn0DbHz > 0f }
            return usedSignals.takeIf { it.isNotEmpty() }?.map { it.cn0DbHz }?.average()?.toFloat()
        }
}
