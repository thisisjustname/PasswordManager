package com.example.passwordmanager

import android.content.Context
import android.net.Uri
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class SecureImageStorage(private val context: Context) {
    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Сохранение массива изображений
    fun saveEncryptedImages(uris: List<Uri>): List<String> {
        return uris.map { uri ->
            val fileName = generateUniqueFileName()
            saveEncryptedImage(uri, fileName)
            fileName
        }
    }

    // Сохранение одного изображения
    private fun saveEncryptedImage(uri: Uri, fileName: String): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, fileName)

        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        encryptedFile.openFileOutput().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        inputStream?.close()

        return fileName
    }

    // Получение массива изображений
    fun getDecryptedImages(fileNames: List<String>): List<File> {
        return fileNames.mapNotNull { fileName ->
            getDecryptedImage(fileName)
        }
    }

    // Получение одного изображения
    fun getDecryptedImage(fileName: String): File? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return null

        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val decryptedFile = File(context.cacheDir, "decrypted_$fileName")
        encryptedFile.openFileInput().use { input ->
            FileOutputStream(decryptedFile).use { output ->
                input.copyTo(output)
            }
        }

        return decryptedFile
    }

    // Генерация уникального имени файла
    private fun generateUniqueFileName(): String {
        return "image_${UUID.randomUUID()}.jpg"
    }

    fun getAllEncryptedFileNames(): List<String> {
        return context.filesDir.listFiles()
            ?.filter { it.name.startsWith("image_") }
            ?.map { it.name }
            ?: emptyList()
    }

    // Удаление зашифрованного изображения
    fun deleteEncryptedImage(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return file.delete()
    }
}