package com.bebsoft.yatirimtakip.helper

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.bebsoft.yatirimtakip.R
import java.io.InputStream
import java.util.*

class ConfigHelper {

    companion object {
        fun getConfigValue(context: Context, name: String?): String? {
            val resources: Resources = context.resources

            try {
                val rawResource: InputStream = resources.openRawResource(R.raw.config)
                val properties = Properties()
                properties.load(rawResource)
                return properties.getProperty(name)
            } catch (e: Exception) {
                Log.e("ConfigHelper", "An exception occured while getting config value")
            }

            return null
        }
    }
}