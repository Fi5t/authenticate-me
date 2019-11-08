package com.redmadrobot.authenticateme.unauthorized_zone.login.pin.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.redmadrobot.authenticateme.R
import kotlinx.android.synthetic.main.input_pin_fragment.*


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

        hidden_pin.onFilledListener = { viewModel.savePin(it) }
        keyboard.keyboardClickListener = { hidden_pin.add(it) }
    }
}

