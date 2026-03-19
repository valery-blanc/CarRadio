package com.carradio.player

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.carradio.domain.model.RadioStation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .build()
            .also { it.addListener(playerListener) }
    }

    private val _state = MutableStateFlow(PlayerState.IDLE)
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            _state.value = when (playbackState) {
                Player.STATE_BUFFERING -> PlayerState.LOADING
                Player.STATE_READY -> if (player.isPlaying) PlayerState.PLAYING else PlayerState.PAUSED
                Player.STATE_IDLE, Player.STATE_ENDED -> PlayerState.IDLE
                else -> PlayerState.IDLE
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (_state.value != PlayerState.LOADING) {
                _state.value = if (isPlaying) PlayerState.PLAYING else PlayerState.PAUSED
            }
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            _state.value = PlayerState.ERROR
        }
    }

    fun play(station: RadioStation) {
        _currentStation.value = station

        // Start the foreground service only when actually playing
        val intent = Intent(context, RadioPlayerService::class.java)
        context.startForegroundService(intent)

        val mediaItem = MediaItem.Builder()
            .setUri(station.streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(station.name)
                    .setArtworkUri(station.faviconUrl?.toUri())
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _state.value = PlayerState.LOADING
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
        _state.value = PlayerState.IDLE
        _currentStation.value = null
    }

    fun togglePlayPause(station: RadioStation) {
        val current = _currentStation.value
        when {
            current?.uuid == station.uuid && _state.value == PlayerState.PLAYING -> pause()
            current?.uuid == station.uuid && _state.value == PlayerState.PAUSED -> resume()
            else -> play(station)
        }
    }
}
