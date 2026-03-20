@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.carradio.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carradio.data.db.FavoriteStation
import com.carradio.player.PlayerState

private fun Int.formatAsTimer(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToTimer: () -> Unit,
    isTimerRunning: Boolean = false,
    remainingSeconds: Int? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 4 })

    // Build a 32-slot grid: nulls for empty slots
    val slots: List<FavoriteStation?> = remember(favorites) {
        val map = favorites.associateBy { it.position }
        (0 until 32).map { map[it] }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CarRadio") },
                actions = {
                    if (currentStation != null) {
                        IconButton(onClick = { viewModel.stopPlayback() }) {
                            Icon(Icons.Default.StopCircle, contentDescription = "Stop")
                        }
                    }
                    // Sleep timer countdown
                    if (isTimerRunning && remainingSeconds != null) {
                        Row(
                            modifier = Modifier
                                .clickable { onNavigateToTimer() }
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Bedtime,
                                contentDescription = "Minuteur actif",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = remainingSeconds.formatAsTimer(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    // Hourglass / timer icon
                    IconButton(onClick = onNavigateToTimer) {
                        Icon(
                            if (isTimerRunning) Icons.Default.HourglassTop else Icons.Default.HourglassEmpty,
                            contentDescription = "Minuteur"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                }
            )
        },
        bottomBar = {
            NowPlayingBar(
                station = currentStation,
                playerState = playerState,
                onPlayPause = {
                    val station = currentStation ?: return@NowPlayingBar
                    viewModel.onTileTapped(station)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val pageSlots = slots.subList(page * 8, page * 8 + 8)
                TileGrid(
                    slots = pageSlots,
                    pageOffset = page * 8,
                    currentStationUuid = currentStation?.uuid,
                    playerState = playerState,
                    onTileTap = { position ->
                        val station = slots[position]?.toDomain()
                        if (station != null) {
                            viewModel.onTileTapped(station)
                        } else {
                            onNavigateToSettings()
                        }
                    }
                )
            }

            // Page indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(4) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = MaterialTheme.shapes.small,
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun TileGrid(
    slots: List<FavoriteStation?>,
    pageOffset: Int,
    currentStationUuid: String?,
    playerState: PlayerState,
    onTileTap: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in 0 until 4) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until 2) {
                    val slotIndex = row * 2 + col
                    val globalPosition = pageOffset + slotIndex
                    val station = slots.getOrNull(slotIndex)
                    RadioTile(
                        station = station,
                        isActive = station != null && station.uuid == currentStationUuid,
                        playerState = playerState,
                        onTap = { onTileTap(globalPosition) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
