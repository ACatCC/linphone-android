/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.activities.assistant.fragments

import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.R
import org.linphone.activities.GenericFragment
import org.linphone.activities.assistant.viewmodels.PhoneAccountValidationViewModel
import org.linphone.activities.assistant.viewmodels.PhoneAccountValidationViewModelFactory
import org.linphone.activities.assistant.viewmodels.SharedAssistantViewModel
import org.linphone.activities.main.navigateToAccountSettings
import org.linphone.activities.main.navigateToEchoCancellerCalibration
import org.linphone.databinding.AssistantPhoneAccountValidationFragmentBinding

class PhoneAccountValidationFragment : GenericFragment<AssistantPhoneAccountValidationFragmentBinding>() {
    private lateinit var sharedViewModel: SharedAssistantViewModel
    private lateinit var viewModel: PhoneAccountValidationViewModel

    override fun getLayoutId(): Int = R.layout.assistant_phone_account_validation_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.lifecycleOwner = this

        sharedViewModel = activity?.run {
            ViewModelProvider(this).get(SharedAssistantViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        viewModel = ViewModelProvider(this, PhoneAccountValidationViewModelFactory(sharedViewModel.getAccountCreator())).get(PhoneAccountValidationViewModel::class.java)
        binding.viewModel = viewModel

        viewModel.phoneNumber.value = arguments?.getString("PhoneNumber")
        viewModel.isLogin.value = arguments?.getBoolean("IsLogin", false)
        viewModel.isCreation.value = arguments?.getBoolean("IsCreation", false)
        viewModel.isLinking.value = arguments?.getBoolean("IsLinking", false)

        viewModel.leaveAssistantEvent.observe(viewLifecycleOwner, {
            it.consume {
                when {
                    viewModel.isLogin.value == true || viewModel.isCreation.value == true -> {
                        if (coreContext.core.isEchoCancellerCalibrationRequired) {
                            if (findNavController().currentDestination?.id == R.id.phoneAccountValidationFragment) {
                                navigateToEchoCancellerCalibration()
                            }
                        } else {
                            requireActivity().finish()
                        }
                    }
                    viewModel.isLinking.value == true -> {
                        if (findNavController().currentDestination?.id == R.id.phoneAccountValidationFragment) {
                            val args = Bundle()
                            args.putString("Identity", "sip:${viewModel.accountCreator.username}@${viewModel.accountCreator.domain}")
                            navigateToAccountSettings(args)
                        }
                    }
                }
            }
        })

        val clipboard = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            val data = clipboard.primaryClip
            if (data != null && data.itemCount > 0) {
                val clip = data.getItemAt(0).text.toString()
                if (clip.length == 4) {
                    viewModel.code.value = clip
                }
            }
        }
    }
}