package com.example.pythoneditor.model

data class VersionInfo(
    val versionCode: Int,
    val versionName: String,
    val updateMessage: String,
    val downloadUrl: String,
    val forceUpdate: Boolean
) 