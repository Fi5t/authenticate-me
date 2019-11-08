package com.redmadrobot.authenticateme.unauthorized_zone.login.pin.create

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.redmadrobot.authenticateme.StorageKey


class CreatePinViewModel(application: Application) : AndroidViewModel(application) {
    val pinIsCreated = MutableLiveData<Boolean>()

    private val preferences by lazy {
        getApplication<Application>().getSharedPreferences("main", Context.MODE_PRIVATE)
    }

    fun savePin(pin: String) {
        preferences.edit().putString(StorageKey.PIN, pin).apply()
        pinIsCreated.value = true
    }
}

