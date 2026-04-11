package com.example.bookkeeping.data.backup

import android.util.Base64
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8
import org.json.JSONObject

object BackupCrypto {

    private const val PBKDF2_ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12

    fun encrypt(plainText: String, password: String): String {
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val secretKey = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        val cipherBytes = cipher.doFinal(plainText.toByteArray(UTF_8))

        return JSONObject()
            .put("format", "bookkeeping-backup-v1")
            .put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            .put("payload", Base64.encodeToString(cipherBytes, Base64.NO_WRAP))
            .toString()
    }

    fun decrypt(encryptedText: String, password: String): String {
        val root = JSONObject(encryptedText)
        val salt = Base64.decode(root.getString("salt"), Base64.NO_WRAP)
        val iv = Base64.decode(root.getString("iv"), Base64.NO_WRAP)
        val payload = Base64.decode(root.getString("payload"), Base64.NO_WRAP)
        val secretKey = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        return cipher.doFinal(payload).toString(UTF_8)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val secret = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec)
        return SecretKeySpec(secret.encoded, "AES")
    }
}
