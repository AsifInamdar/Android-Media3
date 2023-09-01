package com.asif.mymedia3.ui

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.asif.mymedia3.R
import com.asif.mymedia3.databinding.ActivityBasicViewsBinding
import com.asif.mymedia3.databinding.ActivityPlayerViewBinding

class PlayerViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoUri = Uri.parse(intent.getStringExtra("videoUri"))

        val player = ExoPlayer.Builder(this).build()

        val mediaItem = MediaItem.fromUri(videoUri)
        // Set the media item to be played.
        player.setMediaItem(mediaItem)
        // Prepare the player.
        player.prepare()
        // Start the playback.
        player.play()

        binding.playerView.player = player

    }
}