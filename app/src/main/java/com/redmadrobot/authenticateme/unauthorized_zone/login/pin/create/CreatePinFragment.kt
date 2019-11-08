package com.redmadrobot.authenticateme.unauthorized_zone.login.pin.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.redmadrobot.authenticateme.R
import com.redmadrobot.authenticateme.unauthorized_zone.login.pin.BiometricParams
import kotlinx.android.synthetic.main.input_pin_fragment.*
import java.util.concurrent.Executors


class CreatePinFragment : Fragment() {

    private lateinit var viewModel: CreatePinViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_pin_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(CreatePinViewModel::class.java)

        viewModel.pinIsCreated.observe(viewLifecycleOwner, Observer { isCreated ->
            findNavController().navigate(R.id.inputPinFragment)
        })

        viewModel.biometricEnableDialog.observe(viewLifecycleOwner, Observer { secretKey ->
            secretKey.getContentIfNotHandled()?.let { showBiometricEnableDialog() }
        })

        viewModel.biometricParams.observe(viewLifecycleOwner, Observer { params ->
            showBiometricPrompt(params)
        })

        hidden_pin.onFilledListener = { viewModel.savePin(it) }
        keyboard.keyboardClickListener = { hidden_pin.add(it) }
    }

    private fun showBiometricPrompt(params: BiometricParams) {
        val biometricPrompt = BiometricPrompt(
            this,
            Executors.newSingleThreadExecutor(),
            viewModel.authenticationCallback
        )

        biometricPrompt.authenticate(params.promptInfo, params.cryptoObject)
    }

    private fun showBiometricEnableDialog() {
        AlertDialog.Builder(context!!)
            .setMessage("Do you want to enable biometric authentication?")
            .setPositiveButton("yes") { _, _ -> viewModel.enableBiometric() }
            .setNegativeButton("no") { _, _ -> findNavController().navigate(R.id.inputPinFragment) }
            .create()
            .show()

    }
}
