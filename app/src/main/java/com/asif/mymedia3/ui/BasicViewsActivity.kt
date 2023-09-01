package com.asif.mymedia3.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.asif.mymedia3.databinding.ActivityBasicViewsBinding
import com.asif.mymedia3.utils.getFilePath
import com.asif.mymedia3.utils.logE
import kotlinx.coroutines.launch

@UnstableApi
class BasicViewsActivity : AppCompatActivity() {

    private lateinit var outputPath: String
    private lateinit var inputPath: String

    private lateinit var binding: ActivityBasicViewsBinding
    private val transformViewModel: TransformViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBasicViewsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.apply {
            selectLocalFileButton.setOnClickListener {
                openGallery()
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
                startActivity(Intent(this, PlayerViewActivity::class.java).apply {
                    putExtra(
                        "videoUri",
                        transformViewModel.videoPath.value
                    )
                })
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

                    binding.progressBar.visibility = View.VISIBLE
                    transformViewModel.cropVideo(inputPath, this)
                }
            }
        }

}