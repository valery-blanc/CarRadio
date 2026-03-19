package com.carradio.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carradio.domain.model.RadioStation
import com.carradio.player.PlayerState

@Composable
fun NowPlayingBar(
    station: RadioStation?,
    playerState: PlayerState,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (station == null) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPlayPause) {
                when (playerState) {
                    PlayerState.PLAYING -> Icon(
                        Icons.Default.PauseCircle, contentDescription = "Pause",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    PlayerState.LOADING -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    else -> Icon(
                        Icons.Default.PlayCircle, contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (playerState) {
                        PlayerState.LOADING -> "Chargement…"
                        PlayerState.PLAYING -> "En direct"
                        PlayerState.PAUSED -> "En pause"
                        PlayerState.ERROR -> "Erreur de connexion"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
