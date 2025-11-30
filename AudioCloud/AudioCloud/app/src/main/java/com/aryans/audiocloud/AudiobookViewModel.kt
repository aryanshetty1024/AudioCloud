package com.aryans.audiocloud

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AudiobookViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentBook = MutableStateFlow<Audiobook?>(null)
    val currentBook: StateFlow<Audiobook?> = _currentBook

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition

    private val _totalDuration = MutableStateFlow(0)
    val totalDuration: StateFlow<Int> = _totalDuration

    private var mediaPlayer: MediaPlayer? = null

    fun getAudioSessionId(): Int {
        return mediaPlayer?.audioSessionId ?: 0
    }

    fun play(audiobook: Audiobook) {
        if (_currentBook.value == audiobook) {
            mediaPlayer?.start()
        } else {
            mediaPlayer?.release()
            _currentBook.value = audiobook
            mediaPlayer = MediaPlayer.create(getApplication(), audiobook.audioResId).apply {
                setOnCompletionListener { stopPlayback() }
            }
            _totalDuration.value = mediaPlayer?.duration ?: 0
            mediaPlayer?.start()
        }

        _isPlaying.value = true
        startTrackingPosition()
    }

    fun pause() {
        mediaPlayer?.pause()
        _isPlaying.value = false
    }

    fun resume() {
        mediaPlayer?.start()
        _isPlaying.value = true
    }

    fun seekTo(progress: Float) {
        val newPosition = (progress * _totalDuration.value).toInt()
        mediaPlayer?.seekTo(newPosition)
        _currentPosition.value = newPosition
    }

    private fun stopPlayback() {
        _isPlaying.value = false
        _currentPosition.value = 0
    }

    private fun startTrackingPosition() {
        viewModelScope.launch {
            while (_isPlaying.value) {
                _currentPosition.value = mediaPlayer?.currentPosition ?: 0
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        mediaPlayer?.release()
        super.onCleared()
    }
}
