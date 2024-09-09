package com.example.passwordmanager.passwords

import kotlin.random.Random

class PasswordGenerator {
    private val lowercaseChars = "abcdefghijklmnopqrstuvwxyz"
    private val uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val numberChars = "0123456789"
    private val specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    fun generatePassword(
        length: Int,
        useSpecialChars: Boolean,
        useNumbers: Boolean,
        useUppercase: Boolean
    ): String {
        var charPool = lowercaseChars
        if (useUppercase) charPool += uppercaseChars
        if (useNumbers) charPool += numberChars
        if (useSpecialChars) charPool += specialChars

        return (1..length)
            .map { Random.nextInt(0, charPool.length) }
            .map(charPool::get)
            .joinToString("")
    }
}