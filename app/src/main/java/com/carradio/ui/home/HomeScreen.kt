@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.carradio.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val currentStation by viewModel.currentStation.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 })

    // Build a 16-slot grid: nulls for empty slots
    val slots: List<FavoriteStation?> = remember(favorites) {
        val map = favorites.associateBy { it.position }
        (0 until 16).map { map[it] }
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
                repeat(2) { index ->
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
