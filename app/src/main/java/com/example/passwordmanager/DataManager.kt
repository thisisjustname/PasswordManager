package com.example.passwordmanager

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class DataManager(context: Context) {
    private val gson = Gson()
    private val masterKeyAlias = "master_key"
    private val sharedPrefsFile = "secure_prefs"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        sharedPrefsFile,
        getMasterKey(context),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun getMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context, masterKeyAlias)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        return if (keyStore.containsAlias(masterKeyAlias)) {
            keyStore.getKey(masterKeyAlias, null) as SecretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(masterKeyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    fun <T> saveEncrypted(key: String, value: T) {
        val jsonString = gson.toJson(value)
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(jsonString.toByteArray(Charsets.UTF_8))
        val combined = cipher.iv + encrypted
        val base64Encoded = Base64.encodeToString(combined, Base64.DEFAULT)
        encryptedSharedPreferences.edit().putString(key, base64Encoded).apply()
    }

    fun <T> getDecrypted(key: String, clazz: Class<T>): T? {
        val base64Encoded = encryptedSharedPreferences.getString(key, null) ?: return null
        val combined = Base64.decode(base64Encoded, Base64.DEFAULT)
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val ivSize = 12
        val iv = combined.slice(0 until ivSize).toByteArray()
        val encrypted = combined.slice(ivSize until combined.size).toByteArray()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val decrypted = cipher.doFinal(encrypted)
        val jsonString = String(decrypted, Charsets.UTF_8)
        return gson.fromJson(jsonString, clazz)
    }
}