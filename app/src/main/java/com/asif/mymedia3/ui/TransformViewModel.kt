package com.asif.mymedia3.ui

import android.content.Context
import android.graphics.Matrix
import android.text.SpannableString
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop
import androidx.media3.effect.MatrixTransformation
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.OverlaySettings
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.effect.TextOverlay
import androidx.media3.effect.TextureOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.Transformer.PROGRESS_STATE_NOT_STARTED
import com.asif.mymedia3.utils.getFilePath
import com.asif.mymedia3.utils.logE
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min


@UnstableApi
class TransformViewModel : ViewModel() {

    var transformerProgress = MutableSharedFlow<Int>()
    var videoPath: MutableLiveData<String> = MutableLiveData<String>()
    var isEditCompleted: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    fun removeAudio(inputPath: String, context: Context) {

        val outputPath = getFilePath(context)!!

        val inputMediaItem = MediaItem.fromUri(inputPath)
        val editedMediaItem =
            EditedMediaItem.Builder(inputMediaItem).setRemoveAudio(true).build()
        val transformer = Transformer.Builder(context)
            .setTransformationRequest(
                TransformationRequest.Builder().setVideoMimeType(MimeTypes.VIDEO_H265).build()
            )
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItem, outputPath)

        viewModelScope.launch {
            setProgress(transformer)
        }
    }

    fun removeVideo(inputPath: String, context: Context) {

        val outputPath = getFilePath(context)!!

        val inputMediaItem = MediaItem.fromUri(inputPath)
        val editedMediaItem =
            EditedMediaItem.Builder(inputMediaItem).setRemoveVideo(true).build()
        val transformer = Transformer.Builder(context)
            .setTransformationRequest(
                TransformationRequest.Builder().setVideoMimeType(MimeTypes.VIDEO_H265).build()
            )
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItem, outputPath)

        viewModelScope.launch {
            setProgress(transformer)
        }
    }


    fun zoomInVideo(inputPath: String, context: Context) {
        val zoomOutEffect = MatrixTransformation { presentationTimeUs ->
            val transformationMatrix = Matrix()
            val scale = 2 - min(
                1f,
                presentationTimeUs / 3_000_000f
            ) // Video will zoom from 2x to 1x in the first second
            transformationMatrix.postScale(/* sx= */ scale, /* sy= */ scale)
            logE("ZoomVideo", "scale $scale")
            transformationMatrix // The calculated transformations will be applied each frame in turn
        }

        val videoEffects = mutableListOf<Effect>()
        videoEffects.add(zoomOutEffect)

        setMedia(inputPath, context, videoEffects)
    }

    fun zoomOutVideo(inputPath: String, context: Context) {
        val zoomInEffect = MatrixTransformation { presentationTimeUs ->
            val transformationMatrix = Matrix()
            val scale = 2 - max(
                1f,
                presentationTimeUs / 3_000_000f
            )
            transformationMatrix.postScale(/* sx= */ scale, /* sy= */ scale)
            logE("ZoomVideo", "scale $scale")
            transformationMatrix // The calculated transformations will be applied each frame in turn
        }

        val videoEffects = mutableListOf<Effect>()
        videoEffects.add(zoomInEffect)

        setMedia(inputPath, context, videoEffects)
    }

    fun rotateVideo(inputPath: String, context: Context) {

        val rotate = ScaleAndRotateTransformation.Builder()
            .setRotationDegrees(90f).build()

        val videoEffects = mutableListOf<Effect>()
        videoEffects.add(rotate)

        setMedia(inputPath, context, videoEffects)
    }

    fun trimVideo(inputPath: String, context: Context) {
        val inputMediaItem = MediaItem.Builder()
            .setUri(inputPath)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(10_000)
                    .setEndPositionMs(20_000)
                    .build()
            )
            .build()

        val outputPath = setOutputVideoPath(context)

        val editedMediaItem =
            EditedMediaItem.Builder(inputMediaItem)
                .build()
        val transformer = Transformer.Builder(context)
            .setTransformationRequest(
                TransformationRequest.Builder().setVideoMimeType(MimeTypes.VIDEO_H265).build()
            )
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItem, outputPath)

    }

    fun flipVideo(inputPath: String, context: Context) {
        val flipEffect = MatrixTransformation { presentationTimeUs ->
            val transformationMatrix = Matrix()
            transformationMatrix.postScale(/* sx= */ -1f, /* sy= */ 1f)
            transformationMatrix // The calculated transformations will be applied each frame in turn
        }

        val videoEffects = mutableListOf<Effect>()
        videoEffects.add(flipEffect)

        setMedia(inputPath, context, videoEffects)
    }

    fun reverseVideo(inputPath: String, context: Context) {
        val flipEffect = MatrixTransformation { presentationTimeUs ->
            val transformationMatrix = Matrix()
            transformationMatrix.preScale(/* sx= */ -1f, /* sy= */ 1f)
            transformationMatrix // The calculated transformations will be applied each frame in turn
        }

        val videoEffects = mutableListOf<Effect>()
        videoEffects.add(flipEffect)

        setMedia(inputPath, context, videoEffects)
    }

    fun cropVideo(inputPath: String, context: Context) {

        val cropEffect = Crop(-1f, 0.3f, -1f, 0.3f)

        val videoEffects = mutableListOf<Effect>()
        videoEffects.add(cropEffect)

        setMedia(inputPath, context, videoEffects)

        /*val outputPath = getFilePath(context)!!

        val inputMediaItem = MediaItem.fromUri(inputPath)
        val editedMediaItem =
            EditedMediaItem.Builder(inputMediaItem).setEffects(Effects(emptyList(), videoEffects))
                .build()
        val transformer = Transformer.Builder(context)
            .setTransformationRequest(
                TransformationRequest.Builder().setVideoMimeType(MimeTypes.VIDEO_H265).build()
            )
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItem, outputPath)*/
    }

    private suspend fun setProgress(transformer: Transformer) {
        val progressHolder = ProgressHolder()

        if (transformer.getProgress(progressHolder) != PROGRESS_STATE_NOT_STARTED) {
            transformerProgress.emit(progressHolder.progress)
        }
    }

    private val transformerListener: Transformer.Listener =
        object : Transformer.Listener {
            override fun onCompleted(composition: Composition, result: ExportResult) {
                // playOutput()
                Log.e("transform", "completed $result")
                isEditCompleted.value = true
            }

            override fun onError(
                composition: Composition, result: ExportResult,
                exception: ExportException
            ) {
                //displayError(exception)
                isEditCompleted.value = false
                exception.printStackTrace()
                Log.e("error", "exception ${exception.errorCode} ${exception.errorCodeName}")
            }
        }

    private fun setOutputVideoPath(context: Context): String {

        val outputPath = getFilePath(context)!!

        videoPath.value = outputPath

        return outputPath
    }

    private fun setMedia(inputPath: String, context: Context, effects: MutableList<Effect>) {

        val outputPath = setOutputVideoPath(context)

        val inputMediaItem = MediaItem.fromUri(inputPath)
        val editedMediaItem =
            EditedMediaItem.Builder(inputMediaItem).setEffects(Effects(emptyList(), effects))
                .build()
        val transformer = Transformer.Builder(context)
            .setTransformationRequest(
                TransformationRequest.Builder().setVideoMimeType(MimeTypes.VIDEO_H265).build()
            )
            .addListener(transformerListener)
            .build()
        transformer.start(editedMediaItem, outputPath)
    }

    fun addEmoji(inputPath: String, context: Context){

        val videoEffects = mutableListOf<Effect>()
        videoEffects.add(createVideoEffects())

        setMedia(inputPath, context, videoEffects)

    }

    fun createVideoEffects(): Effect {
        return createOverlayEffect()!!
    }

    @OptIn(UnstableApi::class)
    private fun createOverlayEffect(): OverlayEffect? {
        val overLaysBuilder: ImmutableList.Builder<TextureOverlay> = ImmutableList.builder()

        val getEmoji = "\uD83D\uDE19"
        val overlayEmoji = SpannableString(getEmoji)

        val emojiTextureOverlay: TextureOverlay =
            TextOverlay.createStaticTextOverlay(overlayEmoji)
        overLaysBuilder.add(emojiTextureOverlay)

        val overlays: ImmutableList<TextureOverlay> = overLaysBuilder.build()
        return if (overlays.isEmpty()) null else OverlayEffect(overlays)
    }

}