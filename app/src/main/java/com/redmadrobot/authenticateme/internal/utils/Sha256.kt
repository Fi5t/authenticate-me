package com.redmadrobot.authenticateme.internal.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Sha256 {
    fun hash(byteArray: ByteArray): ByteArray {
        val digest = try {
            MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            MessageDigest.getInstance("SHA")
        }

        return with(digest) {
            update(byteArray)
            digest()
        }
    }
}
