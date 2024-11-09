package com.example.pythoneditor.service

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import com.example.pythoneditor.model.VersionInfo

class UpdateChecker(private val context: Context) {
    private val currentVersionCode: Int
        get() = try {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }

    suspend fun checkUpdate(updateUrl: String): VersionInfo? = withContext(Dispatchers.IO) {
        try {
            val response = URL(updateUrl).readText()
            val json = JSONObject(response)
            
            val serverVersionCode = json.getInt("versionCode")
            if (serverVersionCode > currentVersionCode) {
                VersionInfo(
                    versionCode = serverVersionCode,
                    versionName = json.getString("versionName"),
                    updateMessage = json.getString("updateMessage"),
                    downloadUrl = json.getString("downloadUrl"),
                    forceUpdate = json.getBoolean("forceUpdate")
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
} 