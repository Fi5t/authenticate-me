package com.redmadrobot.authenticateme.unauthorized_zone.login.pin.create

import android.app.Application
import android.content.Context
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.redmadrobot.authenticateme.StorageKey
import com.redmadrobot.authenticateme.internal.utils.Salt
import com.redmadrobot.authenticateme.internal.utils.Sha256


class CreatePinViewModel(application: Application) : AndroidViewModel(application) {
    val pinIsCreated = MutableLiveData<Boolean>()

    private val preferences by lazy {
        getApplication<Application>().getSharedPreferences("main", Context.MODE_PRIVATE)
    }

    fun savePin(pin: String) {
        val salt = Salt.generate()
        val saltedPin = pin.toByteArray() + salt

        val hashedPin = Sha256.hash(saltedPin)
        val encodedHash = Base64.encodeToString(hashedPin, Base64.DEFAULT)
        val encodedSalt = Base64.encodeToString(salt, Base64.DEFAULT)

        preferences.edit()
            .putString(StorageKey.PIN, encodedHash)
            .putString(StorageKey.SALT, encodedSalt)
            .apply()

        pinIsCreated.value = true
    }
}

