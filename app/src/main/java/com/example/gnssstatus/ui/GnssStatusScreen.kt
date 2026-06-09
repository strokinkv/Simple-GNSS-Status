package com.example.gnssstatus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gnssstatus.domain.GnssUiState
import com.example.gnssstatus.domain.SatelliteInfo
import com.example.gnssstatus.domain.SignalQuality
import com.example.gnssstatus.domain.classifySignalQuality
import com.example.gnssstatus.domain.formatAccuracy
import com.example.gnssstatus.domain.formatCn0
import com.example.gnssstatus.domain.formatCoordinates
import com.example.gnssstatus.domain.formatSatelliteId
import com.example.gnssstatus.domain.formatSatelliteSystemFlag
import com.example.gnssstatus.domain.formatSatelliteSummary
import com.example.gnssstatus.domain.signalFraction

private const val IdColumnFraction = 0.15f
private const val FlagColumnFraction = 0.15f
private const val UsedColumnFraction = 0.15f
private const val SignalColumnFraction = 0.55f

@Composable
fun GnssStatusScreen(
    state: GnssUiState,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = GnssColors.AppBackground,
    ) {
        if (!hasPermission) {
            PermissionState(onRequestPermission = onRequestPermission)
        } else {
            MainContent(state = state)
        }
    }
}

@Composable
private fun PermissionState(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Нужно разрешение на точную геолокацию",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = GnssColors.PrimaryText,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Оно нужно для чтения GNSS-статуса, координат и списка спутников.",
            style = MaterialTheme.typography.bodyMedium,
            color = GnssColors.SecondaryText,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onRequestPermission) {
            Text("Разрешить")
        }
    }
}

@Composable
private fun MainContent(state: GnssUiState) {
    val displayedSatellites = state.satellites.filter { it.cn0DbHz > 0f }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Header(state = state)
        }

        item {
            SatelliteTableHeader()
        }

        state.errorMessage?.let { message ->
            item {
                StatusMessage(message)
            }
        }

        if (!state.isLocationEnabled) {
            item {
                StatusMessage("Геолокация выключена в системе")
            }
        }

        if (state.satellites.isEmpty() && state.errorMessage == null && state.isLocationEnabled) {
            item {
                StatusMessage("Ожидание спутников")
            }
        }

        items(displayedSatellites) { satellite ->
            SatelliteRow(satellite = satellite)
        }
    }
}

@Composable
private fun Header(state: GnssUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GnssColors.CardBackground)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Metric(
                label = "Координаты",
                value = formatCoordinates(state.latitude, state.longitude),
                modifier = Modifier.weight(1f),
            )
            Metric(
                label = "Точность",
                value = formatAccuracy(state.horizontalAccuracyMeters),
                modifier = Modifier.width(112.dp),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Metric(
                label = "Средний",
                value = formatCn0(state.averageSignalLevelDbHz),
                modifier = Modifier.weight(1f),
            )
            Metric(
                label = "Спутники",
                value = formatSatelliteSummary(state.usedSatellites, state.visibleSatellites),
                modifier = Modifier.width(112.dp),
            )
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(GnssColors.MetricBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = GnssColors.SecondaryText)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = GnssColors.PrimaryText,
        )
    }
}

@Composable
private fun SatelliteTableHeader() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GnssColors.TableHeaderBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        val idWidth = maxWidth * IdColumnFraction
        val flagWidth = maxWidth * FlagColumnFraction
        val usedWidth = maxWidth * UsedColumnFraction
        val signalWidth = maxWidth * SignalColumnFraction
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TableCell("ID", idWidth, header = true)
            TableCell("Флаг", flagWidth, header = true)
            TableCell("Исп.", usedWidth, header = true)
            Text(
                text = "Уровень сигнала",
                modifier = Modifier.width(signalWidth),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = GnssColors.HeaderText,
            )
        }
    }
}

@Composable
private fun SatelliteRow(satellite: SatelliteInfo) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(GnssColors.TableRowBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        val idWidth = maxWidth * IdColumnFraction
        val flagWidth = maxWidth * FlagColumnFraction
        val usedWidth = maxWidth * UsedColumnFraction
        val signalWidth = maxWidth * SignalColumnFraction
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TableCell(formatSatelliteId(satellite.svid), idWidth, bold = satellite.usedInFix)
            Text(
                text = formatSatelliteSystemFlag(satellite.system),
                modifier = Modifier.width(flagWidth),
                style = MaterialTheme.typography.bodyMedium,
                color = GnssColors.PrimaryText,
            )
            TableCell(
                text = if (satellite.usedInFix) "Да" else "Нет",
                width = usedWidth,
                color = if (satellite.usedInFix) GnssColors.Good else GnssColors.Warning,
            )
            Row(
                modifier = Modifier.width(signalWidth),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TableCell(satellite.cn0DbHz.toInt().toString(), 32.dp, bold = true)
                LinearProgressIndicator(
                    progress = { signalFraction(satellite.cn0DbHz) },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = signalQualityColor(classifySignalQuality(satellite.cn0DbHz, satellite.usedInFix)),
                    trackColor = GnssColors.Track,
                    drawStopIndicator = {},
                )
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    width: Dp,
    header: Boolean = false,
    bold: Boolean = false,
    color: Color = GnssColors.PrimaryText,
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        style = if (header) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
        fontWeight = if (header || bold) FontWeight.Bold else FontWeight.Normal,
        color = if (header) GnssColors.HeaderText else color,
    )
}

@Composable
private fun StatusMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GnssColors.CardBackground)
            .padding(16.dp),
    ) {
        Text(text = text, color = GnssColors.SecondaryText)
    }
}

private fun signalQualityColor(quality: SignalQuality): Color = when (quality) {
    SignalQuality.Weak -> GnssColors.Weak
    SignalQuality.Usable -> GnssColors.Warning
    SignalQuality.GoodUsed -> GnssColors.Good
}
