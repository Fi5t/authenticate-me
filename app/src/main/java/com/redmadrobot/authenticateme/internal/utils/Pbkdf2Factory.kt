package com.redmadrobot.authenticateme.internal.utils

import android.os.Build
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Pbkdf2Factory {
    private const val DEFAULT_ITERATIONS = 10000
    private const val DEFAULT_KEY_LENGTH = 256

    private val secretKeyFactory by lazy {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            SecretKeyFactory.getInstance("PBKDF2withHmacSHA1")
        } else {
            SecretKeyFactory.getInstance("PBKDF2withHmacSHA256")
        }
    }

    fun createKey(
        passphraseOrPin: CharArray,
        salt: ByteArray,
        iterations: Int = DEFAULT_ITERATIONS,
        outputKeyLength: Int = DEFAULT_KEY_LENGTH
    ): SecretKey {
        val keySpec = PBEKeySpec(passphraseOrPin, salt, iterations, outputKeyLength)
        return secretKeyFactory.generateSecret(keySpec)
    }
}
