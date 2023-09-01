package com.asif.mymedia3.utils

class Utils {

    companion object {
        @kotlin.jvm.JvmField
        var PRESET_FILE_URI_DESCRIPTIONS=  kotlin.arrayOf( // same order as PRESET_FILE_URIS
        "720p H264 video and AAC audio",
        "1080p H265 video and AAC audio",
        "360p H264 video and AAC audio",
        "360p VP8 video and Vorbis audio",
        "4K H264 video and AAC audio (portrait, no B-frames)",
        "8k H265 video and AAC audio",
        "Short 1080p H265 video and AAC audio",
        "Long 180p H264 video and AAC audio",
        "H264 video and AAC audio (portrait, H > W, 0°)",
        "H264 video and AAC audio (portrait, H < W, 90°)",
        "London JPG image (Plays for 5secs at 30fps)",
        "Tokyo JPG image (Portrait, Plays for 5secs at 30fps)",
        "SEF slow motion with 240 fps",
        "480p DASH (non-square pixels)",
        "HDR (HDR10) H265 limited range video (encoding may fail)",
        "HDR (HLG) H265 limited range video (encoding may fail)",
        "720p H264 video with no audio"
        )
    }

}