package com.redmadrobot.authenticateme.unauthorized_zone.login.pin.create

import android.app.Application
import android.util.Base64
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.redmadrobot.authenticateme.App
import com.redmadrobot.authenticateme.StorageKey
import com.redmadrobot.authenticateme.internal.utils.Pbkdf2Factory
import com.redmadrobot.authenticateme.internal.utils.Salt


class CreatePinViewModel(application: Application) : AndroidViewModel(application) {
    val pinIsCreated = MutableLiveData<Boolean>()

    private val fakeAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZWNyZXQiOiJXZSdyZSBoaXJpbmcgOykifQ.WZrEWG-l3VsJzJrbnjn2BIYO68gHIGyat6jrw7Iu-Rw"

    private val preferences by lazy { getApplication<App>().encryptedStorage }

    private val aead by lazy { getApplication<App>().pinSecuredAead }

    fun savePin(pin: String) {
        val salt = Salt.generate()
        val secretKey = Pbkdf2Factory.createKey(pin.toCharArray(), salt)

        val encryptedToken = aead.encrypt(fakeAccessToken.toByteArray(), secretKey.encoded)

        preferences.edit {
            putString(StorageKey.TOKEN, Base64.encodeToString(encryptedToken, Base64.DEFAULT))
            putString(StorageKey.SALT, Base64.encodeToString(salt, Base64.DEFAULT))
            putBoolean(StorageKey.PIN_IS_ENABLED, true)
        }

        pinIsCreated.value = true

    }
}
