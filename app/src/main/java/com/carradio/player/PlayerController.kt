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
    // Nullable backing field so stop() can skip initialization when never used
    private var playerInstance: ExoPlayer? = null

    val player: ExoPlayer
        get() = playerInstance ?: ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .build()
            .also {
                it.addListener(playerListener)
                playerInstance = it
            }

    val isPlayerInitialized: Boolean get() = playerInstance != null

    private val _state = MutableStateFlow(PlayerState.IDLE)
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation.asStateFlow()

    // onEvents() fires once after all pending events are dispatched — eliminates
    // the race condition between onPlaybackStateChanged and onIsPlayingChanged.
    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED
                )
            ) {
                _state.value = when {
                    player.playbackState == Player.STATE_BUFFERING -> PlayerState.LOADING
                    player.isPlaying -> PlayerState.PLAYING
                    player.playbackState == Player.STATE_READY -> PlayerState.PAUSED
                    else -> PlayerState.IDLE
                }
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

        try {
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
        } catch (_: Exception) {
            _state.value = PlayerState.ERROR
        }
    }

    fun pause() {
        try { playerInstance?.pause() } catch (_: Exception) { }
    }

    fun resume() {
        try { playerInstance?.play() } catch (_: Exception) { }
    }

    fun stop() {
        val p = playerInstance ?: return // Never initialized — nothing to stop
        try {
            p.stop()
            p.clearMediaItems()
        } catch (_: Exception) { }
        _state.value = PlayerState.IDLE
        _currentStation.value = null
        // BUG-016 : arrêter le service quand la lecture s'arrête pour éviter qu'Android
        // le détruise à l'improviste (ce qui libérait le player singleton et causait un
        // audio focus leak lors de la prochaine lecture)
        context.stopService(Intent(context, RadioPlayerService::class.java))
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
