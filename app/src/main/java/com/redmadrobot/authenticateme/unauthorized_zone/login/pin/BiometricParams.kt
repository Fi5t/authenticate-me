package com.redmadrobot.authenticateme.unauthorized_zone.login.pin

import androidx.biometric.BiometricPrompt

data class BiometricParams(
    val promptInfo: BiometricPrompt.PromptInfo,
    val cryptoObject: BiometricPrompt.CryptoObject
)
