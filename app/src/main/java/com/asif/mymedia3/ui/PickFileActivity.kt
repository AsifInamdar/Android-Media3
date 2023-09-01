package com.asif.mymedia3.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.exoplayer.ExoPlayer
import com.asif.mymedia3.databinding.ActivityPickFileBinding
import com.asif.mymedia3.utils.getFilePath
import com.asif.mymedia3.utils.logE
import kotlinx.coroutines.launch

@UnstableApi
class PickFileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPickFileBinding
    private lateinit var outputPath: String
    private lateinit var inputPath: String
    private val transformViewModel: TransformViewModel by viewModels()
    private lateinit var player: ExoPlayer
    private var rotation = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickFileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = ExoPlayer.Builder(this).build()

        binding.apply {
            selectLocalFileButton.setOnClickListener {
                openGallery()
            }

            btnZoomIn.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                transformViewModel.zoomInVideo(
                    inputPath,
                    this@PickFileActivity
                )
            }

            btnZoomOut.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                transformViewModel.zoomOutVideo(
                    inputPath,
                    this@PickFileActivity
                )
            }

            btnReverse.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                transformViewModel.reverseVideo(
                    inputPath,
                    this@PickFileActivity
                )
            }

            btnRotate.setOnClickListener {
                //progressBar.visibility = View.VISIBLE
                /*transformViewModel.rotateVideo(
                    inputPath,
                    this@PickFileActivity
                )*/

                rotation += 90f
                playerView.rotation = rotation
            }

            btnCrop.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                transformViewModel.cropVideo(
                    inputPath,
                    this@PickFileActivity
                )
            }

            btnFlip.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                transformViewModel.flipVideo(
                    inputPath,
                    this@PickFileActivity
                )
            }

            btnTrim.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                transformViewModel.trimVideo(inputPath, this@PickFileActivity)
                //transformViewModel.addEmoji(inputPath, this@PickFileActivity)
            }
        }

        lifecycleScope.launch {
            transformViewModel.transformerProgress.collect {
                logE("Transform progress", "value : $it")
            }
        }

        transformViewModel.isEditCompleted.observe(this) {
            binding.progressBar.visibility = View.GONE
            if (it) {
                showVideo(Uri.parse(transformViewModel.videoPath.value))
                playVideo()
            } else {
                Toast.makeText(this, "Processing failed.", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        resultLauncher.launch(intent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data

                data?.data?.also { uri ->
                    inputPath = uri.toString()
                    outputPath = getFilePath(this)!!

                    showVideo(uri)
                }
            }
        }

    private fun showVideo(uri: Uri) {

        binding.playerView.visibility = View.VISIBLE

        val mediaItem = MediaItem.fromUri(uri)
        // Set the media item to be played.
        player.setMediaItem(mediaItem)
        // Prepare the player.
        player.prepare()

        //player.setVideoEffects(mutableListOf(transformViewModel.createVideoEffects()))

        binding.playerView.player = player
    }

    private fun playVideo() {
        player.play()
    }
}