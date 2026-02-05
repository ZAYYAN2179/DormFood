package com.zayyan.dormfood.ui.utils

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryConfig {

    private var isInit = false

    fun init(context: Context) {
        if (isInit) return

        val config = mapOf(
            "cloud_name" to "dpuasrpfm"
        )

        MediaManager.init(context, config)
        isInit = true
    }
}
