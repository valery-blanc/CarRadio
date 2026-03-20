package com.carradio.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.carradio.data.db.FavoriteStation
import com.carradio.player.PlayerState

@Composable
fun RadioTile(
    station: FavoriteStation?,
    isActive: Boolean,
    playerState: PlayerState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isActive && playerState == PlayerState.PLAYING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onTap() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (station == null) {
            EmptyTile()
        } else {
            FilledTile(station = station, isActive = isActive, playerState = playerState)
        }
    }
}

@Composable
private fun EmptyTile() {
    Icon(
        imageVector = Icons.Default.Add,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        modifier = Modifier.size(32.dp)
    )
}

@Composable
private fun FilledTile(
    station: FavoriteStation,
    isActive: Boolean,
    playerState: PlayerState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(56.dp)) {
            if (!station.faviconUrl.isNullOrBlank()) {
                AsyncImage(
                    model = station.faviconUrl,
                    contentDescription = station.name,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = station.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    )
                }
            }

            if (isActive && playerState == PlayerState.PLAYING) {
                PlayingIndicator(
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = station.name,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlayingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = modifier
            .size(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF4CAF50).copy(alpha = alpha))
    )
}
