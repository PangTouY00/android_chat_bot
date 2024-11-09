package com.example.pythoneditor.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.pythoneditor.model.VersionInfo

class UpdateDialog(private val context: Context) {
    fun show(versionInfo: VersionInfo) {
        val dialog = AlertDialog.Builder(context)
            .setTitle("发现新版本 ${versionInfo.versionName}")
            .setMessage(versionInfo.updateMessage)
            .setPositiveButton("立即更新") { _, _ ->
                openDownloadUrl(versionInfo.downloadUrl)
            }

        if (!versionInfo.forceUpdate) {
            dialog.setNegativeButton("稍后再说", null)
        }

        dialog.setCancelable(!versionInfo.forceUpdate)
        dialog.show()
    }

    private fun openDownloadUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
} 