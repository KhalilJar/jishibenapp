package com.example.bookkeeping.data.backup

import android.content.Context
import android.net.Uri
import java.io.IOException

class BackupManager(
    private val context: Context
) {

    fun exportEncrypted(uri: Uri, password: String, snapshot: BackupSnapshot) {
        require(password.isNotBlank()) { "备份密码不能为空" }
        val plainJson = BackupJsonCodec.encode(snapshot)
        val encrypted = BackupCrypto.encrypt(plainJson, password)
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            writer.write(encrypted)
        } ?: throw IOException("无法打开导出文件")
    }

    fun importEncrypted(uri: Uri, password: String): BackupSnapshot {
        require(password.isNotBlank()) { "备份密码不能为空" }
        val encrypted = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
            reader.readText()
        } ?: throw IOException("无法读取备份文件")
        val json = BackupCrypto.decrypt(encrypted, password)
        return BackupJsonCodec.decode(json)
    }
}
