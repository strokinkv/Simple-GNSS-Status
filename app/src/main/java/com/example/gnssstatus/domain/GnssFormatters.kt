package com.example.gnssstatus.domain

import java.util.Locale
import kotlin.math.roundToInt

fun formatCoordinates(latitude: Double?, longitude: Double?): String {
    if (latitude == null || longitude == null) return "Ожидание координат"
    return String.format(Locale.US, "%.6f, %.6f", latitude, longitude)
}

fun formatAccuracy(accuracyMeters: Float?): String {
    if (accuracyMeters == null) return "—"
    return String.format(Locale.US, "±%.1f м", accuracyMeters)
}

fun formatSatelliteSummary(used: Int, visible: Int): String = "$used / $visible"

fun formatSatelliteId(svid: Int): String = svid.toString()

fun formatSatelliteSystemFlag(system: SatelliteSystem): String = when (system) {
    SatelliteSystem.Gps -> "🇺🇸"
    SatelliteSystem.Glonass -> "🇷🇺"
    SatelliteSystem.Galileo -> "🇪🇺"
    SatelliteSystem.Beidou -> "🇨🇳"
    SatelliteSystem.Qzss,
    SatelliteSystem.Sbas,
    SatelliteSystem.Irnss,
    SatelliteSystem.Unknown -> "•"
}

fun formatCn0(cn0DbHz: Float?): String {
    if (cn0DbHz == null) return "—"
    return "${cn0DbHz.roundToInt()} dB-Hz"
}

fun signalFraction(cn0DbHz: Float): Float = (cn0DbHz / 60f).coerceIn(0f, 1f)

fun classifySignalQuality(cn0DbHz: Float, usedInFix: Boolean): SignalQuality = when {
    usedInFix && cn0DbHz >= 35f -> SignalQuality.GoodUsed
    cn0DbHz >= 25f -> SignalQuality.Usable
    else -> SignalQuality.Weak
}
